package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.loader.api.ForgeroInitializedCallback;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for template generation - verifying that templates
 * generate component permutations correctly.
 */
public class TemplateGenerationTest {

    private static final String BATCH = "forgero_templates";

    private static ComponentRegistry getRegistry() {
        return ForgeroInitializedCallback.getServices()
                .map(s -> s.componentRegistry())
                .orElseThrow(() -> new AssertionError("ComponentRegistry not available"));
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void template_system_generates_components(TestContext context) {
        int componentCount = getRegistry().all().size();

        // Base materials: ~40
        // Upgrade materials: ~36
        // If templates are working, we should have more than just materials
        assertTrue(componentCount > 70,
                "Template system should generate additional components beyond base materials. Found: " + componentCount);

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void generated_components_have_valid_ids(TestContext context) {
        var allComponents = getRegistry().all();

        for (Component component : allComponents) {
            OpenIdentifier id = component.id();
            assertNotNull(id, "Generated component must have ID");
            assertFalse(id.name().isEmpty(), "ID name must not be empty");
            assertFalse(id.nameSpace().isEmpty(), "ID namespace must not be empty");
        }

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void generated_components_follow_naming_pattern(TestContext context) {
        var allComponents = getRegistry().all();

        // Generated components should follow {material}-{part} or just {material} pattern
        for (Component component : allComponents) {
            String name = component.id().name();
            assertFalse(name.contains("null"), "Component name should not contain 'null'");
            assertFalse(name.contains("undefined"), "Component name should not contain 'undefined'");
        }

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void no_duplicate_component_ids(TestContext context) {
        var allComponents = getRegistry().all();
        long uniqueCount = allComponents.stream()
                .map(Component::id)
                .distinct()
                .count();

        assertEquals(allComponents.size(), uniqueCount,
                "All generated components must have unique IDs");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void all_generated_components_have_tags(TestContext context) {
        var allComponents = getRegistry().all();

        for (Component component : allComponents) {
            assertNotNull(component.getTags(),
                    "Generated component must have tags: " + component.id());
            // Tags can be empty, but must not be null
        }

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void template_generation_produces_consistent_results(TestContext context) {
        // Verify that getting the same component multiple times returns equivalent objects
        var ironOpt = getRegistry().get(OpenIdentifier.of("forgero:iron"));

        assertTrue(ironOpt.isPresent(), "Iron should be consistently available");

        Component iron1 = ironOpt.get();
        Component iron2 = getRegistry().get(OpenIdentifier.of("forgero:iron")).get();

        assertEquals(iron1.id(), iron2.id(), "Same component should have same ID");
        assertEquals(iron1.getTags(), iron2.getTags(), "Same component should have same tags");

        context.complete();
    }
}
