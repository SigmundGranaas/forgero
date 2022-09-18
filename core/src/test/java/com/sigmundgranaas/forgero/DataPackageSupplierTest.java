package com.sigmundgranaas.forgero;

import com.sigmundgranaas.forgero.resource.data.v2.FilePackageLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgero.resource.data.Constant.CORE_PATH;

public class DataPackageSupplierTest {

    @Test
    void testLoadDataPackage() {
        var dataPackage = new FilePackageLoader(CORE_PATH).get();
        Assertions.assertEquals("core_test", dataPackage.name());
        Assertions.assertEquals("forgero", dataPackage.nameSpace());
    }
}
