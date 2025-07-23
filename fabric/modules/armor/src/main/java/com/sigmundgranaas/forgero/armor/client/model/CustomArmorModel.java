package com.sigmundgranaas.forgero.armor.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

/**
 * A generic, Biped-like entity model that wraps a custom ModelPart.
 * This is used for models loaded from custom EntityModelLayers. It assumes
 * the provided ModelPart is already correctly shaped and dilated.
 *
 * @param <T> The type of entity this model is for.
 */
public class CustomArmorModel<T extends LivingEntity> extends BipedEntityModel<T> {

	public CustomArmorModel(ModelPart root) {
		super(root);
	}

	@NotNull
	@Override
	protected Iterable<ModelPart> getBodyParts() {
		return ImmutableList.of(this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg, this.hat);
	}

	@NotNull
	@Override
	protected Iterable<ModelPart> getHeadParts() {
		return ImmutableList.of(this.head);
	}

	@Override
	public void copyBipedStateTo(BipedEntityModel<T> biped) {
		super.copyBipedStateTo(biped);
	}
}
