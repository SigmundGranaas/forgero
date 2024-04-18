package com.sigmundgranaas.forgero.fabric.mixins;

import com.sigmundgranaas.forgero.minecraft.common.client.impl.model.RenderContextManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
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
}
