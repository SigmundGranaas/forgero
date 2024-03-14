package com.sigmundgranaas.forgero.minecraft.common.predicate;

import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class DimensionPredicate(String dimension) implements Predicate<World> {

	@Override
	public boolean test(World world) {
		return world.getRegistryKey().getValue().equals(new Identifier(dimension));
	}


}
