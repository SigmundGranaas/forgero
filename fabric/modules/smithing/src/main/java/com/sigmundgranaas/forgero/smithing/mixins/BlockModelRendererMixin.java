// java
package com.sigmundgranaas.forgero.smithing.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.world.BlockRenderView;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@Mixin(BlockModelRenderer.class)
public class BlockModelRendererMixin {
	// Inject into the renderQuad overload with world/state/pos + 4 floats + 5 ints
	@Inject(
			method = "renderQuad(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/model/BakedQuad;FFFFIIIII)V",
			at = @At("HEAD")
	)
	private void injectCustomUVRotation(BlockRenderView world,
	                                    BlockState state,
	                                    BlockPos pos,
	                                    VertexConsumer vertexConsumer,
	                                    MatrixStack.Entry matrices,
	                                    BakedQuad quad,
	                                    float red,
	                                    float green,
	                                    float blue,
	                                    float alpha,
	                                    int i1,
	                                    int i2,
	                                    int i3,
	                                    int i4,
	                                    int i5,
	                                    CallbackInfo ci) {
		// TODO: Custom UV rotation or color adjustments
	}
}
