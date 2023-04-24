package com.sigmundgranaas.forgero.quilt.client;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.minecraft.common.entity.SoulEntity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.util.Identifier;

import java.util.Collections;

@Environment(value = EnvType.CLIENT)
public class SoulEntityModel<T extends SoulEntity>
		extends AnimalModel<T> {
	public static final EntityModelLayer SOUL_ENTITY_MODEL_LAYER = new EntityModelLayer(new Identifier("forgero", "soul"), "main");
	private final ModelPart bone;

	public SoulEntityModel(ModelPart root) {
		super(false, 24.0f, 0.0f);
		this.bone = root.getChild(EntityModelPartNames.BONE);
	}

	public static TexturedModelData getTexturedModelData() {
		float f = 19.0f;
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData modelPartData2 = modelPartData.addChild(EntityModelPartNames.BONE, ModelPartBuilder.create(), ModelTransform.pivot(0.0f, 19.0f, 0.0f));
		ModelPartData modelPartData3 = modelPartData2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 0).cuboid(-3.5f, -4.0f, -5.0f, 7.0f, 7.0f, 10.0f), ModelTransform.NONE);
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
