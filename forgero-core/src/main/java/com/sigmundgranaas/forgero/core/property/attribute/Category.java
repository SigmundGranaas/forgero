package com.sigmundgranaas.forgero.core.property.attribute;

import java.util.EnumSet;

public enum Category {
	OFFENSIVE,
	UTILITY,
	DEFENSIVE,
	ALL,
	UNDEFINED;

	public static final EnumSet<Category> UPGRADE_CATEGORIES = EnumSet.of(OFFENSIVE, UTILITY, DEFENSIVE, ALL);
}
