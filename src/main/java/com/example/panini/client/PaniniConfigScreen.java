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
 * - Vertical compression
 * - Camera zoom
 */
public class PaniniConfigScreen extends Screen {

    private static final int SLIDER_WIDTH = 150;
    private static final int SLIDER_HEIGHT = 20;
    private static final int BUTTON_WIDTH = 100;
    private static final int TAB_BUTTON_WIDTH = 76;
    private static final int TAB_BUTTON_GAP = 4;
    private static final int PRESET_BUTTON_WIDTH = 60;
    private static final int PRESET_BUTTON_GAP = 6;
    private static final int CHECKBOX_WIDTH = 130;

    private static final int LABEL_Y_START = 36;
    private static final int ROW_HEIGHT = 24;
    private static final int BOTTOM_MARGIN = 16;

    private final Screen parent;
    private final PaniniConfig config;
    private final Minecraft mc;

    // Widgets
    private Checkbox enableCheckbox;
    private PaniniSlider strengthSlider;
    private StringWidget strengthLabel;
    private PaniniSlider verticalCompressionSlider;
    private StringWidget verticalCompressionLabel;
    private PaniniSlider cameraZoomSlider;
    private StringWidget cameraZoomLabel;
    private PaniniSlider fovSlider;
    private StringWidget fovLabel;
    private Checkbox viewmodelCompensationCheckbox;
    private PaniniSlider viewmodelScaleSlider;
    private StringWidget viewmodelScaleLabel;
    private PaniniSlider viewmodelOffsetXSlider;
    private StringWidget viewmodelOffsetXLabel;
    private PaniniSlider viewmodelOffsetYSlider;
    private StringWidget viewmodelOffsetYLabel;
    private boolean viewmodelTab;

    public PaniniConfigScreen(Screen parent, PaniniConfig config) {
        super(Component.literal("Panini Projection Settings"));
        this.parent = parent;
        this.config = config;
        this.mc = Minecraft.getInstance();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        addTabButtons(centerX);

        if (viewmodelTab) {
            initViewmodelTab(centerX);
        } else {
            initCameraTab(centerX);
        }

        this.addRenderableWidget(Button.builder(
                Component.literal("Done"),
                btn -> onDone())
            .bounds(centerX - BUTTON_WIDTH / 2, this.height - BOTTOM_MARGIN * 2 - SLIDER_HEIGHT, BUTTON_WIDTH, SLIDER_HEIGHT)
            .build());
    }

    private void addTabButtons(int centerX) {
        int tabWidth = TAB_BUTTON_WIDTH * 2 + TAB_BUTTON_GAP;
        int tabX = centerX - tabWidth / 2;
        this.addRenderableWidget(Button.builder(
                Component.literal("Camera"),
                btn -> switchTab(false))
            .bounds(tabX, 8, TAB_BUTTON_WIDTH, SLIDER_HEIGHT)
            .build());
        this.addRenderableWidget(Button.builder(
                Component.literal("Viewmodel"),
                btn -> switchTab(true))
            .bounds(tabX + TAB_BUTTON_WIDTH + TAB_BUTTON_GAP, 8, TAB_BUTTON_WIDTH, SLIDER_HEIGHT)
            .build());
    }

    private void switchTab(boolean showViewmodelTab) {
        if (viewmodelTab != showViewmodelTab) {
            viewmodelTab = showViewmodelTab;
            rebuildWidgets();
        }
    }

    private void initCameraTab(int centerX) {
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

        // Vertical compression label + slider
        verticalCompressionLabel = new StringWidget(
            centerX - SLIDER_WIDTH / 2, y,
            SLIDER_WIDTH, SLIDER_HEIGHT,
            formatVerticalCompressionLabel(), this.mc.font);
        this.addRenderableWidget(verticalCompressionLabel);
        y += ROW_HEIGHT;

        verticalCompressionSlider = new PaniniSlider(
            centerX - SLIDER_WIDTH / 2, y, SLIDER_WIDTH, SLIDER_HEIGHT,
            0.0f, 1.0f, config.verticalCompression, false, value -> {
                config.verticalCompression = value;
                verticalCompressionLabel.setMessage(formatVerticalCompressionLabel());
            });
        this.addRenderableWidget(verticalCompressionSlider);
        y += ROW_HEIGHT;

        // Camera zoom label + slider
        cameraZoomLabel = new StringWidget(
            centerX - SLIDER_WIDTH / 2, y,
            SLIDER_WIDTH, SLIDER_HEIGHT,
            formatCameraZoomLabel(), this.mc.font);
        this.addRenderableWidget(cameraZoomLabel);
        y += ROW_HEIGHT;

        cameraZoomSlider = new PaniniSlider(
            centerX - SLIDER_WIDTH / 2, y, SLIDER_WIDTH, SLIDER_HEIGHT,
            0.5f, 1.0f, config.cameraZoom, false, value -> {
                config.cameraZoom = value;
                cameraZoomLabel.setMessage(formatCameraZoomLabel());
            });
        this.addRenderableWidget(cameraZoomSlider);
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

        int presetWidth = PRESET_BUTTON_WIDTH * 3 + PRESET_BUTTON_GAP * 2;
        int presetX = centerX - presetWidth / 2;
        this.addRenderableWidget(createPresetButton("Natural", presetX, y, () ->
            applyPreset(105, 0.20f, 0.06f, 0.96f)));
        presetX += PRESET_BUTTON_WIDTH + PRESET_BUTTON_GAP;
        this.addRenderableWidget(createPresetButton("Wide", presetX, y, () ->
            applyPreset(110, 0.25f, 0.06f, 0.98f)));
        presetX += PRESET_BUTTON_WIDTH + PRESET_BUTTON_GAP;
        this.addRenderableWidget(createPresetButton("GoPro", presetX, y, () ->
            applyPreset(110, 0.40f, 0.06f, 1.0f)));
    }

