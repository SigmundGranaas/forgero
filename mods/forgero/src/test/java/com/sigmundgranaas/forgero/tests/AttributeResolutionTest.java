package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.loader.api.ForgeroInitializedCallback;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AttributeResolutionTest {

    private static final String BATCH = "forgero_attributes";

    private static ComponentRegistry getRegistry() {
        return ForgeroInitializedCallback.getServices()
                .map(s -> s.componentRegistry())
                .orElseThrow(() -> new AssertionError("ComponentRegistry not available"));
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void materials_have_durability_attribute(TestContext context) {
        Component iron = getRegistry().get(OpenIdentifier.of("forgero:iron"))
                .orElseThrow(() -> new AssertionError("Iron must exist"));

        List<? extends Attribute> attributes = iron.properties(Attribute.KEY);
        boolean hasDurability = attributes.stream()
                .anyMatch(attr -> attr.type().name().contains("durability"));

        assertTrue(hasDurability, "Iron must have durability attribute");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void attributes_have_valid_values(TestContext context) {
        var allComponents = getRegistry().all();

        for (Component component : allComponents) {
            List<? extends Attribute> attributes = component.properties(Attribute.KEY);
            for (Attribute attr : attributes) {
                assertNotNull(attr.type(), "Attribute must have type");
                assertNotNull(attr, "Attribute must not be null");
            }
        }
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void attributes_have_namespaced_types(TestContext context) {
        var allComponents = getRegistry().all();

        for (Component component : allComponents) {
            List<? extends Attribute> attributes = component.properties(Attribute.KEY);
            for (Attribute attr : attributes) {
                OpenIdentifier type = attr.type();
                assertFalse(type.name().isEmpty(), "Attribute type name must not be empty");
                assertFalse(type.namespace().isEmpty(), "Attribute type namespace must not be empty");
            }
        }
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void conditional_attributes_exist(TestContext context) {
        Component iron = getRegistry().get(OpenIdentifier.of("forgero:iron"))
                .orElseThrow(() -> new AssertionError("Iron must exist"));

        List<? extends Attribute> attributes = iron.properties(Attribute.KEY);
        assertFalse(attributes.isEmpty(), "Iron should have attributes");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void no_null_attributes_in_any_component(TestContext context) {
        var allComponents = getRegistry().all();

        for (Component component : allComponents) {
            List<? extends Attribute> attributes = component.properties(Attribute.KEY);
            assertNotNull(attributes, "Attributes list must not be null");
            assertFalse(attributes.contains(null), "Attributes list must not contain null");
        }
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void material_attributes_follow_naming_convention(TestContext context) {
        Component iron = getRegistry().get(OpenIdentifier.of("forgero:iron"))
                .orElseThrow(() -> new AssertionError("Iron must exist"));

        List<? extends Attribute> attributes = iron.properties(Attribute.KEY);

        for (Attribute attr : attributes) {
            assertEquals("forgero", attr.type().namespace(),
                    "Attribute type should be in forgero namespace: " + attr.type());
        }
        context.complete();
    }
}
