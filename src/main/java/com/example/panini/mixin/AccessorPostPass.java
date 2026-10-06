package com.example.panini.mixin;

import net.minecraft.client.renderer.PostPass;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * Accessor mixin for {@link PostPass} to get the custom uniforms map.
 * Needed to update the PaniniConfig uniform buffer at runtime.
 */
@Mixin(PostPass.class)
public interface AccessorPostPass {
    /**
     * @return The map of custom uniform name → GpuBuffer.
     */
    @Accessor("customUniforms")
    Map<String, GpuBuffer> getCustomUniforms();
}
