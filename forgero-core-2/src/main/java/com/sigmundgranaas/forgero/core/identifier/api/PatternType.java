package com.sigmundgranaas.forgero.core.identifier.api;

/**
 * Defines different parts of an OpenIdentifier that can be used for pattern matching.
 * For an identifier like "forgero:parts/heads/pickaxe_head":
 * <ul>
 *     <li>{@link #NAMESPACE}: The namespace part ("forgero").</li>
 *     <li>{@link #FILETYPE}: The first segment of the path, representing a category or type ("parts").</li>
 *     <li>{@link #PATH}: The directory structure of the path, excluding the name ("parts/heads").</li>
 *     <li>{@link #NAME}: The final segment of the path, representing the resource's name ("pickaxe_head").</li>
 *     <li>{@link #LOCATION}: The full path including all segments ("parts/heads/pickaxe_head").</li>
 * </ul>
 */
public enum PatternType {
	NAMESPACE,
	FILETYPE,
	PATH,
	NAME,
	LOCATION
}
