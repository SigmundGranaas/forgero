package com.sigmundgranaas.forgero.predicate.minecraft.standalone;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.runtime.DynamicContext;
import com.sigmundgranaas.forgero.common.runtime.EvaluableCondition;
import com.sigmundgranaas.forgero.common.runtime.MinecraftContextKeys;

import net.minecraft.world.World;

/**
 * Dynamic condition on the lunar phase (0-7, where 0 is full moon). Passes when the current phase
 * is one of the listed {@code phases}.
 *
 * <pre>{ "type": "forgero:moon_phase", "phases": [0] }</pre>
 */
public record MoonPhasePredicate(List<Integer> phases) implements EvaluableCondition {
	public static final OpenIdentifier TYPE = new OpenIdentifier("forgero", "moon_phase");

	public static final Codec<MoonPhasePredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.list(Codec.intRange(0, 7)).fieldOf("phases").forGetter(MoonPhasePredicate::phases)
	).apply(instance, MoonPhasePredicate::new));

	public MoonPhasePredicate {
		if (phases.isEmpty()) {
			throw new IllegalArgumentException("moon_phase requires at least one phase");
		}
	}

	@Override
	public boolean test(DynamicContext context) {
		Optional<World> worldOpt = context.get(MinecraftContextKeys.WORLD);
		return worldOpt.map(world -> phases.contains(world.getMoonPhase())).orElse(false);
	}

	@Override
	public OpenIdentifier type() {
		return TYPE;
	}
}
