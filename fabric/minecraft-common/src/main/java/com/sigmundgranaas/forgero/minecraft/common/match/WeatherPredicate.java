package com.sigmundgranaas.forgero.minecraft.common.match;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.sigmundgranaas.forgero.core.model.match.builders.ElementParser;
import com.sigmundgranaas.forgero.core.model.match.builders.PredicateBuilder;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.WORLD;

public class WeatherPredicate implements Matchable {
	public static String ID = "minecraft:weather_check";

	@Nullable
	final Boolean raining;
	@Nullable
	final Boolean thundering;

	WeatherPredicate(@Nullable Boolean raining, @Nullable Boolean thundering) {
		this.raining = raining;
		this.thundering = thundering;
	}

	public boolean test(Matchable target, MatchContext context) {
		var worldOpt = context.get(WORLD);
		if (worldOpt.isPresent() && worldOpt.get() instanceof ServerWorld serverWorld) {
			if (this.raining != null && this.raining != serverWorld.isRaining()) {
				return false;
			} else {
				return this.thundering == null || this.thundering == serverWorld.isThundering();
			}
		}
		return false;
	}

	public static class Serializer implements JsonSerializer<WeatherPredicate> {
		public void toJson(JsonObject jsonObject, WeatherPredicate weatherPredicate, JsonSerializationContext jsonSerializationContext) {
			jsonObject.addProperty("raining", weatherPredicate.raining);
			jsonObject.addProperty("thundering", weatherPredicate.thundering);
		}

		public WeatherPredicate fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
			Boolean raining = jsonObject.has("raining") ? JsonHelper.getBoolean(jsonObject, "raining") : null;
			Boolean thundering = jsonObject.has("thundering") ? JsonHelper.getBoolean(jsonObject, "thundering") : null;
			return new WeatherPredicate(raining, thundering);
		}
	}

	public static class WeatherPredicateBuilder implements PredicateBuilder {
		@Override
		public Optional<Matchable> create(JsonElement element) {
			return ElementParser.fromIdentifiedElement(element, ID)
					.map(json -> new WeatherPredicate.Serializer().fromJson(json, null));
		}
	}
}
