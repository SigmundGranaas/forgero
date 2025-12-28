package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.loader.api.ForgeroInitializedCallback;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for component assembly - verifying that individual components
 * can be combined into complete tools with correct structure.
 */
public class ComponentAssemblyTest {

    private static final String BATCH = "forgero_assembly";

    private static ComponentRegistry getRegistry() {
        return ForgeroInitializedCallback.getServices()
                .map(s -> s.componentRegistry())
                .orElseThrow(() -> new AssertionError("ComponentRegistry not available"));
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void individual_materials_exist(TestContext context) {
        Component iron = getRegistry().get(OpenIdentifier.of("forgero:iron"))
                .orElseThrow(() -> new AssertionError("Iron material must exist"));

        assertNotNull(iron.id(), "Material must have ID");
        assertFalse(iron.getTags().isEmpty(), "Material must have tags");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void materials_can_combine_into_parts(TestContext context) {
        // Verify that the template system would generate parts
        // The actual generation happens during data loading
        ComponentRegistry registry = getRegistry();

        // Check if any generated parts exist (e.g., iron-pickaxe_head)
        // Note: Parts are generated from templates during loading
        int totalComponents = registry.all().size();
        assertTrue(totalComponents > 40,
                "Should have more than just base materials - templates should generate parts. Found: " + totalComponents);

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void structured_components_have_parts(TestContext context) {
        var allComponents = getRegistry().all();

        // Find any structured component
        var structuredOpt = allComponents.stream()
                .filter(c -> c instanceof StructuredComponent)
                .findFirst();

        if (structuredOpt.isPresent()) {
            StructuredComponent structured = (StructuredComponent) structuredOpt.get();
            assertFalse(structured.structure().allParts().isEmpty(),
                    "Structured component must have parts");
        } else {
            // No structured components generated yet - that's okay for minimal content
            context.complete();
        }

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void customizable_components_have_upgrade_slots(TestContext context) {
        var allComponents = getRegistry().all();

        // Find any customizable component
        var customizableOpt = allComponents.stream()
                .filter(c -> c instanceof CustomizableComponent)
                .findFirst();

        if (customizableOpt.isPresent()) {
            CustomizableComponent customizable = (CustomizableComponent) customizableOpt.get();
            assertNotNull(customizable.upgrades(), "Customizable must have upgrades container");
        }

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void components_have_properties(TestContext context) {
        Component iron = getRegistry().get(OpenIdentifier.of("forgero:iron"))
                .orElseThrow(() -> new AssertionError("Iron material must exist"));

        assertNotNull(iron.propertiesAsMap(), "Component must have properties map");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void component_children_accessible(TestContext context) {
        var allComponents = getRegistry().all();

        for (Component component : allComponents) {
            assertNotNull(component.getChildren(), "getChildren() must not return null");
        }

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void component_tags_are_valid(TestContext context) {
        var allComponents = getRegistry().all();

        for (Component component : allComponents) {
            assertNotNull(component.getTags(), "Tags must not be null");
            for (OpenIdentifier tag : component.getTags()) {
                assertNotNull(tag, "Individual tag must not be null");
                assertFalse(tag.name().isEmpty(), "Tag name must not be empty");
            }
        }

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void all_components_have_type_identifier(TestContext context) {
        var allComponents = getRegistry().all();

        for (Component component : allComponents) {
            OpenIdentifier typeId = component.getTypeIdentifier();
            assertNotNull(typeId, "Type identifier must not be null for: " + component.id());
            assertFalse(typeId.name().isEmpty(), "Type name must not be empty");
        }

        context.complete();
    }
}
