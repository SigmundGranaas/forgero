package com.sigmundgranaas.forgero.mc.testcommon.gametest;

import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import com.sigmundgranaas.forgero.common.api.ForgeroServices;
import net.minecraft.test.TestContext;

public final class ForgeroTestUtils {
    private ForgeroTestUtils() {}

    public static ForgeroServices services() {
        return ForgeroApi.services();
    }

    public static ForgeroTestContext forgero(TestContext context) {
        return new ForgeroTestContext(context);
    }
}
