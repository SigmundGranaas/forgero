package com.sigmundgranaas.forgero.properties.minecraft.useinteraction.handlers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.SimpleUseHandler;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

/**
 * Handler that applies durability damage to the item stack.
 *
 * <h3>JSON Example:</h3>
 * <pre>
 * {
 *   "type": "forgero:damage_stack",
 *   "damage": 1
 * }
 * </pre>
 */
public record DamageStackHandler(int damage) implements SimpleUseHandler {

	public static final String TYPE = "forgero:damage_stack";

	public static final Codec<DamageStackHandler> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.INT.optionalFieldOf("damage", 1).forGetter(DamageStackHandler::damage)
			).apply(instance, DamageStackHandler::new)
	);

	@Override
	public void apply(LivingEntity user, ItemStack stack, Hand hand) {
		if (user instanceof PlayerEntity player && !player.getAbilities().creativeMode) {
			stack.damage(damage, player, (p) -> p.sendToolBreakStatus(hand));
		}
	}

	@Override
	public String type() {
		return TYPE;
	}
}
