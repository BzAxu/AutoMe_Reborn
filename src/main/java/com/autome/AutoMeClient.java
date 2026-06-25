package com.autome;

import com.autome.config.AutoMeConfig;
import com.autome.data.FilterMatcher;
import com.autome.data.TuiBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class AutoMeClient implements ClientModInitializer {

    public static boolean awaitingPrefix = false;

    @Override
    public void onInitializeClient() {
        AutoMeConfig.get();

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

    public static void handleDotCommand(String input) {
        // 去掉 .autome 前缀，取后续参数
        String args = input.length() > 7 ? input.substring(7).trim() : "";

        if (args.isEmpty()) {
            TuiBuilder.sendMain();
            return;
        }

        String[] parts = args.split("\\s+", 3);
        String sub = parts[0].toLowerCase();

        switch (sub) {
            case "on" -> {
                AutoMeConfig cfg = AutoMeConfig.get();
                cfg.enabled = true; cfg.save();
                send("[AutoMe] 已启用，前缀: " + cfg.prefix, Formatting.GREEN);
            }
            case "off" -> {
                AutoMeConfig cfg = AutoMeConfig.get();
                cfg.enabled = false; cfg.save();
                send("[AutoMe] 已禁用", Formatting.RED);
            }
            case "set" -> {
                if (parts.length < 2) { send("[AutoMe] 用法: .autome set <前缀>", Formatting.YELLOW); return; }
                String p = args.substring(3).trim();
                if ((p.startsWith("\"") && p.endsWith("\"")) ||
                    (p.startsWith("'") && p.endsWith("'")))
                    p = p.substring(1, p.length() - 1).trim();
                AutoMeConfig cfg = AutoMeConfig.get();
                cfg.addHistory(cfg.prefix);
                cfg.prefix = p; cfg.save();
                send("[AutoMe] 前缀已设置: " + p, Formatting.GREEN);
            }
            case "setprefix" -> {
                awaitingPrefix = true;
                send("[AutoMe] 请输入新前缀，下一条消息将被设为前缀", Formatting.YELLOW);
            }
            case "status" -> {
                AutoMeConfig cfg = AutoMeConfig.get();
                send("[AutoMe] " + (cfg.enabled ? "ON" : "OFF") +
                    " | 前缀: " + cfg.prefix +
                    " | 配置: " + AutoMeConfig.getCurrentKey(), Formatting.GRAY);
            }
            case "history" -> TuiBuilder.sendHistory();
            case "pin" -> {
                if (parts.length < 2) return;
                String p = args.substring(3).trim();
                AutoMeConfig.get().pin(p);
                send("[AutoMe] 已置顶: " + p, Formatting.YELLOW);
            }
            case "unpin" -> {
                if (parts.length < 2) return;
                String p = args.substring(5).trim();
                AutoMeConfig.get().unpin(p);
                send("[AutoMe] 已取消置顶: " + p, Formatting.GRAY);
            }
            case "filter" -> {
                if (parts.length < 2) { TuiBuilder.sendFilterList(); return; }
                String action = parts[1].toLowerCase();
                String word = parts.length > 2 ? parts[2].trim() : "";
                switch (action) {
                    case "list" -> TuiBuilder.sendFilterList();
                    case "add" -> {
                        if (word.isEmpty()) return;
                        AutoMeConfig.get().addFilter(word);
                        send("[AutoMe] 已添加屏蔽词: " + word, Formatting.LIGHT_PURPLE);
                    }
                    case "del" -> {
                        if (word.isEmpty()) return;
                        AutoMeConfig.get().removeFilter(word);
                        send("[AutoMe] 已删除屏蔽词: " + word, Formatting.RED);
                    }
                }
            }
            default -> send("[AutoMe] 未知命令: " + sub, Formatting.RED);
        }
    }

    public static void send(String msg, Formatting color) {
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
