package com.sigmundgranaas.forgero.model.api;

/**
 * Represents a named coordinate on a model's texture grid.
 * Used for aligning or "snapping" child models to parent models.
 *
 * @param name The unique name of the mount point (e.g., "handle_top", "center").
 * @param x    The x-coordinate of the point.
 * @param y    The y-coordinate of the point.
 */
public record MountPoint(String name, int x, int y) {
	public static final MountPoint ZERO = new MountPoint("zero", 0, 0);

	public Offset getOffset() {
		return new Offset(x, y);
	}
}
