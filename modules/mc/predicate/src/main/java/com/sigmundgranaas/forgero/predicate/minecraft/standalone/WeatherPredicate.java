package com.sigmundgranaas.forgero.predicate.minecraft.standalone;
import com.sigmundgranaas.forgero.common.runtime.MinecraftContextKeys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.runtime.EvaluableCondition;
import com.sigmundgranaas.forgero.common.runtime.DynamicContext;

import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * A dynamic condition that checks the current weather in the world.
 * Can check for rain, thunderstorms, or both.
 *
 * <h3>Example (check for rain):</h3>
 * <pre>
 * {
 *   "type": "minecraft:weather",
 *   "raining": true
 * }
 * </pre>
 *
 * <h3>Example (check for thunderstorm):</h3>
 * <pre>
 * {
 *   "type": "minecraft:weather",
 *   "thundering": true
 * }
 * </pre>
 *
 * <h3>Example (check for clear weather):</h3>
 * <pre>
 * {
 *   "type": "minecraft:weather",
 *   "raining": false,
 *   "thundering": false
 * }
 * </pre>
 */
public record WeatherPredicate(
		@Nullable Boolean raining,
		@Nullable Boolean thundering
) implements EvaluableCondition {

	private static final Logger LOGGER = LoggerFactory.getLogger(WeatherPredicate.class);
	public static final OpenIdentifier TYPE = new OpenIdentifier("minecraft", "weather");

	public static final Codec<WeatherPredicate> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.BOOL.optionalFieldOf("raining").forGetter(c -> Optional.ofNullable(c.raining())),
					Codec.BOOL.optionalFieldOf("thundering").forGetter(c -> Optional.ofNullable(c.thundering()))
			).apply(instance, (rain, thunder) ->
					new WeatherPredicate(rain.orElse(null), thunder.orElse(null))));

	@Override
	public boolean test(DynamicContext context) {
		Optional<World> worldOpt = context.get(MinecraftContextKeys.WORLD);
		if (worldOpt.isEmpty()) {
			LOGGER.debug("WeatherPredicate: No world in context");
			return false;
		}
		boolean result = testWeather(worldOpt.get());
		LOGGER.trace("WeatherPredicate: raining={}, thundering={}, result={}", raining, thundering, result);
		return result;
	}

	private boolean testWeather(World world) {
		if (raining != null && raining != world.isRaining()) {
			return false;
		}
		if (thundering != null && thundering != world.isThundering()) {
			return false;
		}
		return true;
	}

	@Override
	public OpenIdentifier type() {
		return TYPE;
	}
}
