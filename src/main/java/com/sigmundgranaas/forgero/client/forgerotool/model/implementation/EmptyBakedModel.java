package com.sigmundgranaas.forgero.client.forgerotool.model.implementation;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.AbstractRandom;
import net.minecraft.world.BlockRenderView;

import java.util.function.Supplier;


public class EmptyBakedModel implements FabricBakedModel {

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<AbstractRandom> randomSupplier, RenderContext context) {

    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<AbstractRandom> randomSupplier, RenderContext context) {

    }
}

