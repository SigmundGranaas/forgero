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

    @Inject(method = "renderBakedItemModel", at = @At("HEAD"), cancellable = true)
    public void renderBakedItemModel(BakedModel model, ItemStack stack, int light, int overlay, MatrixStack matrices, VertexConsumer vertices, CallbackInfo ci) {
        if ((stack.getItem() instanceof ForgeroToolItem || stack.getItem() instanceof ToolPartItem)) {

            VertexConsumerProvider.Immediate consumer = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
            RenderLayer renderLayer = RenderLayers.getItemLayer(stack, true);


            VertexConsumer vertexConsumer;
            if (stack.getItem() instanceof ForgeroToolItem) {
                boolean glintModelTool = model.getSprite().getId().getPath().contains("handle") && !model.getSprite().getId().getPath().contains("secondary") && !model.getSprite().getId().getPath().contains("gem") && stack.hasGlint();
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

            ci.cancel();
        }
    }


    @Shadow
    protected abstract void renderBakedItemQuads(MatrixStack matrices, VertexConsumer vertices, List<BakedQuad> quads, ItemStack stack, int light, int overlay);
}
