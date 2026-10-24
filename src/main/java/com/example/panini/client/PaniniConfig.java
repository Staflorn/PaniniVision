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
     * Vertical compression of the generalized Panini projection.
     * 0.0 keeps the basic Panini vertical mapping; 1.0 fully straightens
     * horizontal lines.
     */
    public float verticalCompression = 0.06f;

    /**
     * Scale of the projected image.
     * 1.0 preserves the game's full FOV; lower values crop the edge of the
     * rendered view to create a less extreme camera angle.
     */
    public float cameraZoom = 0.7f;

    /**
     * Whether the first-person hand and item compensation is enabled.
     */
    public boolean viewmodelCompensationEnabled = false;

    /**
     * Uniform scale applied to first-person hands and items.
     */
    public float viewmodelScale = 0.92f;

    /**
     * Horizontal offset applied to first-person hands and items.
     */
    public float viewmodelOffsetX = 0.0f;

    /**
     * Vertical offset applied to first-person hands and items.
     */
    public float viewmodelOffsetY = 0.0f;

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
