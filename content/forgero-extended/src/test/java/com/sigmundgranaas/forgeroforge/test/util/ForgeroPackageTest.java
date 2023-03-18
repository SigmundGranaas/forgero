package com.sigmundgranaas.forgeroforge.test.util;

import org.junit.jupiter.api.BeforeAll;

public class ForgeroPackageTest {
	@BeforeAll
	public static void runSetup() {
		ForgeroPipeLineSetup.setup();
	}
}
