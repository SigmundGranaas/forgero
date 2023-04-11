package com.sigmundgranaas.forgeroforge;

import com.sigmundgranaas.forgero.core.resource.data.v2.packages.FilePackageLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgero.core.resource.data.Constant.CORE_PATH;

public class DataPackageSupplierTest {

	@Test
	void testLoadDataPackage() {
		var dataPackage = new FilePackageLoader(CORE_PATH + "minecraft-vanilla").get();
		Assertions.assertEquals("minecraft-vanilla", dataPackage.name());
		Assertions.assertEquals("minecraft", dataPackage.nameSpace());
	}
}
