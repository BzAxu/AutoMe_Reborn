package com.autome;

import com.autome.config.AutoMeConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import com.autome.gui.AutoMeScreen;

public class AutoMeClient implements ClientModInitializer {

    public static final String[] WHITELIST = {
            "mc.mangomc.top"
    };

    public static boolean onWhitelistedServer = false;

    @Override
    public void onInitializeClient() {
        AutoMeConfig.get();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(
                        ClientCommandManager.literal("autome")
                                .executes(ctx -> {
                                    MinecraftClient.getInstance().execute(() ->
                                            MinecraftClient.getInstance().setScreen(
                                                    new AutoMeScreen(MinecraftClient.getInstance().currentScreen)
                                            )
                                    );
                                    return 1;
                                })
                )
        );

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            String address = client.getCurrentServerEntry() != null
                    ? client.getCurrentServerEntry().address : "";

            onWhitelistedServer = false;
            for (String wl : WHITELIST) {
                if (address.contains(wl)) {
                    onWhitelistedServer = true;
                    break;
                }
            }

            if (onWhitelistedServer) {
                AutoMeConfig cfg = AutoMeConfig.get();
                if (!cfg.enabled) {
                    cfg.enabled = true;
                    cfg.save();
                }
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            onWhitelistedServer = false;
        });
    }

    private static final char[] BYPASS_PREFIXES = {'/', '.', '#', '$', '&', '*', '@'};

    public static String transformMessage(String message) {
        AutoMeConfig cfg = AutoMeConfig.get();

        if (!cfg.enabled) return message;
        if (message.isEmpty()) return message;

        char first = message.charAt(0);
        for (char bp : BYPASS_PREFIXES) {
            if (first == bp) return message;
        }

        String prefix = cfg.prefix == null ? "/me" : cfg.prefix.trim();
        if (prefix.isEmpty()) return message;

        return prefix + " " + message;
    }
}
