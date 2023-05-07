package com.sigmundgranaas.forgero.fabric.client;

import java.util.Collections;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.minecraft.common.entity.SoulEntity;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value = EnvType.CLIENT)
public class SoulEntityModel<T extends SoulEntity>
		extends AnimalModel<T> {
	public static final EntityModelLayer SOUL_ENTITY_MODEL_LAYER = new EntityModelLayer(new Identifier("forgero", "soul"), "main");
	private final ModelPart bone;

	public SoulEntityModel(ModelPart root) {
		super(false, 24.0f, 0.0f);
		this.bone = root.getChild("bone");
	}

	public static TexturedModelData getTexturedModelData() {
		float f = 19.0f;
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData modelPartData2 = modelPartData.addChild("bone", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 19.0F, 0.0F));
		ModelPartData modelPartData3 = modelPartData2.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-3.5F, -4.0F, -5.0F, 7.0F, 7.0F, 10.0F), ModelTransform.NONE);
		return TexturedModelData.of(modelData, 64, 64);
	}

	@Override
	protected Iterable<ModelPart> getHeadParts() {
		return Collections.emptyList();
	}

	@Override
	protected Iterable<ModelPart> getBodyParts() {
		return ImmutableList.of(this.bone);
	}

	@Override
	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {

	}
}
