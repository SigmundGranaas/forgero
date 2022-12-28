package com.sigmundgranaas.forgeroforge;

import com.sigmundgranaas.forgero.resource.data.v2.packages.FilePackageLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgero.resource.data.Constant.CORE_PATH;

public class DataPackageSupplierTest {

    @Test
    void testLoadDataPackage() {
        var dataPackage = new FilePackageLoader(CORE_PATH).get();
        Assertions.assertEquals("minecraft-vanilla", dataPackage.name());
        Assertions.assertEquals("minecraft", dataPackage.nameSpace());
    }
}
