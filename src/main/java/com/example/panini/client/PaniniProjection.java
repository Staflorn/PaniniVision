package com.example.panini.client;

import com.example.panini.mixin.AccessorPostChain;
import com.example.panini.mixin.AccessorPostPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.state.WindowRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages the Panini projection post-processing effect.
 *
 * <p>The Panini projection is applied as a post-processing chain (PostChain)
 * that reads from the main render target, applies the Panini distortion via
 * a fragment shader, and writes the result back. This runs after world
 * rendering but before hand/GUI rendering, so the hand and UI are not
 * distorted.
 *
 * <p>The PostChain is defined in {@code post_effect/panini.json} and uses
 * a custom fragment shader at {@code shaders/post/panini.fsh}. The shader
 * uses the full Panini projection formula (not the simplified matrix
 * approximation), reading from the perspective matrix to determine the
 * correct FOV and aspect ratio.
 */
public final class PaniniProjection {

    private static final Identifier POST_CHAIN_ID = Identifier.parse("panini_vision:panini");
    private static final String CONFIG_UNIFORM = "PaniniConfig";
    private static final AtomicBoolean WARNED_MISSING_CHAIN = new AtomicBoolean();
    private static final AtomicBoolean WARNED_MISSING_UNIFORM = new AtomicBoolean();

    private PaniniProjection() {
    }

    /**
     * Apply the Panini projection post-processing effect to the main render target.
     *
     * <p>This method should be called after the world has been rendered but
     * before the hand and GUI are rendered. It loads the Panini PostChain,
     * updates its uniforms with the current camera/projection state, and
     * executes the post-processing pass.
     *
     * @param minecraft  The Minecraft instance
     * @param mainTarget the main render target (already has world rendering)
     * @param allocator the resource allocator for the post chain
     * @param cameraState the current camera render state (contains projection matrix)
     * @param windowState the current window render state
     */
    public static void process(
        Minecraft minecraft,
        com.mojang.blaze3d.pipeline.RenderTarget mainTarget,
        com.mojang.blaze3d.resource.GraphicsResourceAllocator allocator,
        CameraRenderState cameraState,
        WindowRenderState windowState
    ) {
        if (!shouldApply(cameraState) || !hasValidWindow(windowState)) {
            return;
        }

        PostChain postChain = minecraft.getShaderManager().getPostChain(POST_CHAIN_ID, net.minecraft.client.renderer.LevelTargetBundle.MAIN_TARGETS);
        if (postChain == null) {
            if (WARNED_MISSING_CHAIN.compareAndSet(false, true)) {
                System.err.println("[PaniniProjection] Unable to apply Panini Projection: post chain " + POST_CHAIN_ID + " is unavailable");
            }
            return;
        }

        if (updateUniforms(postChain, cameraState)) {
            postChain.process(mainTarget, allocator);
        }
    }

    /**
     * Check whether the Panini projection should be applied for this frame.
     */
    private static boolean shouldApply(CameraRenderState cameraState) {
        PaniniConfig config = PaniniProjectionClient.getConfig();
        if (!config.enabled || config.paniniStrength <= 0.0f) {
            return false;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return false;
        }

        // Don't apply while scoping (e.g., spyglass)
        if (mc.player.isScoping()) {
            return false;
        }

        // Don't apply in panoramic mode
        if (cameraState == null || cameraState.isPanoramicMode) {
            return false;
        }

        // Don't apply if frustum is captured (e.g., in a GUI that captures the frustum)
        if (cameraState.isFrustumCaptured) {
            return false;
        }

        // Optionally only apply at wide FOV (the shader handles all FOVs correctly,
        // but users may want Panini only at high FOVs for performance or preference)
        if (config.onlyWideFov) {
            float fov = (float) Math.toDegrees(Math.atan(1.0f / Math.abs(cameraState.projectionMatrix.m11())) * 2.0);
            if (fov < config.wideFovThreshold) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if the window has valid dimensions for rendering.
     */
    private static boolean hasValidWindow(WindowRenderState windowState) {
        if (windowState == null) {
            return false;
        }
        return windowState.width > 0 && windowState.height > 0 && !windowState.isMinimized;
    }

    /**
     * Update the PaniniConfig uniform in the PostChain's passes.
     *
     * <p>The uniform is a vec4 containing:
     * - x: Panini strength (0-1)
     * - y: 1/|m00| (horizontal tangent: aspect * tan(fov/2))
     * - z: 1/|m11| (vertical tangent: tan(fov/2))
     * - w: padding
     *
     * @return true if the uniforms were successfully updated
     */
    private static boolean updateUniforms(PostChain postChain, CameraRenderState cameraState) {
        List<PostPass> passes = ((AccessorPostChain) postChain).getPasses();
        for (PostPass pass : passes) {
            Map<String, GpuBuffer> customUniforms = ((AccessorPostPass) pass).getCustomUniforms();
            GpuBuffer configBuffer = customUniforms.get(CONFIG_UNIFORM);
            if (configBuffer != null) {
                configBuffer = prepareConfigUniform(customUniforms, configBuffer);
                return writeConfigUniform(configBuffer, cameraState);
            }
        }

        if (WARNED_MISSING_UNIFORM.compareAndSet(false, true)) {
            System.err.println("[PaniniProjection] Unable to apply Panini Projection: uniform '" + CONFIG_UNIFORM + "' is unavailable");
        }
        return false;
    }

    /**
     * Ensure the config uniform buffer is valid and writable.
     * If the existing buffer is closed or lacks uniform usage, create a new one.
     */
    private static GpuBuffer prepareConfigUniform(Map<String, GpuBuffer> customUniforms, GpuBuffer existingBuffer) {
        if ((existingBuffer.usage() & 8) != 0 && !existingBuffer.isClosed()) {
            return existingBuffer;
        }

        // Create a new buffer with default values
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer data = createConfigBuffer(stack, 0.0f, 1.0f, 1.0f);
            GpuBuffer newBuffer = RenderSystem.getDevice().createBuffer(
                () -> "PaniniProjection config", 136, data
            );
            existingBuffer.close();
            customUniforms.put(CONFIG_UNIFORM, newBuffer);
            return newBuffer;
        }
    }

    /**
     * Write the Panini parameters to the uniform buffer.
     *
     * @return true if successful, false if the projection matrix is invalid
     */
    private static boolean writeConfigUniform(GpuBuffer buffer, CameraRenderState cameraState) {
        PaniniConfig config = PaniniProjectionClient.getConfig();
        float strength = config.paniniStrength;

        Matrix4f projectionMatrix = cameraState.projectionMatrix;
        float sourceExtentX = reciprocalMagnitude(projectionMatrix.m00());
        float sourceExtentY = reciprocalMagnitude(projectionMatrix.m11());

        // Sanity check: ensure we have valid projection values
        if (!Float.isFinite(sourceExtentX) || !Float.isFinite(sourceExtentY)) {
            return false;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer data = createConfigBuffer(stack, strength, sourceExtentX, sourceExtentY);
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(buffer.slice(), data);
        }

        return true;
    }

    private static float reciprocalMagnitude(float value) {
        return 1.0f / Math.abs(value);
    }

    /**
     * Create a std140 vec4 buffer with the given parameters.
     */
    private static ByteBuffer createConfigBuffer(MemoryStack stack, float strength, float sourceExtentX, float sourceExtentY) {
        return com.mojang.blaze3d.buffers.Std140Builder.onStack(stack, 16)
            .putVec4(strength, sourceExtentX, sourceExtentY, 0.0f)
            .get();
    }
}
