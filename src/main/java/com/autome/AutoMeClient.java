package com.autome;

import com.autome.config.AutoMeConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import com.mojang.brigadier.arguments.StringArgumentType;

public class AutoMeClient implements ClientModInitializer {

    public static final String[] WHITELIST = { "mc.mangomc.top" };
    public static boolean onWhitelistedServer = false;

    @Override
    public void onInitializeClient() {
        AutoMeConfig.get();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                ClientCommandManager.literal("autome")

                    // /autome on
                    .then(ClientCommandManager.literal("on")
                        .executes(ctx -> {
                            AutoMeConfig cfg = AutoMeConfig.get();
                            cfg.enabled = true;
                            cfg.save();
                            ctx.getSource().sendFeedback(
                                Text.literal("[AutoMe] 已启用，前缀: " + cfg.prefix));
                            return 1;
                        }))

                    // /autome off
                    .then(ClientCommandManager.literal("off")
                        .executes(ctx -> {
                            AutoMeConfig cfg = AutoMeConfig.get();
                            cfg.enabled = false;
                            cfg.save();
                            ctx.getSource().sendFeedback(
                                Text.literal("[AutoMe] 已禁用"));
                            return 1;
                        }))

                    // /autome set <前缀>
                    .then(ClientCommandManager.literal("set")
                        .then(ClientCommandManager.argument("prefix", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                String input = StringArgumentType.getString(ctx, "prefix");
                                // 去掉首尾引号（兼容 "/me" 写法）
                                if ((input.startsWith("\"") && input.endsWith("\"")) ||
                                    (input.startsWith("'") && input.endsWith("'"))) {
                                    input = input.substring(1, input.length() - 1);
                                }
                                AutoMeConfig cfg = AutoMeConfig.get();
                                cfg.prefix = input.trim();
                                cfg.save();
                                ctx.getSource().sendFeedback(
                                    Text.literal("[AutoMe] 前缀已设置为: " + cfg.prefix));
                                return 1;
                            })))

                    // /autome status
                    .then(ClientCommandManager.literal("status")
                        .executes(ctx -> {
                            AutoMeConfig cfg = AutoMeConfig.get();
                            ctx.getSource().sendFeedback(Text.literal(
                                "[AutoMe] 状态: " + (cfg.enabled ? "ON" : "OFF") +
                                " | 前缀: " + cfg.prefix +
                                " | 白名单服务器: " + (onWhitelistedServer ? "是" : "否")));
                            return 1;
                        }))

                    // /autome（无参数，显示帮助）
                    .executes(ctx -> {
                        ctx.getSource().sendFeedback(Text.literal(
                            "[AutoMe] 用法:\n" +
                            "  /autome on        - 启用\n" +
                            "  /autome off       - 禁用\n" +
                            "  /autome set /me   - 设置前缀\n" +
                            "  /autome set \"/me\" - 含斜杠时加引号\n" +
                            "  /autome status    - 查看状态"));
                        return 1;
                    })
            );
        });

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

        // 纯数字不转换
        if (message.matches("^[0-9]+$")) return message;

        // "all" 不转换
        if (message.equalsIgnoreCase("all")) return message;

        String prefix = cfg.prefix == null ? "/me" : cfg.prefix.trim();
        if (prefix.isEmpty()) return message;

        return prefix + " " + message;
    }
}
