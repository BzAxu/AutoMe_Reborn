package com.autome.mixin;

import com.autome.AutoMeClient;
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

        // 拦截 .autome 命令
        if (trimmed.startsWith(".autome")) {
            ci.cancel();
            AutoMeClient.handleDotCommand(trimmed);
            // 关闭聊天框
            net.minecraft.client.MinecraftClient.getInstance().execute(() ->
                net.minecraft.client.MinecraftClient.getInstance().setScreen(null));
            return;
        }

        // 普通消息前缀处理
        if (!trimmed.startsWith("/")) {
            String result = AutoMeClient.transformMessage(trimmed);
            if (result == null) {
                ci.cancel();
                net.minecraft.client.MinecraftClient.getInstance().execute(() ->
                    net.minecraft.client.MinecraftClient.getInstance().setScreen(null));
                return;
            }
            if (!result.equals(trimmed)) {
                ci.cancel();
                net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
                final String finalResult = result;
                mc.execute(() -> {
                    if (mc.player != null) {
                        mc.player.networkHandler.sendChatMessage(finalResult);
                    }
                });
            }
        }
    }
}
