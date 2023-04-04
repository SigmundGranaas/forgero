package com.sigmundgranaas.forgero.fabric.client;

import com.sigmundgranaas.forgero.minecraft.common.entity.EnderTeleportationEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;

public class TeleportationEntityRenderer extends EntityRenderer<EnderTeleportationEntity> {
	private static final Identifier BLINK_TEXTURE = new Identifier("minecraft", "textures/item/chest.png");

	private final ItemRenderer itemRenderer;
	private final float scale;


	public TeleportationEntityRenderer(EntityRendererFactory.Context ctx, float scale) {
		super(ctx);
		this.itemRenderer = ctx.getItemRenderer();
		this.scale = scale;

	}

	@Override
	public Identifier getTexture(EnderTeleportationEntity entity) {
		return BLINK_TEXTURE;
	}

	@Override
	public void render(EnderTeleportationEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		if (entity.age >= 2 || !(this.dispatcher.camera.getFocusedEntity().squaredDistanceTo(entity) < 12.25)) {
			matrices.push();
			matrices.scale(this.scale, this.scale, this.scale);
			matrices.multiply(this.dispatcher.getRotation());
			matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0F));
			this.itemRenderer.renderItem(new ItemStack(Items.CHEST), ModelTransformation.Mode.GROUND, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.getId());
			matrices.pop();
			super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
		}
	}
}
