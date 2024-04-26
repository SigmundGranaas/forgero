package com.sigmundgranaas.forgero.test.util;

import org.junit.jupiter.api.BeforeAll;

public class ForgeroPackageTest {
	@BeforeAll
	public static void runSetup() {
		StateHelper.ruleSetup();
		ForgeroPipeLineSetup.setup();
	}
}
