package com.example.panini.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuration for the Panini Projection mod.
 *
 * <p>Controls how the Panini projection is applied to the standard
 * Minecraft perspective camera.
 *
 * <p>Configuration is persisted to {@code config/panini_vision/config.json}
 * in the game directory and loaded on client startup.
 */
public class PaniniConfig {
    /**
     * Whether the Panini projection is enabled.
     */
    public boolean enabled = true;

    /**
     * Strength of the Panini effect.
     * 0.0 = disabled (pure perspective), 1.0 = full Panini.
     */
    public float paniniStrength = 0.5f;

    /**
     * If true, Panini is only applied when the FOV is above {@link #wideFovThreshold}.
     */
    public boolean onlyWideFov = false;

    /**
     * FOV (in degrees) above which Panini is applied when {@link #onlyWideFov} is true.
     */
    public float wideFovThreshold = 90.0f;

    /**
     * If true, applies a vertical scale correction to compensate for
     * the vertical FOV change introduced by the Panini projection.
     */
    public boolean verticalCorrection = false;

    // ---- Persistence ----

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Get the config file path.
     */
    public static Path getConfigPath() {
        return Path.of("config", "panini_vision", "config.json");
    }

    /**
     * Load the configuration from the config file. If the file does not exist
     * or is invalid, a default config is used.
     */
    public static PaniniConfig load() {
        Path path = getConfigPath();
        if (Files.exists(path)) {
            try {
                String json = Files.readString(path, StandardCharsets.UTF_8);
                PaniniConfig loaded = GSON.fromJson(json, PaniniConfig.class);
                if (loaded != null) {
                    return loaded;
                }
            } catch (IOException | JsonSyntaxException e) {
                System.err.println("[PaniniProjection] Failed to load config: " + e.getMessage());
            }
        }
        return new PaniniConfig();
    }

    /**
     * Save the configuration to the config file.
     */
    public void save() {
        Path path = getConfigPath();
        try {
            Files.createDirectories(path.getParent());
            String json = GSON.toJson(this);
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                writer.write(json);
            }
            System.out.println("[PaniniProjection] Config saved to " + path);
        } catch (IOException e) {
            System.err.println("[PaniniProjection] Failed to save config: " + e.getMessage());
        }
    }
}
