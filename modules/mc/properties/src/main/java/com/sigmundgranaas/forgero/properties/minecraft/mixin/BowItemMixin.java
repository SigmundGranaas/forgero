package com.sigmundgranaas.forgero.properties.minecraft.mixin;

import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.UseInteractionManager;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for {@link BowItem} to intercept the vanilla arrow-shooting behavior.
 *
 * <p>The vanilla {@link BowItem#onStoppedUsing} method directly spawns an ArrowEntity.
 * This mixin cancels that behavior when the bow has a UseInteractionProperty,
 * allowing the Forgero {@link UseInteractionManager} to handle the arrow spawning
 * via the configured handlers (e.g., LaunchProjectileHandler which spawns DynamicArrowEntity).</p>
 *
 * <p>Without this mixin, Forgero bows would spawn both a vanilla ArrowEntity (from BowItem)
 * AND potentially a DynamicArrowEntity (from the handler), or only the vanilla one if
 * the handler timing doesn't work correctly.</p>
 */
@Mixin(BowItem.class)
public abstract class BowItemMixin {

	/**
	 * Intercepts {@link BowItem#onStoppedUsing} to cancel vanilla arrow spawning
	 * when the bow has UseInteractionProperty configured.
	 *
	 * <p>The actual arrow spawning is handled by:
	 * <ol>
	 *   <li>{@link com.sigmundgranaas.forgero.properties.minecraft.mixin.ItemUseInteractionMixin}
	 *       calls {@link UseInteractionManager#handleRelease}</li>
	 *   <li>Which resolves the UseInteractionProperty and executes on_release handlers</li>
	 *   <li>The LaunchProjectileHandler spawns DynamicArrowEntity for Forgero arrows</li>
	 * </ol>
	 * </p>
	 */
	@Inject(method = "onStoppedUsing", at = @At("HEAD"), cancellable = true)
	private void forgero$interceptBowRelease(ItemStack stack, World world, LivingEntity user,
	                                          int remainingUseTicks, CallbackInfo ci) {
		if (UseInteractionManager.hasUseInteraction(stack)) {
			// Let the UseInteractionManager handle the release via ItemUseInteractionMixin
			// Cancel vanilla BowItem behavior to prevent duplicate arrow spawning
			UseInteractionManager.handleRelease(stack, world, user, remainingUseTicks);
			ci.cancel();
		}
	}
}
