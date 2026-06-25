package com.autome.mixin;

import com.autome.AutoMeClient;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @Inject(method = "sendMessage", at = @At("HEAD"), cancellable = true)
    private void autome_onSendMessage(String message, boolean addToHistory, CallbackInfo ci) {
        String result = AutoMeClient.transformMessage(message);
        if (result == null) {
            // setprefix 模式，拦截消息不发送
            ci.cancel();
            return;
        }
        if (!result.equals(message)) {
            ci.cancel();
            net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
            if (mc.player != null) {
                if (result.startsWith("/")) {
                    mc.player.networkHandler.sendChatCommand(result.substring(1));
                } else {
                    mc.player.networkHandler.sendChatMessage(result);
                }
            }
        }
    }
}
