package com.autome.mixin;

import com.autome.AutoMeClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @Shadow protected TextFieldWidget chatField;

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void autome_onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        // 257 = Enter, 335 = Numpad Enter
        if (keyCode != 257 && keyCode != 335) return;

        String message = chatField.getText().trim();
        if (message.isEmpty()) return;

        // 只处理非命令消息（命令以/开头让原版处理）
        if (message.startsWith("/")) return;

        String result = AutoMeClient.transformMessage(message);
        if (result == null) {
            // setprefix 模式，拦截
            chatField.setText("");
            ((ChatScreen)(Object)this).close();
            cir.setReturnValue(true);
            return;
        }

        if (!result.equals(message)) {
            // 有前缀变换，拦截并手动发送
            chatField.setText("");
            ((ChatScreen)(Object)this).close();
            net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
            if (mc.player != null) {
                if (result.startsWith("/")) {
                    mc.player.networkHandler.sendChatCommand(result.substring(1));
                } else {
                    mc.player.networkHandler.sendChatMessage(result);
                }
            }
            cir.setReturnValue(true);
        }
    }
}
