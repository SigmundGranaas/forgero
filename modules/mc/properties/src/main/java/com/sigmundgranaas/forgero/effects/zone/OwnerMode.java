package com.sigmundgranaas.forgero.effects.zone;

import java.util.Locale;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

/**
 * Controls how a persistent zone resolves the "source" of its effects once its owner is gone
 * (logged out, dead, or unloaded).
 */
public enum OwnerMode implements StringIdentifiable {
	/**
	 * Use the original owner as the effect source. If the owner is absent, source-dependent
	 * (contextual) effects are skipped, but target-only effects still apply. Default.
	 */
	KEEP_OWNER,
	/**
	 * Each affected entity is treated as its own source (self-inflicted). All effects always run.
	 */
	TARGET_AS_SOURCE,
	/**
	 * The whole zone tick is skipped while the owner is absent (fail-closed).
	 */
	OWNER_REQUIRED;

	public static final Codec<OwnerMode> CODEC = StringIdentifiable.createCodec(OwnerMode::values);

	@Override
	public String asString() {
		return name().toLowerCase(Locale.ROOT);
	}
}
