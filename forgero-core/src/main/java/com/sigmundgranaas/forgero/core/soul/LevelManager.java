package com.sigmundgranaas.forgero.core.soul;

import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;

public class LevelManager {
	public int getXpForLevel(int i) {
		int xp = ForgeroConfigurationLoader.configuration.baseSoulLevelRequirement;
		for (int j = 0; j < i; j++) {
			xp = xp * 2;
		}
		return xp;
	}
}
