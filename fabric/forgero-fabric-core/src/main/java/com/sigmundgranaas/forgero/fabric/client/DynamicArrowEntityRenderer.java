package com.sigmundgranaas.forgero.fabric.client;


import com.sigmundgranaas.forgero.minecraft.common.item.DynamicArrowEntity;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * This class, DynamicArrowEntityRenderer, is responsible for the rendering of DynamicArrowEntity.
 * Originally adapted from SpriteArrowRenderer
 * <p>
 * It is worth noting that the original version of this class was designed and implemented by the GitHub user 'agnor99' in their
 * 'arrow-sprites' mod, which can be found at: <a href="https://modrinth.com/mod/sprite-arrows">...</a>.
 * <p>
 * The original implementation can be accessed in 'agnor99's' GitHub repository at:
 * <a href="https://github.com/SigmundGranaas/arrow-sprites/blob/1.20.x/common/src/main/java/dev/agnor/spritearrows/SpriteArrowRenderer.java">SpriteArrowRenderer</a>.
 * <p>
 * This code is shared under the terms of the MIT License. This license permits use, alteration, and distribution, given that the
 * original copyright notice is included.
 * <p>
 * In maintaining the integrity and respect for the original work by 'agnor99', any modifications to this code have been made with the
 * intention of improving functionality specific to this project.
 * <p>
 * For any copyright-related concerns or issues regarding the use of this code, please contact us or the original author directly.
 * <p>
 * Original Author: 'agnor99'
 */
@Environment(value = EnvType.CLIENT)
public class DynamicArrowEntityRenderer extends EntityRenderer<DynamicArrowEntity> {
	private static final Identifier ARROW = new Identifier("textures/entity/projectiles/arrow.png");
	private final ItemRenderer renderer;


	public DynamicArrowEntityRenderer(EntityRendererFactory.Context context) {
		super(context);
		this.renderer = context.getItemRenderer();
	}


	@Override
	public void render(DynamicArrowEntity arrow, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
		matrixStack.push();
		matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(MathHelper.lerp(f, arrow.prevYaw, arrow.getYaw()) - 90.0F));
		matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(MathHelper.lerp(f, arrow.prevPitch, arrow.getPitch())));
		matrixStack.translate(-0.2, 0.0, 0.0);
		matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(-45.0F));
		matrixStack.scale(1.5F, 1.5F, 1.5F);

		ItemStack pickupItem = arrow.getStack();

		this.renderer.renderItem(pickupItem, ModelTransformation.Mode.GROUND, i, OverlayTexture.DEFAULT_UV, matrixStack, vertexConsumerProvider, arrow.getId());
		matrixStack.pop();
		super.render(arrow, f, g, matrixStack, vertexConsumerProvider, i);
	}

	@Override
	public Identifier getTexture(DynamicArrowEntity entity) {
		return ARROW;
	}

}
