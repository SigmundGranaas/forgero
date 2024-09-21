package com.sigmundgranaas.forgero.tooltip.v2;

import java.util.List;

import com.sigmundgranaas.forgero.core.util.match.Matchable;

import net.minecraft.text.MutableText;

@FunctionalInterface
public interface PredicateWriter {
	List<MutableText> write(Matchable matchable);
}
