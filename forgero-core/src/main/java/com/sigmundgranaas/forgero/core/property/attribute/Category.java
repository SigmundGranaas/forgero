package com.sigmundgranaas.forgero.core.property.attribute;

import java.util.EnumSet;

import com.mojang.serialization.Codec;

public enum Category {
	PASS,
	OFFENSIVE,
	UTILITY,
	DEFENSIVE,
	ALL,
	UNDEFINED;

	public static final Codec<Category> CODEC = Codec.STRING.xmap(
			Category::valueOf,
			Category::name
	);

	public static final EnumSet<Category> UPGRADE_CATEGORIES = EnumSet.of(OFFENSIVE, UTILITY, DEFENSIVE, ALL);
}
