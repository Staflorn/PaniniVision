package com.example.panini.client;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

/**
 * Custom slider widget for the Panini config screen.
 *
 * <p>Maps a normalized 0.0-1.0 slider value to an arbitrary float range
 * with optional integer snapping.
 */
public class PaniniSlider extends AbstractSliderButton {

    private final float minValue;
    private final float maxValue;
    private final boolean snapToInt;
    private final ValueCallback callback;

    /**
     * Callback invoked when the slider value changes.
     */
    @FunctionalInterface
    public interface ValueCallback {
        void onValueChanged(float value);
    }

    /**
     * Create a new slider.
     *
     * @param x        screen x position
     * @param y        screen y position
     * @param width    widget width
     * @param height   widget height
     * @param min      minimum value
     * @param max      maximum value
     * @param current  current value (clamped to [min, max])
     * @param snapToInt if true, the value is rounded to the nearest integer
     * @param callback called when the slider value changes
     */
    public PaniniSlider(int x, int y, int width, int height, float min, float max,
                        float current, boolean snapToInt, ValueCallback callback) {
        super(x, y, width, height, Component.literal(""),
              normalize(min, max, clamp(current, min, max)));
        this.minValue = min;
        this.maxValue = max;
        this.snapToInt = snapToInt;
        this.callback = callback;
        updateMessage();
    }

    private static double normalize(float min, float max, float value) {
        return (value - min) / (max - min);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    protected void applyValue() {
        float actual = actualValue();
        callback.onValueChanged(actual);
    }

    @Override
    protected void updateMessage() {
        float actual = actualValue();
        if (snapToInt) {
            this.message = Component.literal(String.valueOf(Math.round(actual)));
        } else {
            this.message = Component.literal(String.format("%.2f", actual));
        }
    }

    /**
     * Compute the actual (un-normalized) float value from the internal 0-1 slider value.
     */
    private float actualValue() {
        float v = minValue + (float) (this.value * (maxValue - minValue));
        if (snapToInt) {
            v = Math.round(v);
        }
        return v;
    }

    /**
     * Programmatically set the slider to the given actual value.
     */
    public void setActualValue(float value) {
        this.setValue(normalize(minValue, maxValue, clamp(value, minValue, maxValue)));
        updateMessage();
    }
}
