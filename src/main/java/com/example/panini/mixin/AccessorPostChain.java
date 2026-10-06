package com.example.panini.mixin;

import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/**
 * Accessor mixin for {@link PostChain} to get the private passes list.
 * Needed to update the PaniniConfig uniform buffer at runtime.
 */
@Mixin(PostChain.class)
public interface AccessorPostChain {
    /**
     * @return The list of PostPass objects in this PostChain.
     */
    @Accessor("passes")
    List<PostPass> getPasses();
}
