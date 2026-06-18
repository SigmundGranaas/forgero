package com.sigmundgranaas.forgero.predicate.minecraft.standalone;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.runtime.DynamicContext;
import com.sigmundgranaas.forgero.common.runtime.EvaluableCondition;
import com.sigmundgranaas.forgero.common.runtime.MinecraftContextKeys;

import net.minecraft.world.World;

/**
 * Dynamic condition on the in-world time of day. Either a coarse {@code phase} ("day" / "night",
 * split at dusk = tick 12000) or an explicit {@code min}/{@code max} tick-of-day range.
 *
 * <pre>{ "type": "forgero:time_of_day", "phase": "night" }</pre>
 */
public record TimeOfDayPredicate(Optional<String> phase, Optional<Long> min, Optional<Long> max) implements EvaluableCondition {
	public static final OpenIdentifier TYPE = new OpenIdentifier("forgero", "time_of_day");
	private static final long DUSK = 12000L;
	private static final long DAY_LENGTH = 24000L;

	public static final Codec<TimeOfDayPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.optionalFieldOf("phase").forGetter(TimeOfDayPredicate::phase),
			Codec.LONG.optionalFieldOf("min").forGetter(TimeOfDayPredicate::min),
			Codec.LONG.optionalFieldOf("max").forGetter(TimeOfDayPredicate::max)
	).apply(instance, TimeOfDayPredicate::new));

	public TimeOfDayPredicate {
		phase.ifPresent(p -> {
			if (!p.equals("day") && !p.equals("night")) {
				throw new IllegalArgumentException("time_of_day phase must be 'day' or 'night', got: " + p);
			}
		});
	}

	@Override
	public boolean test(DynamicContext context) {
		Optional<World> worldOpt = context.get(MinecraftContextKeys.WORLD);
		if (worldOpt.isEmpty()) {
			return false;
		}
		long timeOfDay = Math.floorMod(worldOpt.get().getTimeOfDay(), DAY_LENGTH);

		if (phase.isPresent()) {
			boolean night = timeOfDay >= DUSK;
			if (phase.get().equals("night") != night) {
				return false;
			}
		}
		if (min.isPresent() && timeOfDay < min.get()) {
			return false;
		}
		if (max.isPresent() && timeOfDay > max.get()) {
			return false;
		}
		return true;
	}

	@Override
	public OpenIdentifier type() {
		return TYPE;
	}
}
