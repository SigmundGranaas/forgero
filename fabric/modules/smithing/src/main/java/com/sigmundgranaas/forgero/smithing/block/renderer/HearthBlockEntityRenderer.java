package com.sigmundgranaas.forgero.smithing.block.renderer;

import com.sigmundgranaas.forgero.smithing.block.entity.custom.HearthBlockEntity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public class HearthBlockEntityRenderer implements BlockEntityRenderer<HearthBlockEntity> {
    public HearthBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(HearthBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        ItemStack stack = entity.getStack(0);
        if (!stack.isEmpty()) {
            ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
            matrices.push();
            // Position the item above the block center
            matrices.translate(0.5, 1, 0.5);

            // Scale down a bit for aesthetics
            matrices.scale(1f, 1f, 1f);
            itemRenderer.renderItem(
                stack,
                net.minecraft.client.render.model.json.ModelTransformationMode.FIXED,
                light,
                overlay,
                matrices,
                vertexConsumers,
                entity.getWorld(),
                0
            );
            matrices.pop();
        }
    }
}
