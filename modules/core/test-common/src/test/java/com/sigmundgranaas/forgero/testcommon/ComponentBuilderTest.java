package com.sigmundgranaas.forgero.testcommon;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.testcommon.builders.ComponentBuilder;
import com.sigmundgranaas.forgero.testcommon.fixtures.MaterialFixtures;
import com.sigmundgranaas.forgero.testcommon.fixtures.PropertyFixtures;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ComponentBuilder utility class.
 */
class ComponentBuilderTest {

    @Test
    void create_shouldCreateBuilderWithNamespacedId() {
        Component component = ComponentBuilder.create("test").build();

        assertEquals(OpenIdentifier.of("test"), component.id());
    }

    @Test
    void create_shouldPreserveNamespace() {
        Component component = ComponentBuilder.create("custom:test").build();

        assertEquals(OpenIdentifier.parse("custom:test"), component.id());
    }

    @Test
    void withTag_shouldAddTag() {
        Component component = ComponentBuilder.create("test")
            .withTag("pickaxe")
            .build();

        assertTrue(component.getTags().contains(OpenIdentifier.of("pickaxe")));
    }

    @Test
    void withType_shouldAddTypeTag() {
        Component component = ComponentBuilder.create("test")
            .withType(ComponentBuilder.ToolTypes.PICKAXE)
            .build();

        assertTrue(component.getTags().contains(ComponentBuilder.ToolTypes.PICKAXE));
    }

    @Test
    void withAttribute_shouldAddAttribute() {
        Component component = ComponentBuilder.create("test")
            .withAttribute(PropertyFixtures.attackDamage(10.0f))
            .build();

        var attributes = component.properties(Attribute.KEY);
        assertEquals(1, attributes.size());
        assertEquals(10.0f, attributes.get(0).value(), 0.001f);
    }

    @Test
    void withAttributes_shouldAddMultipleAttributes() {
        Component component = ComponentBuilder.create("test")
            .withAttributes(
                PropertyFixtures.attackDamage(10.0f),
                PropertyFixtures.durability(100)
            )
            .build();

        var attributes = component.properties(Attribute.KEY);
        assertEquals(2, attributes.size());
    }

    @Test
    void build_shouldCreateStaticComponentWhenNoStructureOrUpgrades() {
        Component component = ComponentBuilder.create("test")
            .withAttribute(PropertyFixtures.attackDamage(5.0f))
            .build();

        assertFalse(component instanceof StructuredComponent);
        assertFalse(component instanceof CustomizableComponent);
    }

    @Test
    void build_shouldCreateStructuredComponentWhenHasParts() {
        Component iron = MaterialFixtures.iron();

        Component component = ComponentBuilder.create("test")
            .withPart("material", iron)
            .build();

        assertTrue(component instanceof StructuredComponent);
        assertFalse(component instanceof CustomizableComponent);
    }

    @Test
    void build_shouldCreateExtensibleComponentWhenHasSlots() {
        Component component = ComponentBuilder.create("test")
            .withUpgradeSlot(ComponentBuilder.SlotTypes.GEM)
            .build();

        assertFalse(component instanceof StructuredComponent);
        assertTrue(component instanceof CustomizableComponent);
    }

    @Test
    void build_shouldCreateStructuredExtensibleComponentWhenHasBoth() {
        Component iron = MaterialFixtures.iron();

        Component component = ComponentBuilder.create("test")
            .withPart("material", iron)
            .withUpgradeSlot(ComponentBuilder.SlotTypes.GEM)
            .build();

        assertTrue(component instanceof StructuredComponent);
        assertTrue(component instanceof CustomizableComponent);
    }

    @Test
    void withPart_shouldAddPartToStructure() {
        Component iron = MaterialFixtures.iron();

        Component component = ComponentBuilder.create("test")
            .withPart("head", iron)
            .build();

        StructuredComponent structured = (StructuredComponent) component;
        assertEquals(1, structured.structure().parts().size());
        assertTrue(structured.structure().parts().containsKey(OpenIdentifier.of("head")));
    }

    @Test
    void withUpgradeSlot_shouldAddSlot() {
        Component component = ComponentBuilder.create("test")
            .withUpgradeSlot(ComponentBuilder.SlotTypes.GEM)
            .build();

        CustomizableComponent customizable = (CustomizableComponent) component;
        assertEquals(1, customizable.getUpgradeSlots().size());
    }

    @Test
    void withUpgrade_shouldAddFilledSlot() {
        Component gem = ComponentBuilder.create("ruby")
            .withAttribute(PropertyFixtures.attackDamage(2.0f))
            .build();

        Component component = ComponentBuilder.create("test")
            .withUpgrade(ComponentBuilder.SlotTypes.GEM, gem)
            .build();

        CustomizableComponent customizable = (CustomizableComponent) component;
        assertEquals(1, customizable.getUpgradeSlots().size());
        assertTrue(customizable.getUpgradeSlots().get(0).content().isPresent());
    }

    @Test
    void complexComponent_shouldSupportChaining() {
        Component iron = MaterialFixtures.iron();
        Component gem = ComponentBuilder.create("ruby")
            .withAttribute(PropertyFixtures.attackDamage(2.0f))
            .build();

        Component component = ComponentBuilder.create("iron_pickaxe_head")
            .withType(ComponentBuilder.ToolTypes.PICKAXE_HEAD)
            .withPart("material", iron)
            .withAttribute(PropertyFixtures.attackDamage(5.0f))
            .withUpgradeSlot(ComponentBuilder.SlotTypes.GEM)
            .withUpgrade(ComponentBuilder.SlotTypes.BINDING, gem)
            .build();

        assertTrue(component.getTags().contains(ComponentBuilder.ToolTypes.PICKAXE_HEAD));
        assertTrue(component instanceof StructuredComponent);
        assertTrue(component instanceof CustomizableComponent);

        StructuredComponent structured = (StructuredComponent) component;
        CustomizableComponent customizable = (CustomizableComponent) component;

        assertEquals(1, structured.structure().parts().size());
        assertEquals(2, customizable.getUpgradeSlots().size());
    }
}
