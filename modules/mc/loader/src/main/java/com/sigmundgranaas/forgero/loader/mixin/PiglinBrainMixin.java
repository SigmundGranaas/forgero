package com.sigmundgranaas.forgero.loader.mixin;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.PropertyKeys;
import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to make Piglins recognize Forgero gold tools as "golden" items.
 * This allows players holding gold Forgero tools to safely interact with Piglins.
 */
@Mixin(PiglinBrain.class)
public abstract class PiglinBrainMixin {

	@Unique
	private static final String GOLDEN_FEATURE = "forgero:golden";

	@Inject(method = "isGoldenItem", at = @At("HEAD"), cancellable = true)
	private static void isGoldenItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (isGoldenForgeroTool(stack)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "wearsGoldArmor", at = @At("HEAD"), cancellable = true)
	private static void wearsGold(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
		if (isGoldenForgeroTool(entity.getMainHandStack())) {
			cir.setReturnValue(true);
		} else if (isGoldenForgeroTool(entity.getOffHandStack())) {
			cir.setReturnValue(true);
		}
	}

	@Unique
	private static boolean isGoldenForgeroTool(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return false;
		}
		try {
			var componentOpt = ForgeroApi.services().converter().toComponent(stack);
			return componentOpt
					.filter(PiglinBrainMixin::hasGoldenFeatureRecursive)
					.isPresent();
		} catch (Exception e) {
			// If API is not initialized or conversion fails, fall back to false
			return false;
		}
	}

	/**
	 * Recursively checks if a Component or any of its children is made of gold.
	 * Checks both the golden feature property and component identifiers containing "gold".
	 */
	@Unique
	private static boolean hasGoldenFeatureRecursive(Component component) {
		// Check if this component directly has the golden feature
		var features = component.properties(PropertyKeys.FEATURES);
		if (features.contains(GOLDEN_FEATURE)) {
			return true;
		}

		// Check if this component's ID indicates gold material
		String id = component.id().toString().toLowerCase();
		if (id.contains("gold") && !id.contains("golden_apple")) {
			return true;
		}

		// Recursively check all children
		for (Component child : component.getChildren()) {
			if (hasGoldenFeatureRecursive(child)) {
				return true;
			}
		}

		return false;
	}
}
