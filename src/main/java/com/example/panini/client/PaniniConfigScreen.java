package com.example.panini.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;

/**
 * In-game configuration screen for the Panini Projection mod.
 *
 * <p>Allows the player to adjust:
 * - Whether Panini is enabled
 * - Panini strength (0.0 - 1.0)
 * - Game FOV (30-170 degrees)
 * - Only apply at wide FOV + threshold
 */
public class PaniniConfigScreen extends Screen {

    private static final int WIDTH = 192;
    private static final int HEIGHT = 160;
    private static final int SLIDER_WIDTH = 150;
    private static final int SLIDER_HEIGHT = 20;
    private static final int BUTTON_WIDTH = 100;
    private static final int CHECKBOX_WIDTH = 130;

    private static final int LABEL_X = 12;
    private static final int LABEL_Y_START = 32;
    private static final int ROW_HEIGHT = 24;
    private static final int BOTTOM_MARGIN = 16;

    private final Screen parent;
    private final PaniniConfig config;
    private final Minecraft mc;

    // Widgets
    private Checkbox enableCheckbox;
    private PaniniSlider strengthSlider;
    private StringWidget strengthLabel;
    private PaniniSlider fovSlider;
    private StringWidget fovLabel;
    private Checkbox onlyWideFovCheckbox;
    private PaniniSlider fovThresholdSlider;
    private StringWidget thresholdLabel;

    public PaniniConfigScreen(Screen parent, PaniniConfig config) {
        super(Component.literal("Panini Projection Settings"));
        this.parent = parent;
        this.config = config;
        this.mc = Minecraft.getInstance();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = LABEL_Y_START;

        // Enable checkbox
        enableCheckbox = Checkbox.builder(
                Component.literal("Enabled"),
                this.mc.font)
            .pos(centerX - CHECKBOX_WIDTH / 2, y)
            .maxWidth(CHECKBOX_WIDTH)
            .selected(config.enabled)
            .onValueChange((cb, checked) -> {
                config.enabled = checked;
            })
            .build();
        this.addRenderableWidget(enableCheckbox);
        y += ROW_HEIGHT;

        // Panini strength label + slider
        strengthLabel = new StringWidget(
            centerX - SLIDER_WIDTH / 2, y,
            SLIDER_WIDTH, SLIDER_HEIGHT,
            formatStrengthLabel(), this.mc.font);
        this.addRenderableWidget(strengthLabel);
        y += ROW_HEIGHT;

        strengthSlider = new PaniniSlider(
            centerX - SLIDER_WIDTH / 2, y, SLIDER_WIDTH, SLIDER_HEIGHT,
            0.0f, 1.0f, config.paniniStrength, false, value -> {
                config.paniniStrength = value;
                strengthLabel.setMessage(formatStrengthLabel());
            });
        this.addRenderableWidget(strengthSlider);
        y += ROW_HEIGHT;

        // FOV label + slider
        fovLabel = new StringWidget(
            centerX - SLIDER_WIDTH / 2, y,
            SLIDER_WIDTH, SLIDER_HEIGHT,
            formatFovLabel(), this.mc.font);
        this.addRenderableWidget(fovLabel);
        y += ROW_HEIGHT;

        OptionInstance<Integer> fovOption = mc.options.fov();
        int currentFov = fovOption.get();
        fovSlider = new PaniniSlider(
            centerX - SLIDER_WIDTH / 2, y, SLIDER_WIDTH, SLIDER_HEIGHT,
            30.0f, 110.0f, (float) currentFov, true, value -> {
                fovOption.set(Math.round(value));
                fovLabel.setMessage(formatFovLabel());
            });
        this.addRenderableWidget(fovSlider);
        y += ROW_HEIGHT;

        // Only wide FOV checkbox
        onlyWideFovCheckbox = Checkbox.builder(
                Component.literal(formatWideFovLabel()),
                this.mc.font)
            .pos(centerX - CHECKBOX_WIDTH / 2, y)
            .maxWidth(CHECKBOX_WIDTH)
            .selected(config.onlyWideFov)
            .onValueChange((cb, checked) -> {
                config.onlyWideFov = checked;
                onlyWideFovCheckbox.setMessage(Component.literal(formatWideFovLabel()));
            })
            .build();
        this.addRenderableWidget(onlyWideFovCheckbox);
        y += ROW_HEIGHT;

        // FOV threshold label + slider
        thresholdLabel = new StringWidget(
            centerX - SLIDER_WIDTH / 2, y,
            SLIDER_WIDTH, SLIDER_HEIGHT,
            formatThresholdLabel(), this.mc.font);
        this.addRenderableWidget(thresholdLabel);
        y += ROW_HEIGHT;

        fovThresholdSlider = new PaniniSlider(
            centerX - SLIDER_WIDTH / 2, y, SLIDER_WIDTH, SLIDER_HEIGHT,
            30.0f, 110.0f, config.wideFovThreshold, true, value -> {
                config.wideFovThreshold = value;
                thresholdLabel.setMessage(formatThresholdLabel());
                onlyWideFovCheckbox.setMessage(Component.literal(formatWideFovLabel()));
            });
        this.addRenderableWidget(fovThresholdSlider);

        // Done button
        this.addRenderableWidget(Button.builder(
                Component.literal("Done"),
                btn -> onDone())
            .bounds(centerX - BUTTON_WIDTH / 2, this.height - BOTTOM_MARGIN * 2 - SLIDER_HEIGHT, BUTTON_WIDTH, SLIDER_HEIGHT)
            .build());
    }

    private Component formatStrengthLabel() {
        return Component.literal("Panini Strength: " + String.format("%.2f", config.paniniStrength));
    }

    private Component formatFovLabel() {
        return Component.literal("FOV: " + mc.options.fov().get());
    }

    private String formatWideFovLabel() {
        return "Only at Wide FOV (>=" + Math.round(config.wideFovThreshold) + ")";
    }

    private Component formatThresholdLabel() {
        return Component.literal("Wide FOV Threshold: " + Math.round(config.wideFovThreshold));
    }

    private void onDone() {
        config.save();
        mc.setScreenAndShow(parent);
    }

    @Override
    public void onClose() {
        config.save();
        mc.setScreenAndShow(parent);
    }
}
