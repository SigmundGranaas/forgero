package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.util.match.Matchable;

@FunctionalInterface
public interface PredicateWriterBuilder {
	Optional<PredicateWriter> build(Matchable matchable, TooltipConfiguration configuration);
}
