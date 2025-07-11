package com.sigmundgranaas.forgero.model.api;

/**
 * Represents a 2D offset for positioning textures.
 *
 * @param x The horizontal offset.
 * @param y The vertical offset.
 */
public record Offset(int x, int y) {
	public static final Offset ZERO = new Offset(0, 0);
}
