package com.sigmundgranaas.forgero.minecraft.common.handler.afterUse;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.StopHandler;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import lombok.Getter;
import lombok.experimental.Accessors;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * Represents a handler that removes an upgrade after use.
 *
 * <p>Example JSON configuration:
 * <pre>
 * {
 *   "type": "minecraft:on_hit",
 *   "on_hit": {
 *     "type": "minecraft:explosion",
 *     "target": "minecraft:targeted_entity",
 *     "power": 3
 *   }
 *   "after_use": {
 *     "type": "minecraft:consume_upgrade",
 *     "id": "forgero:undying_totem"
 *   }
 * }
 * </pre>
 * </p>
 */
@Getter
@Accessors(fluent = true)
public class ConsumeUpgradeHandler implements AfterUseHandler, StopHandler {
	public static final String TYPE = "forgero:consume_upgrade";
	public static final JsonBuilder<ConsumeUpgradeHandler> BUILDER = HandlerBuilder.fromObject(ConsumeUpgradeHandler.class, ConsumeUpgradeHandler::fromJson);

	private final String identifier;

	public ConsumeUpgradeHandler(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * Constructs an {@link DamageHandler} from a JSON object.
	 *
	 * @param json The JSON object.
	 * @return A new instance of {@link DamageHandler}.
	 */
	public static ConsumeUpgradeHandler fromJson(JsonObject json) {
		String id = json.get("id").getAsString();
		return new ConsumeUpgradeHandler(id);
	}

	@Override
	public void handle(Entity source, ItemStack target, Hand hand) {
		if (source instanceof LivingEntity livingEntity) {
			var state = StateService.INSTANCE.convert(target);
			if (state.isPresent() && state.get() instanceof Composite composite) {
				var newState = composite.removeUpgrade(identifier);
				livingEntity.setStackInHand(hand, StateService.INSTANCE.update(newState, target));
			}
		}
	}

	@Override
	public void stoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		var state = StateService.INSTANCE.convert(stack);
		if (state.isPresent() && state.get() instanceof Composite composite) {
			var newState = composite.removeUpgrade(identifier);
			user.setStackInHand(user.getActiveHand(), StateService.INSTANCE.update(newState, stack));
		}
	}
}
