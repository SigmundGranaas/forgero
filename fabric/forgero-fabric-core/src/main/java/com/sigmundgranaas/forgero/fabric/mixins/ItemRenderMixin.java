package com.sigmundgranaas.forgero.fabric.mixins;

import com.sigmundgranaas.forgero.minecraft.common.client.model.CompositeModelVariant;

import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * This is a stupid mixin. PLEASE REMOVE WHEN POSSIBLE
 */

@Mixin(ItemRenderer.class)
public abstract class ItemRenderMixin {


	@Shadow
	@Final
	private ItemModels models;

	@Inject(at = @At("HEAD"), method = "getModel", cancellable = true)
	public void getModelMixin(ItemStack stack, @Nullable World world, @Nullable LivingEntity entity, int seed, CallbackInfoReturnable<BakedModel> ci) {
		if (this.models.getModel(stack) instanceof CompositeModelVariant variant) {
			var model = variant.getModel(stack);
			ClientWorld clientWorld = world instanceof ClientWorld ? (ClientWorld) world : null;
			ci.setReturnValue(model.getOverrides().apply(model, stack, clientWorld, entity, seed));
		}
	}
}
