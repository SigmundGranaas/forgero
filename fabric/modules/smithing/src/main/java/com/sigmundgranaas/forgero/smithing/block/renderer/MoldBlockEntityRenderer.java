package com.sigmundgranaas.forgero.smithing.block.renderer;

import com.sigmundgranaas.forgero.smithing.block.custom.MoldBlock;
import com.sigmundgranaas.forgero.smithing.block.entity.MoldBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;


public class MoldBlockEntityRenderer implements BlockEntityRenderer<MoldBlockEntity> {
    public MoldBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(MoldBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        BlockState state = entity.getCachedState();
        if (state.get(MoldBlock.FILLED) && state.get(MoldBlock.PROGRESS) >= 75) {
            ItemStack result = entity.getResult();
            if (!result.isEmpty()) {
                // Get the model for the result item
                BakedModel model = MinecraftClient.getInstance().getItemRenderer().getModel(result, null, null, 0);
                // Render the item on top of the mold
                matrices.push();
                matrices.translate(0.5, 1.01, 0.5);
                matrices.scale(0.75f, 0.75f, 0.75f);
                matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(90));
                MinecraftClient.getInstance().getItemRenderer().renderItem(result, ModelTransformationMode.FIXED, light, overlay, matrices, vertexConsumers, null, 0);
                matrices.pop();
            }
        }
    }
}
