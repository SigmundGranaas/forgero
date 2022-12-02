package com.sigmundgranaas.forgero;

import com.sigmundgranaas.forgero.resource.data.DataPool;
import com.sigmundgranaas.forgero.resource.data.v2.FilePackageLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.sigmundgranaas.forgero.resource.data.Constant.CORE_PATH;

public class DataPoolTest {

    @Test
    void testCreateResources() {
        var dataPackage = new FilePackageLoader(CORE_PATH).get();
        var data = new DataPool(List.of(dataPackage)).assemble();
        Assertions.assertEquals("MINERAL", data.resources().get(0).type());
        Assertions.assertEquals("forgero", data.resources().get(0).nameSpace());
    }
}
