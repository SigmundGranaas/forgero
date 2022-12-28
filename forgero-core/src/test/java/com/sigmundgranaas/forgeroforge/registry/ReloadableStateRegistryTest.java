package com.sigmundgranaas.forgeroforge.registry;

import com.sigmundgranaas.forgero.registry.impl.ReloadableStateRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgeroforge.testutil.Materials.IRON;
import static com.sigmundgranaas.forgeroforge.testutil.ToolParts.HANDLE;

class ReloadableStateRegistryTest {
    @Test
    void successfullyRegisterNewState() {
        var registry = new ReloadableStateRegistry();
        var supplier = registry.register(HANDLE);
        var entryOpt = registry.find(HANDLE);

        Assertions.assertTrue(entryOpt.isPresent());
        Assertions.assertEquals(supplier, entryOpt.get());
    }

    @Test
    void successfullyUpdateExistingState() {
        var upgrade = HANDLE.toBuilder().addUpgrade(IRON).build();
        var registry = new ReloadableStateRegistry();
        var supplier = registry.register(HANDLE);
        var upgradeSupplier = registry.update(upgrade);
        var entryOpt = registry.find(HANDLE);


        Assertions.assertTrue(entryOpt.isPresent());
        Assertions.assertEquals(supplier, entryOpt.get());
        Assertions.assertEquals(upgradeSupplier, supplier);
        Assertions.assertEquals(supplier.get(), upgrade);
    }
}