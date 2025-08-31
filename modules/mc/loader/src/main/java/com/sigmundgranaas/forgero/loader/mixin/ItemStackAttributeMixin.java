package com.sigmundgranaas.forgero.loader.mixin;

import com.google.common.collect.Multimap;
import com.sigmundgranaas.forgero.common.attribute.AttributeManager;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin into ItemStack to intercept the getAttributeModifiers method.
 * This is the primary hook for injecting Forgero's attribute system into the game.
 * It delegates all logic to the ForgeroAttributeManager to keep the mixin clean and simple.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackAttributeMixin {

	@Inject(method = "getAttributeModifiers", at = @At("RETURN"), cancellable = true)
	private void forgero$injectForgeroAttributes(EquipmentSlot slot, CallbackInfoReturnable<Multimap<EntityAttribute, EntityAttributeModifier>> cir) {
		ItemStack stack = (ItemStack) (Object) this;
		Multimap<EntityAttribute, EntityAttributeModifier> vanillaMap = cir.getReturnValue();

		// Delegate to the ForgeroAttributeManager to get the final, potentially modified, attribute map.
		Multimap<EntityAttribute, EntityAttributeModifier> finalMap = AttributeManager.getAttributes(stack, vanillaMap, slot);

		cir.setReturnValue(finalMap);
	}
}
