package com.sigmundgranaas.forgero.properties.minecraft.entityuse;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.useinteraction.EntityUseEffect;
import com.sigmundgranaas.forgero.common.useinteraction.UseContext;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.loader.api.ForgeroServices;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

import java.util.List;

/**
 * Manager class for handling entity use events.
 * Resolves EntityUseProperty from items and executes their effects.
 */
public class EntityUseManager {

	private static ComponentConverter converter;
	private static Resolver resolver;

	private EntityUseManager() {
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
	 * Handles the event when a player uses an item on an entity.
	 *
	 * @param stack The item stack used
	 * @param player The player using the item
	 * @param target The entity being used on
	 * @param hand The hand holding the item
	 * @return The result of the interaction
	 */
	public static ActionResult handleEntityUse(ItemStack stack, PlayerEntity player, Entity target, Hand hand) {
		if (stack.isEmpty() || player.getWorld().isClient()) {
			return ActionResult.PASS;
		}

		return converter.toComponent(stack)
				.map(component -> {
					List<EntityUseProperty> properties = getActiveProperties(component);

					if (properties.isEmpty()) {
						return ActionResult.PASS;
					}

					UseContext context = UseContext.startWithTarget(
							player.getWorld(),
							player,
							hand,
							stack,
							target
					);

					ActionResult finalResult = ActionResult.PASS;
					for (EntityUseProperty property : properties) {
						for (EntityUseEffect effect : property.effects()) {
							ActionResult result = effect.apply(context);
							if (result == ActionResult.FAIL) {
								return ActionResult.FAIL;
							}
							if (result != ActionResult.PASS) {
								finalResult = result;
							}
						}
					}

					return finalResult;
				})
				.orElse(ActionResult.PASS);
	}

	private static List<EntityUseProperty> getActiveProperties(Component component) {
		var engine = new EntityUseProperty.Engine();
		DynamicContext.Builder contextBuilder = new DynamicContext.Builder();

		// Build context for dynamic condition evaluation
		return resolver.resolve(component, engine, contextBuilder.build());
	}
}
