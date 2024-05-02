package com.sigmundgranaas.forgero.minecraft.common.predicate;

import java.util.function.Predicate;

import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public record DimensionPredicate(String dimension) implements Predicate<World> {
	@Override
	public boolean test(World world) {
		return world.getRegistryKey().getValue().equals(new Identifier(dimension));
	}
}
