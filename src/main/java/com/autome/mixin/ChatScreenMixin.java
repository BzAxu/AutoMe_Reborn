package com.autome.mixin;

import com.autome.AutoMeClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {

    @Inject(method = "sendMessage", at = @At("HEAD"), cancellable = true, require = 0)
    private void autome_onSend(String message, boolean addToHistory, CallbackInfo ci) {
        String trimmed = message.trim();
        MinecraftClient mc = MinecraftClient.getInstance();

        // 拦截 /autome 命令，由客户端本地处理，不发送给服务器。
        if (trimmed.equals("/autome") || trimmed.startsWith("/autome ")) {
            ci.cancel();
            // 输入管理命令时取消尚未完成的“下一条消息设为前缀”状态。
            AutoMeClient.awaitingPrefix = false;
            String args = trimmed.length() > 7 ? trimmed.substring(7).trim() : "";
            AutoMeClient.handleCommand(args);
            mc.execute(() -> mc.setScreen(null));
            return;
        }

        if (trimmed.isEmpty()) {
            return;
        }

        // setprefix 等待输入时，前缀本身可以是命令（例如 /me）。
        // 必须先处理这个状态，否则斜杠开头的前缀会被直接发往服务器。
        if (AutoMeClient.awaitingPrefix) {
            ci.cancel();
            AutoMeClient.transformMessage(trimmed);
            mc.execute(() -> mc.setScreen(null));
            return;
        }

        // 普通聊天才添加前缀；其他 Minecraft 命令原样交给游戏处理。
        if (!trimmed.startsWith("/")) {
            String result = AutoMeClient.transformMessage(trimmed);
            if (result == null) {
                ci.cancel();
                mc.execute(() -> mc.setScreen(null));
                return;
            }

            if (!result.equals(trimmed)) {
                ci.cancel();
                mc.execute(() -> {
                    if (mc.player == null) return;
                    // 前缀可能是 /me、/msg 等命令，必须走命令发送接口。
                    if (result.startsWith("/")) {
                        mc.player.networkHandler.sendChatCommand(result.substring(1));
                    } else {
                        mc.player.networkHandler.sendChatMessage(result);
                    }
                });
            }
        }
    }
}
