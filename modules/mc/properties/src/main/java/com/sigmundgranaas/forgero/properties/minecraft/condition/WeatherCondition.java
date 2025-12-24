package com.sigmundgranaas.forgero.properties.minecraft.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * A dynamic condition that checks the current weather in the world.
 * Can check for rain, thunderstorms, or both.
 *
 * <h3>Example (check for rain):</h3>
 * <pre>
 * {
 *   "type": "forgero:weather",
 *   "raining": true
 * }
 * </pre>
 *
 * <h3>Example (check for thunderstorm):</h3>
 * <pre>
 * {
 *   "type": "forgero:weather",
 *   "thundering": true
 * }
 * </pre>
 *
 * <h3>Example (check for clear weather):</h3>
 * <pre>
 * {
 *   "type": "forgero:weather",
 *   "raining": false,
 *   "thundering": false
 * }
 * </pre>
 */
public record WeatherCondition(
		OpenIdentifier type,
		@Nullable Boolean raining,
		@Nullable Boolean thundering
) implements DynamicCondition {

	public static final Codec<WeatherCondition> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(WeatherCondition::type),
					Codec.BOOL.optionalFieldOf("raining").forGetter(c -> java.util.Optional.ofNullable(c.raining())),
					Codec.BOOL.optionalFieldOf("thundering").forGetter(c -> java.util.Optional.ofNullable(c.thundering()))
			).apply(instance, (type, rain, thunder) ->
					new WeatherCondition(type, rain.orElse(null), thunder.orElse(null))));

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
}
