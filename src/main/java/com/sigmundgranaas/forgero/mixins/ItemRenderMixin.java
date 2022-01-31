package com.sigmundgranaas.forgero.mixins;

import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.ToolPartItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Random;

/**
 * This is a stupid mixin. PLEASE REMOVE WHEN POSSIBLE
 */

@Mixin(ItemRenderer.class)
public abstract class ItemRenderMixin {
    //TODO Remove this garbage
    //Apparently, items are rendered as duplicates seven times. I have no idea why this happens, but it really messes with the enchanting overlay.
    private int numberOfDuplicates = 1;

    @Inject(method = "renderBakedItemModel", at = @At("HEAD"), cancellable = true)
    public void renderBakedItemModel(BakedModel model, ItemStack stack, int light, int overlay, MatrixStack matrices, VertexConsumer vertices, CallbackInfo ci) {
        if ((stack.getItem() instanceof ForgeroToolItem || stack.getItem() instanceof ToolPartItem) && numberOfDuplicates == 7) {
            //
            //model.getTransformation().getTransformation(ModelTransformation.Mode.GUI).apply(false, matrices);
            //matrices.translate(+0.2, +0.2, 0.2);
            //matrices.push();
            //matrices.pop();

            VertexConsumerProvider.Immediate consumer = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
            RenderLayer renderLayer = RenderLayers.getItemLayer(stack, true);
            //

            VertexConsumer vertexConsumer;
            if (stack.getItem() instanceof ForgeroToolItem) {
                boolean glintModelTool = model.getParticleSprite().getId().getPath().contains("handle") && !model.getParticleSprite().getId().getPath().contains("secondary") && stack.hasGlint();
                vertexConsumer = ItemRenderer.getItemGlintConsumer(consumer, renderLayer, true, glintModelTool);
            } else {
                vertexConsumer = ItemRenderer.getItemGlintConsumer(consumer, renderLayer, true, stack.hasGlint());
            }

            Random random = new Random();
            long l = 42L;
            for (Direction direction : Direction.values()) {
                random.setSeed(l);
                this.renderBakedItemQuads(matrices, vertexConsumer, model.getQuads(null, direction, random), stack, light, overlay);
            }
            random.setSeed(l);
            this.renderBakedItemQuads(matrices, vertexConsumer, model.getQuads(null, null, random), stack, light, overlay);
            numberOfDuplicates = 1;
            //matrices.pop();
            ci.cancel();
        } else if (stack.getItem() instanceof ForgeroToolItem && stack.hasGlint()) {
            numberOfDuplicates++;
            ci.cancel();
        }
    }


    @Shadow
    protected abstract void renderBakedItemQuads(MatrixStack matrices, VertexConsumer vertices, List<BakedQuad> quads, ItemStack stack, int light, int overlay);
}
