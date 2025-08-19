package com.sigmundgranaas.forgero.smithing.model;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

// Made with Blockbench 4.12.6
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports
public class BellowsModel extends EntityModel<Entity> {
	// Allow renderer to request the baked model part
	public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(new Identifier("forgero", "bellows"), "main");
	public static final Identifier TEXTURE = new Identifier("forgero", "textures/block/bellows.png"); // point to your texture

	private final ModelPart accordion;
	private final ModelPart bb_main;

	public BellowsModel(ModelPart root) {
		this.accordion = root.getChild("accordion");
		this.bb_main = root.getChild("bb_main");
	}

	// Optional accessors
	public ModelPart getAccordion() {
		return this.accordion;
	}
	public ModelPart getBase() {
		return this.bb_main;
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData accordion = modelPartData.addChild("accordion", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 23.0F, 0.0F));

		ModelPartData cube_r1 = accordion.addChild("cube_r1", ModelPartBuilder.create().uv(2, 0).cuboid(1.0F, -6.0F, -1.0F, 0.0F, 6.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.3491F, 0.0F, 0.0F));

		ModelPartData bb_main = modelPartData.addChild("bb_main", ModelPartBuilder.create().uv(0, 0).cuboid(-1.0F, -2.0F, -8.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
		return TexturedModelData.of(modelData, 16, 16);
	}

	@Override
	public void setAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		// no-op
	}

	// Render helpers to draw parts individually (so the renderer can scale the accordion only)
	public void renderAccordion(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
		this.accordion.render(matrices, vertices, light, overlay, red, green, blue, alpha);
	}
	public void renderBase(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
		this.bb_main.render(matrices, vertices, light, overlay, red, green, blue, alpha);
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		accordion.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		bb_main.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}
}
