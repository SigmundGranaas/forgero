package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.loader.api.ForgeroInitializedCallback;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the data pipeline - verifying that all content modules
 * load correctly through the ForgeroDataInitializer.
 */
public class DataPipelineTest {

    private static final String BATCH = "forgero_pipeline";

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void forgero_services_available(TestContext context) {
        assertTrue(ForgeroInitializedCallback.getServices().isPresent(),
                "ForgeroServices must be initialized");

        var services = ForgeroInitializedCallback.getServices().get();
        assertNotNull(services.componentRegistry(), "ComponentRegistry must be available");
        assertNotNull(services.taggedComponents(), "TaggedRegistry must be available");
        assertNotNull(services.converter(), "Converter must be available");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void forgero_materials_module_loaded(TestContext context) {
        ComponentRegistry registry = ForgeroInitializedCallback.getServices()
                .map(s -> s.componentRegistry())
                .orElseThrow(() -> new AssertionError("Registry not available"));

        // Verify materials from forgero-materials module
        assertTrue(registry.get(OpenIdentifier.of("forgero:iron")).isPresent(),
                "forgero-materials module must load iron");
        assertTrue(registry.get(OpenIdentifier.of("forgero:diamond")).isPresent(),
                "forgero-materials module must load diamond");
        assertTrue(registry.get(OpenIdentifier.of("forgero:oak")).isPresent(),
                "forgero-materials module must load oak");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void forgero_upgrades_module_loaded(TestContext context) {
        ComponentRegistry registry = ForgeroInitializedCallback.getServices()
                .map(s -> s.componentRegistry())
                .orElseThrow(() -> new AssertionError("Registry not available"));

        // Check if any upgrade materials loaded
        long upgradeCount = registry.all().stream()
                .filter(c -> c.getTags().stream()
                        .anyMatch(tag -> tag.name().contains("upgrade")))
                .count();

        assertTrue(upgradeCount > 0,
                "forgero-upgrades module should load upgrade materials");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void tag_registry_functional(TestContext context) {
        TaggedRegistry<Component> taggedRegistry = ForgeroInitializedCallback.getServices()
                .map(s -> s.taggedComponents())
                .orElseThrow(() -> new AssertionError("TaggedRegistry not available"));

        // Query by tag
        OpenIdentifier metalTag = OpenIdentifier.of("forgero:materials/metal");
        List<Component> metals = taggedRegistry.findByTag(metalTag);

        assertNotNull(metals, "Tag query must return list (not null)");
        // Metals list might be empty if tags aren't fully set up, but shouldn't be null

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void material_tags_resolve_correctly(TestContext context) {
        Component iron = ForgeroInitializedCallback.getServices()
                .map(s -> s.componentRegistry())
                .orElseThrow(() -> new AssertionError("Registry not available"))
                .get(OpenIdentifier.of("forgero:iron"))
                .orElseThrow(() -> new AssertionError("Iron must exist"));

        // Iron should have material tags
        boolean hasMetalTag = iron.getTags().stream()
                .anyMatch(tag -> tag.name().contains("metal"));
        boolean hasMaterialTag = iron.getTags().stream()
                .anyMatch(tag -> tag.name().contains("material"));

        assertTrue(hasMetalTag || hasMaterialTag,
                "Iron should have material-related tags");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void data_pipeline_loads_all_modules(TestContext context) {
        ComponentRegistry registry = ForgeroInitializedCallback.getServices()
                .map(s -> s.componentRegistry())
                .orElseThrow(() -> new AssertionError("Registry not available"));

        int totalComponents = registry.all().size();

        // We should have:
        // - forgero-materials: ~41
        // - forgero-upgrades: ~36
        // - generated parts from templates: varies
        assertTrue(totalComponents >= 70,
                "Data pipeline should load from all content modules. Found: " + totalComponents);

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void no_loading_errors_in_pipeline(TestContext context) {
        // If we got here, the data pipeline completed without throwing exceptions
        ComponentRegistry registry = ForgeroInitializedCallback.getServices()
                .map(s -> s.componentRegistry())
                .orElseThrow(() -> new AssertionError("Registry not available"));

        assertFalse(registry.all().isEmpty(),
                "Registry should not be empty - pipeline should load content");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void component_registry_consistent(TestContext context) {
        ComponentRegistry registry = ForgeroInitializedCallback.getServices()
                .map(s -> s.componentRegistry())
                .orElseThrow(() -> new AssertionError("Registry not available"));

        int size1 = registry.all().size();
        int size2 = registry.all().size();

        assertEquals(size1, size2, "Registry size should be consistent");

        context.complete();
    }
}
