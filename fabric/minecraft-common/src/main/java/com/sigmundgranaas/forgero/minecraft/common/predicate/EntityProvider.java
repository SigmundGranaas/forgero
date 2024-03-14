package com.sigmundgranaas.forgero.minecraft.common.predicate;

import java.util.function.Function;

import net.minecraft.entity.Entity;

public class EntityProvider {
	public static <T> Provider<Entity, T> entity(Function<Entity, T> transformer) {
		return transformer::apply;
	}
}
