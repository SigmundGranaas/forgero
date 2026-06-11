package com.sigmundgranaas.forgero.properties.minecraft.ondamage;

import com.sigmundgranaas.forgero.common.api.item.ItemPropertyApi;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.runtime.ContextKeys;
import com.sigmundgranaas.forgero.common.runtime.DynamicContext;
import com.sigmundgranaas.forgero.common.runtime.RuntimeConditions;
import com.sigmundgranaas.forgero.effects.entity.ContextualEffectHandler;
import com.sigmundgranaas.forgero.effects.entity.EntityEffectHandler;
import com.sigmundgranaas.forgero.effects.entity.OnHitEffect;
import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manager for handling On-Damage-Received events.
 * This is triggered when an entity takes damage and applies defensive effects.
 */
public class OnDamageReceivedManager {

	private OnDamageReceivedManager() {
		// Static class
	}

	/**
	 * Handles the damage received event by applying effects from equipped items.
	 * Checks both armor and held items for on-damage-received properties.
	 *
	 * @param defender The entity taking damage (becomes the "source" for effects)
	 * @param damageSource The source of the damage
	 * @param amount The amount of damage being received
	 */
	public static void handleDamageReceived(LivingEntity defender, DamageSource damageSource, float amount) {
		if (defender.getWorld().isClient()) {
			return; // Server-side only
		}

		Entity attacker = damageSource.getAttacker();
		if (attacker == null) {
			// No attacker (environmental damage, fall damage, etc.) - skip
			return;
		}

		// Build context with attacker tags
		DynamicContext.Builder contextBuilder = new DynamicContext.Builder();
		Set<OpenIdentifier> attackerTags = Registries.ENTITY_TYPE.getEntry(attacker.getType())
				.streamTags()
				.map(TagKey::id)
				.map(id -> new OpenIdentifier(id.getNamespace(), id.getPath()))
				.collect(Collectors.toSet());
		contextBuilder.put(ContextKeys.TARGET_TAGS, attackerTags);
		// Future enhancement: Could add damage amount to context for conditional properties
		// contextBuilder.put(ContextKeys.DAMAGE_AMOUNT, amount);
		DynamicContext context = contextBuilder.build();

		// Collect all equipment (armor + held items)
		List<ItemStack> equipment = new ArrayList<>();
		defender.getHandItems().forEach(equipment::add);
		defender.getArmorItems().forEach(equipment::add);

		// Process each equipped item
		for (ItemStack stack : equipment) {
			if (stack.isEmpty()) {
				continue;
			}

			List<OnDamageReceivedProperty> properties = RuntimeConditions.filter(ForgeroApi.itemProperty().resolve(stack, OnDamageReceivedProperty.Engine::new), context);

			for (OnDamageReceivedProperty property : properties) {
				// Defender is "source", attacker is "initial target"
				List<Entity> finalTargets = property.selector().select(defender, attacker);

				for (Entity finalTarget : finalTargets) {
					for (OnHitEffect effect : property.effects()) {
						if (effect instanceof ContextualEffectHandler contextual) {
							contextual.apply(defender, finalTarget);
						} else if (effect instanceof EntityEffectHandler simple) {
							simple.apply(finalTarget);
						}
					}
				}
			}
		}
	}
}
