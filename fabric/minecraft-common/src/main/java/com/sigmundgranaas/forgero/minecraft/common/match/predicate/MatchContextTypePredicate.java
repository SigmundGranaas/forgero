package com.sigmundgranaas.forgero.minecraft.common.match.predicate;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.sigmundgranaas.forgero.core.model.match.builders.ElementParser;
import com.sigmundgranaas.forgero.core.model.match.builders.PredicateBuilder;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

import net.minecraft.util.JsonSerializer;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

public class MatchContextTypePredicate implements Matchable {
	public static String ID = "forgero:context_type";

	final List<Type> types;

	MatchContextTypePredicate(List<Type> types) {
		this.types = types;
	}

	public boolean test(Matchable target, MatchContext context) {
		return types.stream().allMatch(type -> context.test(type, MatchContext.of()));
	}

	public static class Serializer implements JsonSerializer<MatchContextTypePredicate> {
		public void toJson(JsonObject jsonObject, MatchContextTypePredicate matchContextTypePredicate, JsonSerializationContext jsonSerializationContext) {

		}

		public MatchContextTypePredicate fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
			List<Type> types = StreamSupport.stream(jsonObject.getAsJsonArray("types").spliterator(), false)
					.map(JsonElement::getAsString)
					.map(Type::of)
					.toList();
			return new MatchContextTypePredicate(types);
		}
	}

	public static class MatchContextTypePredicateBuilder implements PredicateBuilder {
		@Override
		public Optional<Matchable> create(JsonElement element) {
			return ElementParser.fromIdentifiedElement(element, ID)
					.map(json -> new MatchContextTypePredicate.Serializer().fromJson(json, null));
		}
	}
}

