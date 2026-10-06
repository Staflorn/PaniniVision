package com.example.panini.mixin;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;

/**
 * No-op mixin placeholder.
 *
 * <p>The Panini projection is now handled entirely via a post-processing
 * shader (see {@link com.example.panini.client.PaniniProjection}).
 * This mixin is kept for structural compatibility but no longer
 * modifies the projection matrix. The world is rendered with the
 * standard perspective projection, and the Panini distortion is
 * applied in a fragment shader after rendering.
 *
 * <p>This approach is superior to the matrix-based method because:
 * - The shader uses abs(x) for symmetric compression (matrix m03 modification is asymmetric)
 * - The hand and GUI are rendered on top, undistorted
 * - The full Panini formula (not the simplified matrix approximation) is used
 */
@Mixin(Camera.class)
public abstract class PaniniCameraMixin {
    // Intentionally empty — Panini is handled by the post-processing shader.
}
