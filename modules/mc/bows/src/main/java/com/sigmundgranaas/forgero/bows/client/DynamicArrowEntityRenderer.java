package com.sigmundgranaas.forgero.bows.client;

import com.sigmundgranaas.forgero.bows.entity.DynamicArrowEntity;
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
 * Renderer for DynamicArrowEntity that displays arrows as custom items with velocity-based orientation.
 *
 * <p>Originally adapted from SpriteArrowRenderer by 'agnor99' in their 'arrow-sprites' mod.
 * <ul>
 *   <li>Modrinth: <a href="https://modrinth.com/mod/sprite-arrows">Sprite Arrows</a></li>
 *   <li>GitHub: <a href="https://github.com/agnor99/arrow-sprites/blob/1.20.x/common/src/main/java/dev/agnor/spritearrows/SpriteArrowRenderer.java">SpriteArrowRenderer</a></li>
 * </ul>
 *
 * <p>Shared under the MIT License. Modifications made for Forgero integration.
 *
 * <p><b>Original Author:</b> 'agnor99'
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
	public void render(DynamicArrowEntity arrow, float f, float g, MatrixStack matrixStack,
	                   VertexConsumerProvider vertexConsumerProvider, int i) {
		matrixStack.push();

		// Calculate orientation based on velocity
		Vec3d velocity = arrow.getVelocity();
		float yaw = (float) Math.toDegrees(Math.atan2(velocity.x, velocity.z));
		float pitch = (float) Math.toDegrees(Math.atan2(velocity.y,
				Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z)));

		// Apply rotations for proper arrow orientation
		matrixStack.multiply(POSITIVE_Y.rotationDegrees(yaw - 90.0F));
		matrixStack.multiply(POSITIVE_Z.rotationDegrees(pitch));
		matrixStack.multiply(POSITIVE_Z.rotationDegrees(-45.0F)); // Pitch adjustment

		// Render the arrow ItemStack as a GROUND item
		this.renderer.renderItem(arrow.getStack(), GROUND, i, 0,
				matrixStack, vertexConsumerProvider, arrow.getWorld(), i);

		matrixStack.pop();
		super.render(arrow, f, g, matrixStack, vertexConsumerProvider, i);
	}

	@Override
	public Identifier getTexture(DynamicArrowEntity entity) {
		return ARROW;
	}
}
