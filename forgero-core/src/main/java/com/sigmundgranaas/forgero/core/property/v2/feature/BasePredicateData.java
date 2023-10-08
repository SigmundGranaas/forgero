package com.sigmundgranaas.forgero.core.property.v2.feature;

import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.model.match.PredicateFactory;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public record BasePredicateData(String id, String type, Matchable predicate) {

	Optional<BasePredicateData> of(JsonElement element) {
		if (element.isJsonObject()) {
			var object = element.getAsJsonObject();
			if (object.has("type")) {
				String type = object.get("type").getAsString();
				var predicate = new PredicateFactory().create(element);
				String id = EMPTY_IDENTIFIER;
				if (object.has("id")) {
					id = object.get(id).getAsString();
				}
				return Optional.of(new BasePredicateData(id, type, predicate));
			}
		}
		return Optional.empty();
	}
}
