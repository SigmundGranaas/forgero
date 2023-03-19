package com.sigmundgranaas.forgero.minecraft.common.utils;

import java.util.Optional;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RegistryUtils {
	public static Optional<Identifier> safeId(String id) {
		try {
			return Optional.of(new Identifier(id));
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	public static <T> Optional<T> safeRegistryLookup(Registry<T> registry, Identifier id) {
		try {
			return Optional.ofNullable(registry.get(id));
		} catch (Exception e) {
			return Optional.empty();
		}
	}
}
