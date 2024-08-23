package com.sigmundgranaas.forgero.core.property.v2.feature;

import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.model.match.PredicateFactory;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public record BasePredicateData(String id,
                                String type,
                                Matchable predicate,
                                String title,
                                List<String> description) {

	private static final Type typeOfList = new TypeToken<List<String>>() {
	}.getType();
	private static final Gson gson = new Gson();

	public static Optional<BasePredicateData> of(JsonElement element) {
		if (element.isJsonObject()) {
			var object = element.getAsJsonObject();
			if (object.has("type")) {
				String type = object.get("type").getAsString();
				Matchable predicate = Matchable.DEFAULT_TRUE;
				if (object.has("predicate")) {
					predicate = new PredicateFactory().create(object.get("predicate"));
				}

				String id = EMPTY_IDENTIFIER;
				String title = EMPTY_IDENTIFIER;
				List<String> description = Collections.emptyList();
				if (object.has("id")) {
					id = object.get("id").getAsString();
				}
				if (object.has("title")) {
					title = object.get("title").getAsString();
				}
				if (object.has("description")) {
					if (object.get("description").isJsonArray()) {
						description = gson.fromJson(object.get("description").getAsJsonArray(), typeOfList);
					} else {
						description = List.of(object.get("description").getAsString());
					}
				}
				return Optional.of(new BasePredicateData(id, type, predicate, title, description));
			}
		}
		return Optional.empty();
	}

	public static BasePredicateData empty(String type){
		return new BasePredicateData(EMPTY_IDENTIFIER, type, Matchable.DEFAULT_TRUE, EMPTY_IDENTIFIER, Collections.emptyList());
	}
}
