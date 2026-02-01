// modules/mc/loader/src/main/java/com/sigmundgranaas/forgero/loader/mixin/ItemStackAttributeMixin.java

package com.sigmundgranaas.forgero.loader.mixin;

import com.google.common.collect.Multimap;
import com.sigmundgranaas.forgero.common.api.MixinServiceAccessor;

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
 * Delegates to MixinServiceAccessor, which bridges to ForgeroServices.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackAttributeMixin {

	@Inject(method = "getAttributeModifiers", at = @At("RETURN"), cancellable = true)
	private void forgero$injectForgeroAttributes(
			EquipmentSlot slot,
			CallbackInfoReturnable<Multimap<EntityAttribute, EntityAttributeModifier>> cir
	) {
		ItemStack stack = (ItemStack) (Object) this;
		Multimap<EntityAttribute, EntityAttributeModifier> vanillaMap = cir.getReturnValue();

		// Delegate to the service accessor - no business logic in mixin
		Multimap<EntityAttribute, EntityAttributeModifier> finalMap =
				MixinServiceAccessor.getAttributeModifiers(stack, vanillaMap, slot);

		cir.setReturnValue(finalMap);
	}
}
