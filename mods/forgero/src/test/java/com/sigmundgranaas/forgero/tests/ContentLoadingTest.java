package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.loader.api.ForgeroInitializedCallback;
import com.sigmundgranaas.forgero.loader.api.ForgeroServices;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import static org.junit.jupiter.api.Assertions.*;

public class ContentLoadingTest {

    private static final String BATCH = "forgero_content";

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void forgero_services_available(TestContext context) {
        assertTrue(ForgeroInitializedCallback.getServices().isPresent(),
                "ForgeroServices must be initialized");

        ForgeroServices services = ForgeroInitializedCallback.getServices().get();
        assertNotNull(services.componentRegistry(), "ComponentRegistry must be available");
        assertNotNull(services.taggedComponents(), "TaggedRegistry must be available");
        assertNotNull(services.converter(), "Converter must be available");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void materials_module_loads(TestContext context) {
        ComponentRegistry registry = ForgeroInitializedCallback.getServices()
                .map(ForgeroServices::componentRegistry)
                .orElseThrow(() -> new AssertionError("Registry not available"));

        int materialCount = registry.all().size();
        assertTrue(materialCount >= 40,
                "Expected at least 40 materials from forgero-materials, found: " + materialCount);

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void data_pipeline_initialized(TestContext context) {
        ComponentRegistry registry = ForgeroInitializedCallback.getServices()
                .map(ForgeroServices::componentRegistry)
                .orElseThrow(() -> new AssertionError("Registry not available"));

        assertFalse(registry.all().isEmpty(),
                "No components loaded - data pipeline may have failed");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void new_content_structure_working(TestContext context) {
        ComponentRegistry registry = ForgeroInitializedCallback.getServices()
                .map(ForgeroServices::componentRegistry)
                .orElseThrow(() -> new AssertionError("Registry not available"));

        boolean hasContent = !registry.all().isEmpty();
        assertTrue(hasContent,
                "New content structure is not loading - no components found");

        context.complete();
    }
}
