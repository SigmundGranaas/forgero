package com.sigmundgranaas.forgero.predicate.minecraft.standalone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.predicate.minecraft.MinecraftContextKeys;

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
) implements DynamicCondition {

	public static final OpenIdentifier TYPE = new OpenIdentifier("minecraft", "weather");

	public static final Codec<WeatherPredicate> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.BOOL.optionalFieldOf("raining").forGetter(c -> Optional.ofNullable(c.raining())),
					Codec.BOOL.optionalFieldOf("thundering").forGetter(c -> Optional.ofNullable(c.thundering()))
			).apply(instance, (rain, thunder) ->
					new WeatherPredicate(rain.orElse(null), thunder.orElse(null))));

	@Override
	public boolean test(DynamicContext context) {
		return context.get(MinecraftContextKeys.WORLD)
				.map(this::testWeather)
				.orElse(false);
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
