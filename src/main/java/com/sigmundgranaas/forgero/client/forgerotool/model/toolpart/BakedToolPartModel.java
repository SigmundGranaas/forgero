package com.sigmundgranaas.forgero.client.forgerotool.model.toolpart;

import com.sigmundgranaas.forgero.client.forgerotool.model.implementation.ToolPartModelBuilderImpl;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BasicBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
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
        if (model instanceof BasicBakedModel basic) {
            var quads = basic.getQuads(null, null, null);
            boolean check = false;
            Renderer renderer = RendererAccess.INSTANCE.getRenderer();
            MeshBuilder builder = renderer.meshBuilder();
            QuadEmitter emitter = builder.getEmitter();
            for (Direction direction : Direction.values()) {
                int spriteIdx = direction == Direction.UP || direction == Direction.DOWN ? 1 : 0;
                // Add a new face to the mesh
                emitter.square(direction, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f);
                // Set the sprite of the face, must be called after .square()
                // We haven't specified any UV coordinates, so we want to use the whole texture. BAKE_LOCK_UV does exactly that.
                emitter.spriteBake(0, basic.getParticleSprite(), MutableQuadView.BAKE_LOCK_UV);
                // Enable texture usage
                emitter.spriteColor(0, -1, -1, -1, -1);
                // Add the quad to the mesh
                emitter.emit();
            }

            context.meshConsumer().accept(builder.build());
        }
    }
}
