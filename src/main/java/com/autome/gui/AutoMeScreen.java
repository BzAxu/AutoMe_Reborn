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
        super(Text.literal("AutoMe"));
        this.parent = parent;
        AutoMeConfig cfg = AutoMeConfig.get();
        this.enabled = cfg.enabled;
        this.prefix = cfg.prefix != null ? cfg.prefix : "/me";
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;
        int panelTop  = cy - PANEL_H / 2;
        int panelLeft = cx - PANEL_W / 2;

        toggleButton = ButtonWidget.builder(
                Text.literal("启用前缀: " + (enabled ? "[ON]" : "[OFF]")),
                btn -> {
                    enabled = !enabled;
                    btn.setMessage(Text.literal("启用前缀: " + (enabled ? "[ON]" : "[OFF]")));
                })
                .dimensions(panelLeft + 20, panelTop + 50, PANEL_W - 40, 20)
                .build();
        this.addDrawableChild(toggleButton);

        prefixField = new TextFieldWidget(
                this.textRenderer,
                panelLeft + 20, panelTop + 100,
                PANEL_W - 40, 20,
                Text.literal("前缀")
        );
        prefixField.setMaxLength(64);
        prefixField.setText(prefix);
        prefixField.setPlaceholder(Text.literal("/me"));
        this.addDrawableChild(prefixField);

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("确定"),
                btn -> confirm())
                .dimensions(cx - 105, panelTop + PANEL_H - 35, 100, 20)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("取消"),
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
        // 全屏遮罩
        ctx.fill(0, 0, this.width, this.height, 0xAA000000);

        int cx = this.width / 2;
        int cy = this.height / 2;
        int pt = cy - PANEL_H / 2;
        int pb = cy + PANEL_H / 2;
        int pl = cx - PANEL_W / 2;
        int pr = cx + PANEL_W / 2;

        // 面板
        ctx.fill(pl, pt, pr, pb, 0xCC101010);
        ctx.fill(pl, pt,     pr, pt + 1, 0xFF555555);
        ctx.fill(pl, pb - 1, pr, pb,     0xFF555555);
        ctx.fill(pl,     pt, pl + 1, pb, 0xFF555555);
        ctx.fill(pr - 1, pt, pr,     pb, 0xFF555555);

        // 标题（用 getMatrix 直接画，不走可能被 mixin 的封装方法）
        net.minecraft.client.util.math.MatrixStack ms = ctx.getMatrices();
        ms.push();
        net.minecraft.client.render.VertexConsumerProvider.Immediate vcp =
                this.client.getBufferBuilders().getEntityVertexConsumers();

        String title = "AutoMe 设置";
        int titleW = this.textRenderer.getWidth(title);
        this.textRenderer.draw(title, cx - titleW / 2f, pt + 16,
                0xFFFFAA00, true, ms.peek().getPositionMatrix(),
                vcp, net.minecraft.client.font.TextRenderer.TextLayerType.NORMAL,
                0, 0xF000F0);

        String label = "聊天前缀：";
        this.textRenderer.draw(label, pl + 20f, pt + 84f,
                0xFFAAAAAA, false, ms.peek().getPositionMatrix(),
                vcp, net.minecraft.client.font.TextRenderer.TextLayerType.NORMAL,
                0, 0xF000F0);

        String hint = "示例: /me  /say  all(不转换)  123(不转换)";
        int hintW = this.textRenderer.getWidth(hint);
        this.textRenderer.draw(hint, cx - hintW / 2f, pt + 128f,
                0xFF888888, false, ms.peek().getPositionMatrix(),
                vcp, net.minecraft.client.font.TextRenderer.TextLayerType.NORMAL,
                0, 0xF000F0);

        vcp.draw();
        ms.pop();

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
