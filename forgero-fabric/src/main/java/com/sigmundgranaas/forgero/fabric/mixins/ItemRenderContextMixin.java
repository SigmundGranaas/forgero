package com.sigmundgranaas.forgero.fabric.mixins;

import com.sigmundgranaas.forgero.minecraft.common.item.DynamicAttributeItem;

import net.fabricmc.fabric.impl.client.indigo.renderer.render.ItemRenderContext;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderContext.class)
public class ItemRenderContextMixin {

	@Shadow
	private MatrixStack matrixStack;

	@Shadow
	private ModelTransformation.Mode transformMode;

    @Inject(method = "renderModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack$Entry;getPositionMatrix()Lorg/joml/Matrix4f;"))
    public void renderModel(ItemStack itemStack, ModelTransformation.Mode transformMode, boolean invert, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int lightmap, int overlay, BakedModel model, ItemRenderContext.VanillaQuadHandler vanillaHandler, CallbackInfo ci) {
        if ((itemStack.getItem() instanceof DynamicAttributeItem) && this.transformMode == ModelTransformation.Mode.GROUND) {
            this.matrixStack.scale(0.5f, 0.5f, 0.5f);
            this.matrixStack.multiplyPositionMatrix(new Matrix4f().translate(0.5f, 0.5f, 0.5f));
        }
    }
}
