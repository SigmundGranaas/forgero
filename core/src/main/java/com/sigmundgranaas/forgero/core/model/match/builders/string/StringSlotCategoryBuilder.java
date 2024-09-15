package com.sigmundgranaas.forgero.core.model.match.builders.string;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.model.match.builders.ElementParser;
import com.sigmundgranaas.forgero.core.model.match.builders.PredicateBuilder;
import com.sigmundgranaas.forgero.core.model.match.predicate.SlotCategoryPredicate;
import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

/**
 * Attempts to create a Matchable of type SlotCategory from a JsonElement if the element contains "slot_category:".
 */
public class StringSlotCategoryBuilder implements PredicateBuilder {
	@Override
	public Optional<Matchable> create(JsonElement element) {
		return ElementParser.fromString(element)
				.filter(string -> string.contains("slot_category:"))
				.map(string -> string.replace("slot_category:", ""))
				.map(Category::valueOf)
				.map(SlotCategoryPredicate::new);
	}
}
