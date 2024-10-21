package com.sigmundgranaas.forgero.fabric.mixins;

import com.sigmundgranaas.forgero.minecraft.common.client.impl.model.RenderContextManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(DrawContext.class)
public class RenderContextItemRenderMixin {

	@Inject(method = "drawItem*", at = @At("HEAD"))
		private void forgero$injectRenderContext(@Nullable LivingEntity entity, @Nullable World world, ItemStack stack, int x, int y, int seed, int z, CallbackInfo ci) {
			if (world instanceof ClientWorld clientWorld) {
				RenderContextManager.setContext(stack, clientWorld, entity, ModelTransformationMode.GUI, seed);
			}
		}

	@Inject(method = "drawItem*", at = @At("RETURN"))
	private void forgero$clearRenderContext(@Nullable LivingEntity entity, @Nullable World world, ItemStack stack, int x, int y, int seed, int z, CallbackInfo ci) {
		if (world instanceof ClientWorld clientWorld) {
			RenderContextManager.clearContext();
		}
	}
}
