package com.example.panini.mixin;

import com.example.panini.client.PaniniConfig;
import com.example.panini.client.PaniniProjectionClient;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class PaniniFirstPersonHandsMixin {

    @Unique
    private boolean paniniVision$viewmodelTransformApplied;

    @Inject(method = "renderPlayerHand", at = @At("HEAD"))
    private void applyHandCompensation(
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        int lightCoords,
        HumanoidArm arm,
        PlayerRenderState playerRenderState,
        CallbackInfo ci
    ) {
        if (!paniniVision$isCompensationEnabled()) {
            return;
        }

        poseStack.pushPose();
        paniniVision$applyTransform(poseStack);
        paniniVision$viewmodelTransformApplied = true;
    }

    @Inject(method = "renderPlayerHand", at = @At("RETURN"))
    private void restoreHandCompensation(
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        int lightCoords,
        HumanoidArm arm,
        PlayerRenderState playerRenderState,
        CallbackInfo ci
    ) {
        if (paniniVision$viewmodelTransformApplied) {
            poseStack.popPose();
            paniniVision$viewmodelTransformApplied = false;
        }
    }

    @Redirect(
        method = "submitArmWithItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"
        )
    )
    private void submitCompensatedItem(
        ItemStackRenderState itemRenderState,
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        int lightCoords,
        int overlayCoords,
        int layer
    ) {
        if (!paniniVision$isCompensationEnabled()) {
            itemRenderState.submit(poseStack, submitNodeCollector, lightCoords, overlayCoords, layer);
            return;
        }

        poseStack.pushPose();
        paniniVision$applyTransform(poseStack);
        itemRenderState.submit(poseStack, submitNodeCollector, lightCoords, overlayCoords, layer);
        poseStack.popPose();
    }

    @Unique
    private static boolean paniniVision$isCompensationEnabled() {
        PaniniConfig config = PaniniProjectionClient.getConfig();
        return config != null && config.viewmodelCompensationEnabled;
    }

    @Unique
    private static void paniniVision$applyTransform(PoseStack poseStack) {
        PaniniConfig config = PaniniProjectionClient.getConfig();
        poseStack.translate(config.viewmodelOffsetX, config.viewmodelOffsetY, 0.0f);
        poseStack.scale(config.viewmodelScale, config.viewmodelScale, config.viewmodelScale);
    }
}
