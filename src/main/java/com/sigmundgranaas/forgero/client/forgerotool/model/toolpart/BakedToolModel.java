package com.sigmundgranaas.forgero.client.forgerotool.model.toolpart;

import com.sigmundgranaas.forgero.client.forgerotool.model.ToolModelBuilder;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.AbstractRandom;
import net.minecraft.world.BlockRenderView;

import java.util.function.Supplier;

public class BakedToolModel implements FabricBakedModel {
    private final FabricBakedModel head;
    private final FabricBakedModel handle;
    private final FabricBakedModel binding;

    public BakedToolModel(ToolModelBuilder toolModelBuilder) {
        this.head = toolModelBuilder.getHeadModel();
        this.handle = toolModelBuilder.getHandleModel();
        this.binding = toolModelBuilder.getBindingModel();
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<AbstractRandom> randomSupplier, RenderContext context) {

    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<AbstractRandom> randomSupplier, RenderContext context) {
        this.handle.emitItemQuads(null, null, context);
        this.head.emitItemQuads(null, null, context);
        this.binding.emitItemQuads(null, null, context);
    }
}
