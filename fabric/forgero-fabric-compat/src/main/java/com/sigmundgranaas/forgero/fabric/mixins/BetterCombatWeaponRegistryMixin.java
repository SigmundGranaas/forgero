package com.sigmundgranaas.forgero.fabric.mixins;

import java.util.Map;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.state.SchematicBased;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedTool;
import com.sigmundgranaas.forgero.minecraft.common.conversion.StateConverter;
import net.bettercombat.api.AttributesContainer;
import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.logic.WeaponRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;


@Mixin(WeaponRegistry.class)
public abstract class BetterCombatWeaponRegistryMixin {
	private static final String BETTER_COMPAT_ATTRIBUTE_IDENTIFIER = "better_compat_attribute_container";
	@Shadow(remap = false)
	static Map<Identifier, AttributesContainer> containers;

	@Inject(method = "getAttributes(Lnet/minecraft/item/ItemStack;)Lnet/bettercombat/api/WeaponAttributes;", at = @At(value = "TAIL"), cancellable = true)
	private static void injectCustomForgeroAttributes(ItemStack stack, CallbackInfoReturnable<WeaponAttributes> cir) {
		var state = StateConverter.of(stack);
		if (state.isPresent() && state.get() instanceof ConstructedTool tool) {
			var head = tool.getHead();
			if (head instanceof SchematicBased based) {
				Optional<String> better_combat_attributes_id = based.schematic().customData().getString(BETTER_COMPAT_ATTRIBUTE_IDENTIFIER);
				if (better_combat_attributes_id.isPresent()) {
					var id = new Identifier(better_combat_attributes_id.get());
					if (containers.containsKey(id)) {
						var container = containers.get(id);
						cir.setReturnValue(WeaponRegistry.resolveAttributes(new Identifier(tool.identifier()), container));
					}
				}
			}
		}
	}
}
