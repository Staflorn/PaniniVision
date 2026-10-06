package com.example.panini.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;

/**
 * Panini Projection Mod — client entrypoint.
 *
 * <p>Panini Projection is a mathematical projection that reduces the distortion
 * at the edges of wide-FOV views, producing a more natural-looking image.
 * It preserves vertical lines and compresses horizontal edges, making it
 * especially useful for ultra-wide FOV settings.
 *
 * <p>The projection is applied as a post-processing shader pass that runs
 * after the world is rendered but before the hand and GUI are rendered.
 * The shader uses the full Panini projection formula, reading the FOV and
 * aspect ratio from the projection matrix to compute the correct mapping.
 *
 * <p>The post-processing is implemented via a Minecraft PostChain (defined
 * in {@code assets/panini_vision/post_effect/panini.json}) that uses
 * a custom fragment shader ({@code assets/panini_vision/shaders/post/panini.fsh}).
 * The PostChain is applied from {@link com.example.panini.mixin.PaniniGameRendererMixin},
 * which injects at the HEAD of {@code GameRenderer.render3dHud()}.
 *
 * <p>The actual Panini math is handled by {@link PaniniProjection}, which
 * loads the PostChain, updates shader uniforms based on the current camera
 * state, and executes the post-processing pass.
 */
public class PaniniProjectionClient implements ClientModInitializer {

    private static PaniniConfig config;

    @Override
    public void onInitializeClient() {
        // Load persisted config (or create default if not present)
        config = PaniniConfig.load();

        // Register the in-game key binding (P by default) to open the config screen
        PaniniKeyBinding.register();
    }

    /**
     * Get the current config singleton.
     */
    public static PaniniConfig getConfig() {
        return config;
    }

    /**
     * Update the config from the in-game config screen and persist it.
     */
    public static void setConfig(PaniniConfig newConfig) {
        config = newConfig;
        config.save();
    }
}
