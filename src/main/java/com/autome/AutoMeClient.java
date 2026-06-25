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

    // 是否在等待玩家输入新前缀
    public static boolean awaitingPrefix = false;

    @Override
    public void onInitializeClient() {
        AutoMeConfig.get();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                ClientCommandManager.literal("autome")

                    // /autome on
                    .then(ClientCommandManager.literal("on").executes(ctx -> {
                        AutoMeConfig cfg = AutoMeConfig.get();
                        cfg.enabled = true; cfg.save();
                        ctx.getSource().sendFeedback(Text.literal("[AutoMe] 已启用，前缀: " + cfg.prefix).formatted(Formatting.GREEN));
                        return 1;
                    }))

                    // /autome off
                    .then(ClientCommandManager.literal("off").executes(ctx -> {
                        AutoMeConfig cfg = AutoMeConfig.get();
                        cfg.enabled = false; cfg.save();
                        ctx.getSource().sendFeedback(Text.literal("[AutoMe] 已禁用").formatted(Formatting.RED));
                        return 1;
                    }))

                    // /autome set <prefix>
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
                                ctx.getSource().sendFeedback(Text.literal("[AutoMe] 前缀已设置: " + input).formatted(Formatting.GREEN));
                                return 1;
                            })))

                    // /autome setprefix（TUI 点击后等待下一条消息）
                    .then(ClientCommandManager.literal("setprefix").executes(ctx -> {
                        awaitingPrefix = true;
                        ctx.getSource().sendFeedback(Text.literal("[AutoMe] 请在聊天栏输入新前缀，下一条消息将被设为前缀").formatted(Formatting.YELLOW));
                        return 1;
                    }))

                    // /autome status
                    .then(ClientCommandManager.literal("status").executes(ctx -> {
                        AutoMeConfig cfg = AutoMeConfig.get();
                        ctx.getSource().sendFeedback(Text.literal(
                            "[AutoMe] " + (cfg.enabled ? "ON" : "OFF") +
                            " | 前缀: " + cfg.prefix +
                            " | 配置: " + AutoMeConfig.getCurrentKey()));
                        return 1;
                    }))

                    // /autome history
                    .then(ClientCommandManager.literal("history").executes(ctx -> {
                        ctx.getSource().sendFeedback(TuiBuilder.buildHistory());
                        return 1;
                    }))

                    // /autome pin <prefix>
                    .then(ClientCommandManager.literal("pin")
                        .then(ClientCommandManager.argument("prefix", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                String p = StringArgumentType.getString(ctx, "prefix").trim();
                                AutoMeConfig.get().pin(p);
                                ctx.getSource().sendFeedback(Text.literal("[AutoMe] 已置顶: " + p).formatted(Formatting.YELLOW));
                                return 1;
                            })))

                    // /autome unpin <prefix>
                    .then(ClientCommandManager.literal("unpin")
                        .then(ClientCommandManager.argument("prefix", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                String p = StringArgumentType.getString(ctx, "prefix").trim();
                                AutoMeConfig.get().unpin(p);
                                ctx.getSource().sendFeedback(Text.literal("[AutoMe] 已取消置顶: " + p).formatted(Formatting.GRAY));
                                return 1;
                            })))

                    // /autome filter
                    .then(ClientCommandManager.literal("filter")
                        .then(ClientCommandManager.literal("list").executes(ctx -> {
                            ctx.getSource().sendFeedback(TuiBuilder.buildFilterList());
                            return 1;
                        }))
                        .then(ClientCommandManager.literal("add")
                            .then(ClientCommandManager.argument("word", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    String w = StringArgumentType.getString(ctx, "word").trim();
                                    AutoMeConfig.get().addFilter(w);
                                    ctx.getSource().sendFeedback(Text.literal("[AutoMe] 已添加屏蔽词: " + w).formatted(Formatting.LIGHT_PURPLE));
                                    return 1;
                                })))
                        .then(ClientCommandManager.literal("del")
                            .then(ClientCommandManager.argument("word", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    String w = StringArgumentType.getString(ctx, "word").trim();
                                    AutoMeConfig.get().removeFilter(w);
                                    ctx.getSource().sendFeedback(Text.literal("[AutoMe] 已删除屏蔽词: " + w).formatted(Formatting.RED));
                                    return 1;
                                }))))

                    // /autome（显示 TUI）
                    .executes(ctx -> {
                        ctx.getSource().sendFeedback(TuiBuilder.buildMain());
                        return 1;
                    })
            );
        });

        // 服务器加入：设置配置 key 为服务器地址
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            String address = client.getCurrentServerEntry() != null
                    ? client.getCurrentServerEntry().address : "singleplayer";
            AutoMeConfig.setCurrentKey(address);
            AutoMeConfig.get(); // 加载该服务器配置
        });

        // 断开连接：重置为默认
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            AutoMeConfig.setCurrentKey("default");
        });
    }

    public static String transformMessage(String message) {
        // setprefix 模式：下一条消息直接成为前缀
        if (awaitingPrefix) {
            awaitingPrefix = false;
            AutoMeConfig cfg = AutoMeConfig.get();
            cfg.addHistory(cfg.prefix);
            cfg.prefix = message;
            cfg.save();
            MinecraftClient.getInstance().player.sendMessage(
                Text.literal("[AutoMe] 前缀已设为: " + message).formatted(Formatting.GREEN), false);
            return null; // 拦截，不发送到聊天
        }

        AutoMeConfig cfg = AutoMeConfig.get();
        if (!cfg.enabled) return message;
        if (message.isEmpty()) return message;

        // 屏蔽词过滤
        if (FilterMatcher.shouldBypass(message, cfg.filters)) return message;

        String prefix = cfg.prefix == null ? "/me" : cfg.prefix.trim();
        if (prefix.isEmpty()) return message;

        return prefix + " " + message;
    }
}
