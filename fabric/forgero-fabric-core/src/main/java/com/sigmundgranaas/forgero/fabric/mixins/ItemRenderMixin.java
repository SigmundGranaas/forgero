package com.sigmundgranaas.forgero.fabric.mixins;

import static net.minecraft.client.render.model.json.ModelTransformationMode.GROUND;

import com.sigmundgranaas.forgero.minecraft.common.client.impl.model.RenderContextManager;
import com.sigmundgranaas.forgero.minecraft.common.client.model.QuadProviderPreparer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin(ItemRenderer.class)
public abstract class ItemRenderMixin {
	@Inject(method = "getModel", at = @At("HEAD"))
	private void injectRenderContext(ItemStack stack, World world, LivingEntity entity, int seed, CallbackInfoReturnable<BakedModel> cir) {
		if (world instanceof ClientWorld clientWorld) {
			RenderContextManager.setContext(stack, clientWorld, entity, seed);
		}
	}

	@Inject(method = "renderBakedItemModel", at = @At("RETURN"))
	private void clearRenderContext(BakedModel model, ItemStack stack, int light, int overlay, MatrixStack matrices, VertexConsumer vertices, CallbackInfo ci) {
		RenderContextManager.clearContext();
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V"), method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V")
	private void forgero$applyGroundTransformation(ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
		if (model instanceof QuadProviderPreparer && renderMode.equals(GROUND)) {
			// The ground model is scaled down by half to make sure it's the same size as other models
			// Investigate why it's doing this. This mixin should not be needed.
			matrices.scale(0.5f, 0.5f, 0.5f);
		}
	}
}
