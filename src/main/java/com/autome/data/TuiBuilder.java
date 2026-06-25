package com.autome.data;

import com.autome.config.AutoMeConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

public class TuiBuilder {

    public static void sendMain() {
        AutoMeConfig cfg = AutoMeConfig.get();
        String displayPrefix = cfg.prefix != null && cfg.prefix.contains("/me")
                ? "[称号]" : cfg.prefix;

        send(Text.literal(""));
        send(Text.literal("══════ AutoMe ══════").formatted(Formatting.GOLD, Formatting.BOLD));
        send(Text.literal("状态: ").formatted(Formatting.GRAY)
            .append(cfg.enabled
                ? Text.literal("■ 开启").formatted(Formatting.GREEN)
                : Text.literal("■ 关闭").formatted(Formatting.RED))
            .append(Text.literal("  前缀: ").formatted(Formatting.GRAY))
            .append(Text.literal(displayPrefix).formatted(Formatting.AQUA)));
        send(Text.literal("────────────────────").formatted(Formatting.DARK_GRAY));

        MutableText row1 = Text.literal("");
        row1.append(cfg.enabled
            ? btn("[ 关闭 ]", "/autome off", Formatting.RED, "关闭 AutoMe")
            : btn("[ 开启 ]", "/autome on", Formatting.GREEN, "开启 AutoMe"));
        row1.append(Text.literal("  "));
        row1.append(btn("[ 设置前缀 ]", "/autome setprefix", Formatting.YELLOW, "输入新前缀"));
        send(row1);

        MutableText row2 = Text.literal("");
        row2.append(btn("[ 过滤列表 ]", "/autome filter list", Formatting.LIGHT_PURPLE, "查看屏蔽词"));
        row2.append(Text.literal("  "));
        row2.append(btn("[ 历史记录 ]", "/autome history", Formatting.AQUA, "查看前缀历史"));
        send(row2);

        send(Text.literal("════════════════════").formatted(Formatting.DARK_GRAY));
    }

    public static void sendHistory() {
        AutoMeConfig cfg = AutoMeConfig.get();
        send(Text.literal(""));
        send(Text.literal("══════ 历史前缀 ══════").formatted(Formatting.GOLD, Formatting.BOLD));

        for (String p : cfg.pinned) {
            MutableText line = Text.literal("★ ").formatted(Formatting.YELLOW)
                .append(Text.literal(p + " ").formatted(Formatting.WHITE))
                .append(btn("[设置]", "/autome set " + p, Formatting.GREEN, "设为当前前缀"))
                .append(Text.literal(" "))
                .append(btn("[取消置顶]", "/autome unpin " + p, Formatting.GRAY, "取消置顶"));
            send(line);
        }

        if (!cfg.pinned.isEmpty() && !cfg.history.isEmpty())
            send(Text.literal("────────────────────").formatted(Formatting.DARK_GRAY));

        for (String p : cfg.history) {
            MutableText line = Text.literal("  " + p + " ").formatted(Formatting.GRAY)
                .append(btn("[设置]", "/autome set " + p, Formatting.GREEN, "设为当前前缀"))
                .append(Text.literal(" "))
                .append(btn("[置顶]", "/autome pin " + p, Formatting.YELLOW, "置顶此前缀"));
            send(line);
        }

        if (cfg.pinned.isEmpty() && cfg.history.isEmpty())
            send(Text.literal("  暂无历史记录").formatted(Formatting.DARK_GRAY));

        send(Text.literal("════════════════════").formatted(Formatting.DARK_GRAY));
    }

    public static void sendFilterList() {
        AutoMeConfig cfg = AutoMeConfig.get();
        send(Text.literal(""));
        send(Text.literal("══════ 屏蔽词列表 ══════").formatted(Formatting.GOLD, Formatting.BOLD));

        for (String f : cfg.filters) {
            MutableText line = Text.literal("  " + f + " ").formatted(Formatting.WHITE)
                .append(btn("[删除]", "/autome filter del " + f, Formatting.RED, "删除此屏蔽词"));
            send(line);
        }

        if (cfg.filters.isEmpty())
            send(Text.literal("  暂无屏蔽词").formatted(Formatting.DARK_GRAY));

        send(Text.literal("════════════════════").formatted(Formatting.DARK_GRAY));
    }

    private static void send(Text text) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) mc.player.sendMessage(text, false);
    }

    private static MutableText btn(String label, String command, Formatting color, String hover) {
        return Text.literal(label).formatted(color)
            .styled(s -> s
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    Text.literal(hover).formatted(Formatting.GRAY))));
    }
}
