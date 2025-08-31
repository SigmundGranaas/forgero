package com.sigmundgranaas.forgero.model.api;

/**
 * Represents a 2D offset for positioning textures.
 *
 * @param x The horizontal offset.
 * @param y The vertical offset.
 */
public record Offset(int x, int y) {
	public static final Offset ZERO = new Offset(0, 0);

	/**
	 * Adds another offset to this one.
	 *
	 * @param other The offset to add.
	 * @return A new Offset instance with the combined values.
	 */
	public Offset add(Offset other) {
		if (other == null) {
			return this;
		}
		return new Offset(this.x + other.x, this.y + other.y);
	}
}
