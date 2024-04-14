package com.sigmundgranaas.forgero.fabric.mixins;

import static net.minecraft.client.render.model.json.ModelTransformationMode.GROUND;

import com.sigmundgranaas.forgero.minecraft.common.client.model.QuadProviderPreparer;
import io.github.moremcmeta.emissiveplugin.fabric.model.OverlayBakedModel;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin(ItemRenderer.class)
public class EmissiveOverlayModelMixin {
	@Shadow
	@Final
	private ItemModels models;

	@Inject(at = @At("HEAD"), method = "getModel")
	public void forgero$preparOverlayModel(ItemStack stack, @Nullable World world, @Nullable LivingEntity entity, int seed, CallbackInfoReturnable<BakedModel> ci) {
		BakedModel bakedModel = this.models.getModel(stack);
		if (bakedModel instanceof OverlayBakedModel overlay) {
			if (overlay.getWrappedModel() instanceof QuadProviderPreparer preparer && world instanceof ClientWorld clientWorld) {
				preparer.provideContext(stack, clientWorld, entity, seed);
			}
		}
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V"), method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V")
	private void forgero$applyGroundTransformationToOverlayModel(ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
		if (renderMode.equals(GROUND) && model instanceof OverlayBakedModel overlayBakedModel && overlayBakedModel.getWrappedModel() instanceof QuadProviderPreparer) {
			// The ground model is scaled down by half to make sure it's the same size as other models
			// Investigate why it's doing this. This mixin should not be needed.
			matrices.scale(0.5f, 0.5f, 0.5f);
		}
	}
}
