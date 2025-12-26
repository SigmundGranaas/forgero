package com.sigmundgranaas.forgero.properties.minecraft.ondamage;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.core.property.context.ContextKeys;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.effects.entity.ContextualEffectHandler;
import com.sigmundgranaas.forgero.effects.entity.EntityEffectHandler;
import com.sigmundgranaas.forgero.effects.entity.OnHitEffect;
import com.sigmundgranaas.forgero.loader.api.ForgeroServices;
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

	private static ComponentConverter converter;
	private static Resolver resolver;

	private OnDamageReceivedManager() {
		// Static class
	}

	/**
	 * Initializes the manager with required services.
	 * Called during Forgero initialization.
	 *
	 * @param services The Forgero services container
	 */
	public static void initialize(ForgeroServices services) {
		converter = services.converter();
		resolver = services.resolver();
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

		// Collect all equipment (armor + held items)
		List<ItemStack> equipment = new ArrayList<>();
		defender.getHandItems().forEach(equipment::add);
		defender.getArmorItems().forEach(equipment::add);

		// Process each equipped item
		for (ItemStack stack : equipment) {
			if (stack.isEmpty()) {
				continue;
			}

			converter.toComponent(stack).ifPresent(component -> {
				List<OnDamageReceivedProperty> properties = getActiveProperties(component, attacker, amount);

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
			});
		}
	}

	private static List<OnDamageReceivedProperty> getActiveProperties(Component component, Entity attacker, float amount) {
		var engine = new OnDamageReceivedProperty.Engine();
		DynamicContext.Builder contextBuilder = new DynamicContext.Builder();

		// Populate context with attacker tags
		Set<OpenIdentifier> attackerTags = Registries.ENTITY_TYPE.getEntry(attacker.getType())
				.streamTags()
				.map(TagKey::id)
				.map(id -> new OpenIdentifier(id.getNamespace(), id.getPath()))
				.collect(Collectors.toSet());

		contextBuilder.put(ContextKeys.TARGET_TAGS, attackerTags);

		// Future enhancement: Could add damage amount to context for conditional properties
		// contextBuilder.put(ContextKeys.DAMAGE_AMOUNT, amount);

		return resolver.resolve(component, engine, contextBuilder.build());
	}
}
