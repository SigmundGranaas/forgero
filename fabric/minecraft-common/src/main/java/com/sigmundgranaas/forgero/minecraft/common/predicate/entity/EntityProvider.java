package com.sigmundgranaas.forgero.minecraft.common.predicate.entity;

import java.util.function.Function;

import com.sigmundgranaas.forgero.minecraft.common.predicate.Provider;

import net.minecraft.entity.Entity;

public class EntityProvider {
	public static <T> Provider<Entity, T> entity(Function<Entity, T> transformer) {
		return transformer::apply;
	}
}
