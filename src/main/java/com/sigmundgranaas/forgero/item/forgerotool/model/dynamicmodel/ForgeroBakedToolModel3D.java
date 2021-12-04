package com.sigmundgranaas.forgero.item.forgerotool.model.dynamicmodel;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.function.Supplier;

public record ForgeroBakedToolModel3D(@NotNull FabricBakedModel head,
                                      @NotNull FabricBakedModel handle,
                                      @Nullable FabricBakedModel binding) implements FabricBakedModel {
    public ForgeroBakedToolModel3D(@NotNull FabricBakedModel head, @NotNull FabricBakedModel handle, @Nullable FabricBakedModel binding) {
        this.head = head;
        this.handle = handle;
        this.binding = binding;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {

    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        if (binding != null) {
            binding.emitItemQuads(null, null, context);
        }
        head.emitItemQuads(null, null, context);
        handle.emitItemQuads(null, null, context);
    }
}
