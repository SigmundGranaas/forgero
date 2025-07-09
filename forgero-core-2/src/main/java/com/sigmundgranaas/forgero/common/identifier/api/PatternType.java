package com.sigmundgranaas.forgero.common.identifier.api;

/**
 * Defines different parts of an OpenIdentifier that can be used for pattern matching.
 * After {@link OpenIdentifier}s are canonicalized (normalized) by {@link IdentifierFactory}
 * to have a single-group path (e.g., "forgero:iron_pickaxe"), the interpretation of some
 * pattern types changes:
 * <ul>
 *     <li>{@link #NAMESPACE}: The namespace part ("forgero").</li>
 *     <li>{@link #FILETYPE}: The single, canonical name of the resource (e.g., "iron_pickaxe").</li>
 *     <li>{@link #PATH}: The directory structure of the path. For a canonical ID, this will always be an empty string.</li>
 *     <li>{@link #NAME}: The final segment of the path, representing the resource's name. For a canonical ID, this is the entire path.</li>
 *     <li>{@link #LOCATION}: The full canonical path, which is now identical to the {@link #NAME} and {@link #FILETYPE} for a canonical ID.</li>
 * </ul>
 */
public enum PatternType {
	NAMESPACE,
	FILETYPE,
	PATH,
	NAME,
	LOCATION
}
