package com.sigmundgranaas.forgero.smithing.model;// Made with Blockbench 4.12.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import net.minecraft.client.model.Dilation;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

// This layer location should be baked with EntityModelLayer in the entity renderer and passed into this model's constructor
public class BellowsModel<T extends Entity> extends EntityModel<T> {
	public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(new Identifier("forgero", "bellows"), "main");
	private final ModelPart bb_main;

	public BellowsModel(ModelPart root) {
		this.bb_main = root.getChild("bb_main");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData root = modelData.getRoot();

		ModelPartData bb_main = root.addChild("bb_main", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		bb_main.addChild(
			"cube_r1",
			ModelPartBuilder.create()
				.uv(-1, -3)
				.cuboid(1.0F, -7.0F, -1.0F, 0.0F, 7.0F, 5.0F, new Dilation(0.0F)),
			ModelTransform.of(0.0F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.0F)
		);

		return TexturedModelData.of(modelData, 16, 16);
	}

	@Override
	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {

	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
		bb_main.render(matrices, vertices, light, overlay, red, green, blue, alpha);
	}
}
