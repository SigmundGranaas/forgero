package com.sigmundgranaas.forgero.armor.client.model;

import com.sigmundgranaas.forgero.armor.item.ForgeroArmorItem;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.model.api.armor.ArmorModel;
import com.sigmundgranaas.forgero.model.resolution.api.armor.ArmorModelResolver;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ForgeroArmorFeatureRenderer<T extends LivingEntity, M extends BipedEntityModel<T>> extends FeatureRenderer<T, M> {

	private final ArmorModelResolver armorModelResolver;
	private final ForgeroArmorTextureManager textureManager;
	private final ForgeroArmorModelManager modelManager;
	private final Function<ItemStack, Optional<Component>> itemToComponent;

	public ForgeroArmorFeatureRenderer(FeatureRendererContext<T, M> context, Function<ItemStack, Optional<Component>> itemToComponent, ArmorModelResolver armorModelResolver, ForgeroArmorTextureManager textureManager, ForgeroArmorModelManager modelManager) {
		super(context);
		this.itemToComponent = itemToComponent;
		this.armorModelResolver = armorModelResolver;
		this.textureManager = textureManager;
		this.modelManager = modelManager;
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (slot.getType() == EquipmentSlot.Type.ARMOR) {
				renderArmorSlot(matrices, vertexConsumers, light, entity, slot, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
			}
		}
	}

	private void renderArmorSlot(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, EquipmentSlot slot, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
		if(!(entity instanceof PlayerEntity)){
			return;
		}
		ItemStack itemStack = entity.getEquippedStack(slot);
		if (!(itemStack.getItem() instanceof ForgeroArmorItem)) return;

		Optional<Component> componentOpt = itemToComponent.apply(itemStack);
		if (componentOpt.isEmpty()) return;
		Component component = componentOpt.get();

		// 1. Get the list of all 3D models to draw for this item.
		List<ArmorModel> modelsToRender = armorModelResolver.resolve(component);

		for (ArmorModel armorModel : modelsToRender) {
			// 2. Get the 3D model from the model manager (cached).
			Identifier modelIdentifier = new Identifier(armorModel.model().toString());
			// Pass the slot to the model manager to get a correctly dilated model
			Optional<EntityModel<T>> modelOpt = modelManager.getModel(modelIdentifier, slot ,this.getContextModel());
			if (modelOpt.isEmpty()) continue;
			EntityModel<T> model = modelOpt.get();

			// 3. Get the dynamic texture from the texture manager (cached/generated).
			Identifier texture = textureManager.getTexture(armorModel, component);

			// 4. Configure and render this specific piece.
			if (model instanceof BipedEntityModel biped) {
				this.getContextModel().copyBipedStateTo(biped);
				biped.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
				setVisible(biped, slot);
			}

			model.render(matrices, vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(texture)), light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
		}
	}

	private void setVisible(BipedEntityModel<?> bipedModel, EquipmentSlot slot) {
		bipedModel.setVisible(false);
		switch (slot) {
			case HEAD:
				bipedModel.head.visible = true;
				bipedModel.hat.visible = true;
				break;
			case CHEST:
				bipedModel.body.visible = true;
				bipedModel.rightArm.visible = true;
				bipedModel.leftArm.visible = true;
				break;
			case LEGS:
				bipedModel.body.visible = true;
				bipedModel.rightLeg.visible = true;
				bipedModel.leftLeg.visible = true;
				break;
			case FEET:
				bipedModel.rightLeg.visible = true;
				bipedModel.leftLeg.visible = true;
		}
	}
}
