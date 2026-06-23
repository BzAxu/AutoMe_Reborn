package com.autome.gui;

import com.autome.config.AutoMeConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class AutoMeScreen extends Screen {

    private final Screen parent;
    private TextFieldWidget prefixField;
    private ButtonWidget toggleButton;
    private boolean enabled;
    private String prefix;

    private static final int PANEL_W = 320;
    private static final int PANEL_H = 200;

    public AutoMeScreen(Screen parent) {
        super(Text.literal("AutoMe 设置"));
        this.parent = parent;
        AutoMeConfig cfg = AutoMeConfig.get();
        this.enabled = cfg.enabled;
        this.prefix = cfg.prefix != null ? cfg.prefix : "/me";
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;
        int panelTop = cy - PANEL_H / 2;
        int panelLeft = cx - PANEL_W / 2;

        toggleButton = ButtonWidget.builder(
                Text.literal("启用聊天前缀：" + (enabled ? "§a[ON]" : "§c[OFF]")),
                btn -> {
                    enabled = !enabled;
                    btn.setMessage(Text.literal("启用聊天前缀：" + (enabled ? "§a[ON]" : "§c[OFF]")));
                })
                .dimensions(panelLeft + 20, panelTop + 50, PANEL_W - 40, 20)
                .build();
        this.addDrawableChild(toggleButton);

        prefixField = new TextFieldWidget(
                this.textRenderer,
                panelLeft + 20, panelTop + 105,
                PANEL_W - 40, 20,
                Text.literal("聊天前缀内容")
        );
        prefixField.setMaxLength(64);
        prefixField.setText(prefix);
        prefixField.setPlaceholder(Text.literal("/me"));
        this.addDrawableChild(prefixField);

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§a确定"),
                btn -> confirm())
                .dimensions(cx - 105, panelTop + PANEL_H - 35, 100, 20)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§c取消"),
                btn -> cancel())
                .dimensions(cx + 5, panelTop + PANEL_H - 35, 100, 20)
                .build());
    }

    private void confirm() {
        AutoMeConfig cfg = AutoMeConfig.get();
        cfg.enabled = enabled;
        cfg.prefix = prefixField.getText().trim().isEmpty() ? "/me" : prefixField.getText().trim();
        cfg.save();
        close();
    }

    private void cancel() {
        close();
    }

    @Override
    public void close() {
        assert this.client != null;
        this.client.setScreen(parent);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0xAA000000);

        int cx = this.width / 2;
        int cy = this.height / 2;
        int panelTop    = cy - PANEL_H / 2;
        int panelBottom = cy + PANEL_H / 2;
        int panelLeft   = cx - PANEL_W / 2;
        int panelRight  = cx + PANEL_W / 2;

        ctx.fill(panelLeft, panelTop, panelRight, panelBottom, 0xCC101010);
        ctx.fill(panelLeft,      panelTop,        panelRight,     panelTop + 1,    0xFF555555);
        ctx.fill(panelLeft,      panelBottom - 1, panelRight,     panelBottom,     0xFF555555);
        ctx.fill(panelLeft,      panelTop,        panelLeft + 1,  panelBottom,     0xFF555555);
        ctx.fill(panelRight - 1, panelTop,        panelRight,     panelBottom,     0xFF555555);

        ctx.drawText(this.textRenderer,
                Text.literal("§e§lAutoMe §r§7设置"),
                cx - this.textRenderer.getWidth("AutoMe 设置") / 2,
                panelTop + 16, 0xFFFFFF, true);

        ctx.drawText(this.textRenderer,
                Text.literal("§7聊天前缀内容："),
                panelLeft + 20, panelTop + 88, 0xAAAAAA, true);

        super.render(ctx, mouseX, mouseY, delta);

        ctx.drawText(this.textRenderer,
                Text.literal("§8示例：/me  /say  /me 前缀"),
                cx - this.textRenderer.getWidth("示例：/me  /say  /me 前缀") / 2,
                panelTop + 132, 0x888888, true);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
