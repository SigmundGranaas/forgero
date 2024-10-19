package com.sigmundgranaas.forgero.utils;

import java.util.Optional;

import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * A utility class for working with registries. Will try to provide safe handling of registry lookups.
 */
public class RegistryUtils {
	/**
	 * Will try to safely create an {@link Identifier} from the given string.
	 *
	 * @param id the string to parse
	 * @return the identifier, if the string is valid
	 */
	public static Optional<Identifier> safeId(String id) {
		try {
			return Optional.of(new Identifier(id));
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	/**
	 * Will try to safely look up an object in the given registry.
	 *
	 * @param registry the registry to lookup in
	 * @param id       the id to lookup
	 * @param <T>      the type of object to lookup
	 * @return the object, if present
	 */
	public static <T> Optional<T> safeRegistryLookup(Registry<T> registry, Identifier id) {
		try {
			return Optional.ofNullable(registry.get(id));
		} catch (Exception e) {
			return Optional.empty();
		}
	}
}
