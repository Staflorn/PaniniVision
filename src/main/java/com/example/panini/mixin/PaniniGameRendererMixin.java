package com.example.panini.mixin;

import com.example.panini.client.PaniniProjection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.WindowRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.GameRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin into {@link GameRenderer} to apply the Panini projection
 * post-processing after the world is rendered but before the hand
 * and GUI are rendered.
 *
 * <p>The injection point is {@code @At("HEAD")} of {@code render3dHud()},
 * which is called from {@code renderLevel()} immediately after
 * {@code LevelRenderer.render()} completes. This means:
 * - The world has already been rendered to {@code mainRenderTarget}
 * - The Panini shader is applied to {@code mainRenderTarget}
 * - The hand and screen effects render on top, undistorted
 *
 * <p>This follows the same approach as Sodium Extra's Panini Projection:
 * a two-pass PostChain (Panini → swap target → blit → main) that
 * reads from the main render target and writes back to it via
 * an intermediate target.
 */
@Mixin(GameRenderer.class)
public abstract class PaniniGameRendererMixin {

    @Shadow
    private Minecraft minecraft;

    @Shadow
    private com.mojang.blaze3d.pipeline.RenderTarget mainRenderTarget;

    @Shadow
    private com.mojang.blaze3d.resource.CrossFrameResourcePool resourcePool;

    @Shadow
    private GameRenderState gameRenderState;

    @Inject(
        method = "render3dHud",
        at = @At("HEAD")
    )
    private void applyPaniniProjection(CallbackInfo ci) {
        // Only apply when the level is being rendered
        if (!this.gameRenderState.shouldRenderLevel) {
            return;
        }

        CameraRenderState cameraState = this.gameRenderState.levelRenderState.cameraRenderState;
        WindowRenderState windowState = this.gameRenderState.windowRenderState;

        PaniniProjection.process(
            this.minecraft,
            this.mainRenderTarget,
            this.resourcePool,
            cameraState,
            windowState
        );
    }
}
