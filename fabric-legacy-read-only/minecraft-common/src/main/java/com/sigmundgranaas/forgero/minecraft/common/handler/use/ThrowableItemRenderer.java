package com.sigmundgranaas.forgero.minecraft.common.handler.use;

import static net.minecraft.util.math.RotationAxis.POSITIVE_Y;
import static net.minecraft.util.math.RotationAxis.POSITIVE_Z;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;


public class ThrowableItemRenderer extends EntityRenderer<ThrowableItem> {
	private static final Identifier ARROW = new Identifier("textures/entity/projectiles/arrow.png");
	private final net.minecraft.client.render.item.ItemRenderer renderer;


	public ThrowableItemRenderer(EntityRendererFactory.Context context) {
		super(context);
		this.renderer = context.getItemRenderer();
	}

	@Override
	public void render(ThrowableItem item, float yaw, float partialTicks, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int light) {
		// Not implemented yet.
		assert (item.getSpinType() != ThrowableItem.SpinType.HORIZONTAL);

		matrices.push();

		Vec3d velocity = item.getVelocity();

		float prevTime = item.age - 1 + partialTicks;
		float currentTime = item.age + partialTicks;
		double sqrt = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
		float spinSpeed = 120;

		float prevSpinAngle = prevTime * spinSpeed;
		float currentSpinAngle = currentTime * spinSpeed;
		float lerpedSpinAngle = MathHelper.lerp(partialTicks, prevSpinAngle, currentSpinAngle);


		float yaw2 = (float) Math.toDegrees(Math.atan2(velocity.x, velocity.z));
		float pitch = (float) Math.toDegrees(Math.atan2(velocity.y, sqrt));

		switch (item.getSpinType()) {
			case VERTICAL:
				matrices.multiply(POSITIVE_Y.rotationDegrees(yaw2 - 90.0F));
				matrices.multiply(POSITIVE_Z.rotationDegrees(pitch));

				matrices.multiply(POSITIVE_Z.rotationDegrees(-45.0F));

				if (sqrt > 0.01 && !item.isInGround()) {
					matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(lerpedSpinAngle));
				}
				break;
			case HORIZONTAL:

				throw new RuntimeException("Not implemented yet!");
			case NONE:

				matrices.multiply(POSITIVE_Y.rotationDegrees(yaw - 90.0F));
				matrices.multiply(POSITIVE_Z.rotationDegrees(pitch));

				matrices.multiply(POSITIVE_Z.rotationDegrees(-135.0F));
				break;
		}

		ItemStack pickupItem = item.asItemStack();
		this.renderer.renderItem(pickupItem, ModelTransformationMode.FIXED, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumerProvider, item.getWorld(), item.getId());

		matrices.pop();
		super.render(item, yaw, partialTicks, matrices, vertexConsumerProvider, light);
	}

	@Override
	public Identifier getTexture(ThrowableItem entity) {
		return ARROW;
	}
}
