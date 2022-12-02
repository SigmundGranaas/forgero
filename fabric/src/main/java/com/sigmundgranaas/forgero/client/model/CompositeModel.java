package com.sigmundgranaas.forgero.client.model;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("ClassCanBeRecord")
public class CompositeModel implements FabricBakedModel {
    private final List<FabricBakedModel> models;

    public CompositeModel(List<FabricBakedModel> models) {
        this.models = models;
    }

    public static CompositeModel of(List<FabricBakedModel> models) {
        return new CompositeModel(models);
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
        models.forEach(model -> model.emitItemQuads(null, null, context));
    }
}
