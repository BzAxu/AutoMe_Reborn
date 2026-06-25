package com.autome.data;

import com.autome.config.AutoMeConfig;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.List;

public class TuiBuilder {

    public static Text buildMain() {
        AutoMeConfig cfg = AutoMeConfig.get();
        String displayPrefix = cfg.prefix != null && cfg.prefix.contains("/me")
                ? "[称号]" : cfg.prefix;

        MutableText out = Text.literal("\n")
            .append(Text.literal("═══ AutoMe ═══\n").formatted(Formatting.GOLD, Formatting.BOLD))
            .append(Text.literal("状态: ").formatted(Formatting.GRAY))
            .append(cfg.enabled
                ? Text.literal("■ 开启").formatted(Formatting.GREEN)
                : Text.literal("■ 关闭").formatted(Formatting.RED))
            .append(Text.literal("  前缀: ").formatted(Formatting.GRAY))
            .append(Text.literal(displayPrefix).formatted(Formatting.AQUA))
            .append(Text.literal("\n──────────────\n").formatted(Formatting.DARK_GRAY));

        // 开关按钮
        out.append(cfg.enabled
            ? clickBtn("[ 关闭 ]", "/autome off", Formatting.RED, "点击关闭 AutoMe")
            : clickBtn("[ 开启 ]", "/autome on", Formatting.GREEN, "点击开启 AutoMe"));
        out.append(Text.literal("  "));

        // 设置前缀
        out.append(clickBtn("[ 设置前缀 ]", "/autome setprefix", Formatting.YELLOW, "点击后在聊天栏输入新前缀"));
        out.append(Text.literal("  "));

        // 过滤列表
        out.append(clickBtn("[ 过滤列表 ]", "/autome filter list", Formatting.LIGHT_PURPLE, "查看屏蔽词列表"));
        out.append(Text.literal("  "));

        // 历史记录
        out.append(clickBtn("[ 历史记录 ]", "/autome history", Formatting.AQUA, "查看前缀历史"));
        out.append(Text.literal("\n═══════════════\n").formatted(Formatting.DARK_GRAY));

        return out;
    }

    public static Text buildHistory() {
        AutoMeConfig cfg = AutoMeConfig.get();
        MutableText out = Text.literal("\n")
            .append(Text.literal("═══ 历史前缀 ═══\n").formatted(Formatting.GOLD, Formatting.BOLD));

        // 置顶
        for (String p : cfg.pinned) {
            out.append(Text.literal("★ ").formatted(Formatting.YELLOW))
               .append(Text.literal(p + " ").formatted(Formatting.WHITE))
               .append(clickBtn("[设置]", "/autome set " + p, Formatting.GREEN, "设置为当前前缀"))
               .append(Text.literal(" "))
               .append(clickBtn("[取消置顶]", "/autome unpin " + p, Formatting.GRAY, "取消置顶"))
               .append(Text.literal("\n"));
        }

        if (!cfg.pinned.isEmpty() && !cfg.history.isEmpty()) {
            out.append(Text.literal("──────────────\n").formatted(Formatting.DARK_GRAY));
        }

        // 历史
        for (String p : cfg.history) {
            out.append(Text.literal("  " + p + " ").formatted(Formatting.GRAY))
               .append(clickBtn("[设置]", "/autome set " + p, Formatting.GREEN, "设置为当前前缀"))
               .append(Text.literal(" "))
               .append(clickBtn("[置顶]", "/autome pin " + p, Formatting.YELLOW, "置顶此前缀"))
               .append(Text.literal("\n"));
        }

        if (cfg.pinned.isEmpty() && cfg.history.isEmpty()) {
            out.append(Text.literal("  暂无历史记录\n").formatted(Formatting.DARK_GRAY));
        }

        out.append(Text.literal("═══════════════\n").formatted(Formatting.DARK_GRAY));
        return out;
    }

    public static Text buildFilterList() {
        AutoMeConfig cfg = AutoMeConfig.get();
        MutableText out = Text.literal("\n")
            .append(Text.literal("═══ 屏蔽词列表 ═══\n").formatted(Formatting.GOLD, Formatting.BOLD));

        for (String f : cfg.filters) {
            out.append(Text.literal("  " + f + " ").formatted(Formatting.WHITE))
               .append(clickBtn("[删除]", "/autome filter del " + f, Formatting.RED, "删除此屏蔽词"))
               .append(Text.literal("\n"));
        }

        if (cfg.filters.isEmpty()) {
            out.append(Text.literal("  暂无屏蔽词\n").formatted(Formatting.DARK_GRAY));
        }

        out.append(Text.literal("═══════════════\n").formatted(Formatting.DARK_GRAY));
        return out;
    }

    private static MutableText clickBtn(String label, String command, Formatting color, String hover) {
        return Text.literal(label).formatted(color)
                .styled(s -> s
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            Text.literal(hover).formatted(Formatting.GRAY))));
    }
}
