package com.sigmundgranaas.forgeroforge;

import static com.sigmundgranaas.forgero.core.resource.data.Constant.CORE_PATH;

import com.sigmundgranaas.forgero.core.resource.data.v2.packages.FilePackageLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DataPackageSupplierTest {

	@Test
	void testLoadDataPackage() {
		var dataPackage = new FilePackageLoader(CORE_PATH + "minecraft-material").get();
		Assertions.assertEquals("minecraft-material", dataPackage.name());
		Assertions.assertEquals("minecraft", dataPackage.nameSpace());
	}
}
