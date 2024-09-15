package com.sigmundgranaas.forgero.handler;

import org.junit.jupiter.api.BeforeAll;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;

public interface Bootstrapped {
	@BeforeAll
	static void bootStrap() {
		SharedConstants.createGameVersion();
		Bootstrap.initialize();
	}
}
