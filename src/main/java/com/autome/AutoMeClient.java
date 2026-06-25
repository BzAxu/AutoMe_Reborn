package com.autome;

import com.autome.config.AutoMeConfig;
import com.autome.data.FilterMatcher;
import com.autome.data.TuiBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class AutoMeClient implements ClientModInitializer {

    public static boolean awaitingPrefix = false;

    @Override
    public void onInitializeClient() {
        AutoMeConfig.get();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                ClientCommandManager.literal("autome")

                    .then(ClientCommandManager.literal("on").executes(ctx -> {
                        AutoMeConfig cfg = AutoMeConfig.get();
                        cfg.enabled = true; cfg.save();
                        send("[AutoMe] 已启用，前缀: " + cfg.prefix, Formatting.GREEN);
                        return 1;
                    }))

                    .then(ClientCommandManager.literal("off").executes(ctx -> {
                        AutoMeConfig cfg = AutoMeConfig.get();
                        cfg.enabled = false; cfg.save();
                        send("[AutoMe] 已禁用", Formatting.RED);
                        return 1;
                    }))

                    .then(ClientCommandManager.literal("set")
                        .then(ClientCommandManager.argument("prefix", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                String input = StringArgumentType.getString(ctx, "prefix").trim();
                                if ((input.startsWith("\"") && input.endsWith("\"")) ||
                                    (input.startsWith("'") && input.endsWith("'")))
                                    input = input.substring(1, input.length() - 1).trim();
                                AutoMeConfig cfg = AutoMeConfig.get();
                                cfg.addHistory(cfg.prefix);
                                cfg.prefix = input;
                                cfg.save();
                                send("[AutoMe] 前缀已设置: " + input, Formatting.GREEN);
                                return 1;
                            })))

                    .then(ClientCommandManager.literal("setprefix").executes(ctx -> {
                        awaitingPrefix = true;
                        send("[AutoMe] 请输入新前缀，下一条消息将被设为前缀", Formatting.YELLOW);
                        return 1;
                    }))

                    .then(ClientCommandManager.literal("status").executes(ctx -> {
                        AutoMeConfig cfg = AutoMeConfig.get();
                        send("[AutoMe] " + (cfg.enabled ? "ON" : "OFF") +
                            " | 前缀: " + cfg.prefix +
                            " | 配置: " + AutoMeConfig.getCurrentKey(), Formatting.GRAY);
                        return 1;
                    }))

                    .then(ClientCommandManager.literal("history").executes(ctx -> {
                        TuiBuilder.sendHistory();
                        return 1;
                    }))

                    .then(ClientCommandManager.literal("pin")
                        .then(ClientCommandManager.argument("prefix", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                String p = StringArgumentType.getString(ctx, "prefix").trim();
                                AutoMeConfig.get().pin(p);
                                send("[AutoMe] 已置顶: " + p, Formatting.YELLOW);
                                return 1;
                            })))

                    .then(ClientCommandManager.literal("unpin")
                        .then(ClientCommandManager.argument("prefix", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                String p = StringArgumentType.getString(ctx, "prefix").trim();
                                AutoMeConfig.get().unpin(p);
                                send("[AutoMe] 已取消置顶: " + p, Formatting.GRAY);
                                return 1;
                            })))

                    .then(ClientCommandManager.literal("filter")
                        .then(ClientCommandManager.literal("list").executes(ctx -> {
                            TuiBuilder.sendFilterList();
                            return 1;
                        }))
                        .then(ClientCommandManager.literal("add")
                            .then(ClientCommandManager.argument("word", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    String w = StringArgumentType.getString(ctx, "word").trim();
                                    AutoMeConfig.get().addFilter(w);
                                    send("[AutoMe] 已添加屏蔽词: " + w, Formatting.LIGHT_PURPLE);
                                    return 1;
                                })))
                        .then(ClientCommandManager.literal("del")
                            .then(ClientCommandManager.argument("word", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    String w = StringArgumentType.getString(ctx, "word").trim();
                                    AutoMeConfig.get().removeFilter(w);
                                    send("[AutoMe] 已删除屏蔽词: " + w, Formatting.RED);
                                    return 1;
                                }))))

                    .executes(ctx -> {
                        TuiBuilder.sendMain();
                        return 1;
                    })
            );
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            String address = client.getCurrentServerEntry() != null
                    ? client.getCurrentServerEntry().address : "singleplayer";
            AutoMeConfig.setCurrentKey(address);
            AutoMeConfig.get();
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            AutoMeConfig.setCurrentKey("default");
        });
    }

    private static void send(String msg, Formatting color) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null)
            mc.player.sendMessage(Text.literal(msg).formatted(color), false);
    }

    public static String transformMessage(String message) {
        if (awaitingPrefix) {
            awaitingPrefix = false;
            AutoMeConfig cfg = AutoMeConfig.get();
            cfg.addHistory(cfg.prefix);
            cfg.prefix = message;
            cfg.save();
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null)
                mc.player.sendMessage(
                    Text.literal("[AutoMe] 前缀已设为: " + message).formatted(Formatting.GREEN), false);
            return null;
        }

        AutoMeConfig cfg = AutoMeConfig.get();
        if (!cfg.enabled) return message;
        if (message.isEmpty()) return message;
        if (FilterMatcher.shouldBypass(message, cfg.filters)) return message;

        String prefix = cfg.prefix == null ? "/me" : cfg.prefix.trim();
        if (prefix.isEmpty()) return message;

        return prefix + " " + message;
    }
}
