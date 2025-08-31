package com.sigmundgranaas.forgero.tools;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;

import org.junit.jupiter.api.BeforeAll;

public interface Bootstrapped {
	@BeforeAll
	static void bootStrap() {
		SharedConstants.createGameVersion();
		Bootstrap.initialize();
	}
}
