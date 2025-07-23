package com.sigmundgranaas.forgero.armor.client.mixin;

import com.sigmundgranaas.forgero.armor.item.ForgeroArmorItem;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.render.entity.feature.ArmorFeatureRenderer.class)
public class ArmorFeatureRendererMixin<T extends LivingEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>> {

	/**
	 * Prevents the default armor rendering logic from running for any ForgeroArmorItem.
	 * This is crucial to avoid Z-fighting and double-rendering, as our custom
	 * ForgeroArmorFeatureRenderer will handle it instead.
	 */
	@Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
	private void onRenderArmor(MatrixStack matrices, VertexConsumerProvider vertexConsumers, T entity, EquipmentSlot armorSlot, int light, A model, CallbackInfo ci) {
		if (entity.getEquippedStack(armorSlot).getItem() instanceof ForgeroArmorItem) {
			ci.cancel();
		}
	}
}
