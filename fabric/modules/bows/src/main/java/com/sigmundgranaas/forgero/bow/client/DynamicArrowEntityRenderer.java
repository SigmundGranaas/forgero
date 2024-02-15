package com.sigmundgranaas.forgero.bow.client;


import com.sigmundgranaas.forgero.bow.entity.DynamicArrowEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import static net.minecraft.client.render.model.json.ModelTransformationMode.GROUND;
import static net.minecraft.util.math.RotationAxis.POSITIVE_Y;
import static net.minecraft.util.math.RotationAxis.POSITIVE_Z;

/**
 * This class, DynamicArrowEntityRenderer, is responsible for the rendering of DynamicArrowEntity.
 * Originally adapted from SpriteArrowRenderer
 * <p>
 * It is worth noting that the original version of this class was designed and implemented by the GitHub user 'agnor99' in their
 * 'arrow-sprites' mod, which can be found at: <a href="https://modrinth.com/mod/sprite-arrows">...</a>.
 * <p>
 * The original implementation can be accessed in 'agnor99's' GitHub repository at:
 * <a href="https://github.com/agnor99/arrow-sprites/blob/1.20.x/common/src/main/java/dev/agnor/spritearrows/SpriteArrowRenderer.java">SpriteArrowRenderer</a>.
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

		// Calculate orientation based on velocity
		Vec3d velocity = arrow.getVelocity();
		float yaw = (float) Math.toDegrees(Math.atan2(velocity.x, velocity.z));
		float pitch = (float) Math.toDegrees(Math.atan2(velocity.y, Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z)));

		matrixStack.multiply(POSITIVE_Y.rotationDegrees(yaw - 90.0F));
		matrixStack.multiply(POSITIVE_Z.rotationDegrees(pitch));

		// Pitching the arrow to correctly render its angle
		matrixStack.multiply(POSITIVE_Z.rotationDegrees(-45.0F));

		this.renderer.renderItem(arrow.getStack(), GROUND, i, 0, matrixStack, vertexConsumerProvider, arrow.getWorld(), i);

		matrixStack.pop();
		super.render(arrow, f, g, matrixStack, vertexConsumerProvider, i);
	}

	@Override
	public Identifier getTexture(DynamicArrowEntity entity) {
		return ARROW;
	}

}
