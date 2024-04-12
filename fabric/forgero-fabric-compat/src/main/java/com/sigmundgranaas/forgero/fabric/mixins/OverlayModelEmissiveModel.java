package com.sigmundgranaas.forgero.fabric.mixins;

import com.sigmundgranaas.forgero.minecraft.common.client.model.QuadProviderPreparer;
import io.github.moremcmeta.emissiveplugin.fabric.model.OverlayBakedModel;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin(ItemRenderer.class)
public class OverlayModelEmissiveModel {
	@Shadow
	@Final
	private ItemModels models;

	@Inject(at = @At("HEAD"), method = "getModel")
	public void forgero$preparOverlayModel(ItemStack stack, @Nullable World world, @Nullable LivingEntity entity, int seed, CallbackInfoReturnable<BakedModel> ci) {
		BakedModel bakedModel = this.models.getModel(stack);
		if (bakedModel instanceof OverlayBakedModel overlay) {
			if (overlay.getWrappedModel() instanceof QuadProviderPreparer preparer && world instanceof ClientWorld clientWorld) {
				preparer.provideContext(stack, clientWorld, entity);
			}
		}
	}
}
