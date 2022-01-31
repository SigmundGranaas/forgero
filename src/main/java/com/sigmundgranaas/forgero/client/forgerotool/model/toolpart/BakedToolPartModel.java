package com.sigmundgranaas.forgero.client.forgerotool.model.toolpart;

import com.sigmundgranaas.forgero.client.forgerotool.model.implementation.ToolPartModelBuilderImpl;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import java.util.Random;
import java.util.function.Supplier;

public class BakedToolPartModel implements FabricBakedModel {
    private final FabricBakedModel model;
    private final FabricBakedModel secondaryMaterial;
    private final FabricBakedModel gem;

    public BakedToolPartModel(ToolPartModelBuilderImpl toolPartModelBuilder) {
        this.model = toolPartModelBuilder.getToolPartModel();
        this.secondaryMaterial = toolPartModelBuilder.getSecondaryMaterial();
        this.gem = toolPartModelBuilder.getGem();
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
        this.model.emitItemQuads(null, null, context);
        this.secondaryMaterial.emitItemQuads(null, null, context);
        this.gem.emitItemQuads(null, null, context);
    }
}
