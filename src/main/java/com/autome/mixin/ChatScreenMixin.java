package com.autome.mixin;

import com.autome.AutoMeClient;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @ModifyVariable(
            method = "sendMessage",
            at = @At("HEAD"),
            argsOnly = true,
            require = 0
    )
    private String autome_transformMessage(String message) {
        String result = AutoMeClient.transformMessage(message);
        return result != null ? result : message;
    }
}
