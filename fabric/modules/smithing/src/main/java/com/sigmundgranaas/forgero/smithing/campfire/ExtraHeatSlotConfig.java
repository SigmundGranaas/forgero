package com.sigmundgranaas.forgero.smithing.campfire;

/**
 * Simple configuration for the extra heat slot visuals and heating.
 */
public final class ExtraHeatSlotConfig {
	private ExtraHeatSlotConfig() {}

	// Heating behavior
	public static final int HEAT_PER_TICK = 2;

	// Particle offset for the extra slot (relative to block pos)
	public static final double PARTICLE_X = 0.80D;
	public static final double PARTICLE_Y = 0.62D;
	public static final double PARTICLE_Z = 0.20D;

	// Vanilla-aligned renderer transforms for the extra slot
	public static final float RENDER_BASE_TRANSLATE_X = 0.5F;
	public static final float RENDER_BASE_TRANSLATE_Y = 0.44921875F; // same as vanilla campfire
	public static final float RENDER_BASE_TRANSLATE_Z = 0.5F;
	// Offset within the campfire surface after facing rotation
	public static final float RENDER_SLOT_OFFSET_X = 0.3125F;   // 5/16 toward the right
	public static final float RENDER_SLOT_OFFSET_Z = -0.3125F;  // 5/16 toward the front
	public static final float RENDER_SCALE = 0.375F;            // vanilla scale for items on campfire
}
