package com.sigmundgranaas.forgero.minecraft.common.match;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.model.v1.match.builders.ElementParser;
import com.sigmundgranaas.forgero.core.model.v1.match.builders.PredicateBuilder;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

import net.minecraft.item.ItemStack;

/**
 * Matches if the damage percentage of an ItemStack within the context is greater than or equal to the given percentage.
 */
public record DamagePercentagePredicate(float percentage) implements Matchable {
	public static String ID = "forgero:damage_percentage";

	@Override
	public boolean test(Matchable match, MatchContext context) {
		var stackOpt = context.get("stack", ItemStack.class);
		if (percentage > 1) {
			return stackOpt.filter(stack -> ((float) stack.getDamage() / (float) stack.getMaxDamage() * 100) >= percentage).isPresent();
		} else {
			return stackOpt.filter(stack -> ((float) stack.getDamage() / (float) stack.getMaxDamage()) >= percentage).isPresent();
		}
	}

	/**
	 * Attempts to create a Matchable of type DamagePercentagePredicate from a JsonElement if the element is identified as a "DamagePercentagePredicate".
	 */
	public static class DamagePercentagePredicateBuilder implements PredicateBuilder {
		@Override
		public Optional<Matchable> create(JsonElement element) {
			return ElementParser.fromIdentifiedElement(element, ID).map(jsonObject -> new DamagePercentagePredicate(jsonObject.get("percentage").getAsInt()));
		}
	}
}
