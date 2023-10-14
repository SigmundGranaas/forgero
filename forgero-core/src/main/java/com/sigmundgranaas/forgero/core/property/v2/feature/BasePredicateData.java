package com.sigmundgranaas.forgero.core.property.v2.feature;

import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

import java.util.Optional;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.model.match.PredicateFactory;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public record BasePredicateData(String id,
                                String type,
                                Matchable predicate,
                                @Nullable
                                String title,
                                @Nullable
                                String description) {

	public static Optional<BasePredicateData> of(JsonElement element) {
		if (element.isJsonObject()) {
			var object = element.getAsJsonObject();
			if (object.has("type")) {
				String type = object.get("type").getAsString();
				var predicate = new PredicateFactory().create(element);
				String id = EMPTY_IDENTIFIER;
				String title = EMPTY_IDENTIFIER;
				String description = EMPTY_IDENTIFIER;
				if (object.has("id")) {
					id = object.get("id").getAsString();
				}
				if (object.has("title")) {
					title = object.get("title").getAsString();
				}
				if (object.has("description")) {
					description = object.get("description").getAsString();
				}
				return Optional.of(new BasePredicateData(id, type, predicate, title, description));
			}
		}
		return Optional.empty();
	}
}