    private void initViewmodelTab(int centerX) {
        int y = LABEL_Y_START;

        viewmodelCompensationCheckbox = Checkbox.builder(
                Component.literal("Viewmodel Compensation"),
                this.mc.font)
            .pos(centerX - CHECKBOX_WIDTH / 2, y)
            .maxWidth(CHECKBOX_WIDTH)
            .selected(config.viewmodelCompensationEnabled)
            .onValueChange((cb, checked) -> config.viewmodelCompensationEnabled = checked)
            .build();
        this.addRenderableWidget(viewmodelCompensationCheckbox);
        y += ROW_HEIGHT;

        viewmodelScaleLabel = new StringWidget(
            centerX - SLIDER_WIDTH / 2, y,
            SLIDER_WIDTH, SLIDER_HEIGHT,
            formatViewmodelScaleLabel(), this.mc.font);
        this.addRenderableWidget(viewmodelScaleLabel);
        y += ROW_HEIGHT;

        viewmodelScaleSlider = new PaniniSlider(
            centerX - SLIDER_WIDTH / 2, y, SLIDER_WIDTH, SLIDER_HEIGHT,
            0.5f, 1.5f, config.viewmodelScale, false, value -> {
                config.viewmodelScale = value;
                viewmodelScaleLabel.setMessage(formatViewmodelScaleLabel());
            });
        this.addRenderableWidget(viewmodelScaleSlider);
        y += ROW_HEIGHT;

        viewmodelOffsetXLabel = new StringWidget(
            centerX - SLIDER_WIDTH / 2, y,
            SLIDER_WIDTH, SLIDER_HEIGHT,
            formatViewmodelOffsetXLabel(), this.mc.font);
        this.addRenderableWidget(viewmodelOffsetXLabel);
        y += ROW_HEIGHT;

        viewmodelOffsetXSlider = new PaniniSlider(
            centerX - SLIDER_WIDTH / 2, y, SLIDER_WIDTH, SLIDER_HEIGHT,
            -0.5f, 0.5f, config.viewmodelOffsetX, false, value -> {
                config.viewmodelOffsetX = value;
                viewmodelOffsetXLabel.setMessage(formatViewmodelOffsetXLabel());
            });
        this.addRenderableWidget(viewmodelOffsetXSlider);
        y += ROW_HEIGHT;

        viewmodelOffsetYLabel = new StringWidget(
            centerX - SLIDER_WIDTH / 2, y,
            SLIDER_WIDTH, SLIDER_HEIGHT,
            formatViewmodelOffsetYLabel(), this.mc.font);
        this.addRenderableWidget(viewmodelOffsetYLabel);
        y += ROW_HEIGHT;

        viewmodelOffsetYSlider = new PaniniSlider(
            centerX - SLIDER_WIDTH / 2, y, SLIDER_WIDTH, SLIDER_HEIGHT,
            -0.5f, 0.5f, config.viewmodelOffsetY, false, value -> {
                config.viewmodelOffsetY = value;
                viewmodelOffsetYLabel.setMessage(formatViewmodelOffsetYLabel());
            });
        this.addRenderableWidget(viewmodelOffsetYSlider);
    }

    private Button createPresetButton(String name, int x, int y, Runnable action) {
        return Button.builder(Component.literal(name), button -> action.run())
            .bounds(x, y, PRESET_BUTTON_WIDTH, SLIDER_HEIGHT)
            .build();
    }

    private void applyPreset(int fov, float strength, float verticalCompression, float cameraZoom) {
        config.paniniStrength = strength;
        config.verticalCompression = verticalCompression;
        config.cameraZoom = cameraZoom;
        mc.options.fov().set(fov);

        strengthSlider.setActualValue(strength);
        verticalCompressionSlider.setActualValue(verticalCompression);
        cameraZoomSlider.setActualValue(cameraZoom);
        fovSlider.setActualValue(fov);

        strengthLabel.setMessage(formatStrengthLabel());
        verticalCompressionLabel.setMessage(formatVerticalCompressionLabel());
        cameraZoomLabel.setMessage(formatCameraZoomLabel());
        fovLabel.setMessage(formatFovLabel());
    }

    private Component formatStrengthLabel() {
        return Component.literal("Panini Strength: " + String.format("%.2f", config.paniniStrength));
    }

    private Component formatFovLabel() {
        return Component.literal("FOV: " + mc.options.fov().get());
    }

    private Component formatVerticalCompressionLabel() {
        return Component.literal("Vertical Compression: " + String.format("%.2f", config.verticalCompression));
    }

    private Component formatCameraZoomLabel() {
        return Component.literal("Camera Zoom: " + Math.round(config.cameraZoom * 100.0f) + "%");
    }

    private Component formatViewmodelScaleLabel() {
        return Component.literal("Viewmodel Scale: " + String.format("%.2f", config.viewmodelScale));
    }

    private Component formatViewmodelOffsetXLabel() {
        return Component.literal("Viewmodel Offset X: " + String.format("%.2f", config.viewmodelOffsetX));
    }

    private Component formatViewmodelOffsetYLabel() {
        return Component.literal("Viewmodel Offset Y: " + String.format("%.2f", config.viewmodelOffsetY));
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
