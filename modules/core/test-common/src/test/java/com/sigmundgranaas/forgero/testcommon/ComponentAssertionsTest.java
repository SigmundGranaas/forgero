package com.sigmundgranaas.forgero.testcommon;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.testcommon.assertions.ComponentAssertions;
import com.sigmundgranaas.forgero.testcommon.builders.ComponentBuilder;
import com.sigmundgranaas.forgero.testcommon.fixtures.MaterialFixtures;
import com.sigmundgranaas.forgero.testcommon.fixtures.PropertyFixtures;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ComponentAssertions utility class.
 */
class ComponentAssertionsTest {

    @Test
    void assertThat_shouldNotThrowForValidComponent() {
        Component component = ComponentBuilder.create("test").build();

        assertDoesNotThrow(() -> ComponentAssertions.assertThat(component));
    }

    @Test
    void assertThat_shouldThrowForNullComponent() {
        assertThrows(AssertionError.class, () -> ComponentAssertions.assertThat(null));
    }

    @Test
    void hasId_shouldPassForCorrectId() {
        Component component = ComponentBuilder.create("test").build();

        assertDoesNotThrow(() ->
            ComponentAssertions.assertThat(component).hasId("forgero:test")
        );
    }

    @Test
    void hasId_shouldPassWithoutNamespace() {
        Component component = ComponentBuilder.create("test").build();

        assertDoesNotThrow(() ->
            ComponentAssertions.assertThat(component).hasId("test")
        );
    }

    @Test
    void hasId_shouldFailForWrongId() {
        Component component = ComponentBuilder.create("test").build();

        assertThrows(AssertionError.class, () ->
            ComponentAssertions.assertThat(component).hasId("wrong")
        );
    }

    @Test
    void hasTag_shouldPassForExistingTag() {
        Component component = ComponentBuilder.create("test")
            .withTag("pickaxe")
            .build();

        assertDoesNotThrow(() ->
            ComponentAssertions.assertThat(component).hasTag("pickaxe")
        );
    }

    @Test
    void hasTag_shouldFailForMissingTag() {
        Component component = ComponentBuilder.create("test").build();

        assertThrows(AssertionError.class, () ->
            ComponentAssertions.assertThat(component).hasTag("pickaxe")
        );
    }

    @Test
    void hasTags_shouldPassForAllExistingTags() {
        Component component = ComponentBuilder.create("test")
            .withTag("pickaxe")
            .withTag("tool")
            .build();

        assertDoesNotThrow(() ->
            ComponentAssertions.assertThat(component).hasTags("pickaxe", "tool")
        );
    }

    @Test
    void doesNotHaveTag_shouldPassForMissingTag() {
        Component component = ComponentBuilder.create("test").build();

        assertDoesNotThrow(() ->
            ComponentAssertions.assertThat(component).doesNotHaveTag("pickaxe")
        );
    }

    @Test
    void isStructured_shouldPassForStructuredComponent() {
        Component component = ComponentBuilder.create("test")
            .withPart("material", MaterialFixtures.iron())
            .build();

        assertDoesNotThrow(() ->
            ComponentAssertions.assertThat(component).isStructured()
        );
    }

    @Test
    void isStructured_shouldFailForNonStructuredComponent() {
        Component component = ComponentBuilder.create("test").build();

        assertThrows(AssertionError.class, () ->
            ComponentAssertions.assertThat(component).isStructured()
        );
    }

    @Test
    void isCustomizable_shouldPassForCustomizableComponent() {
        Component component = ComponentBuilder.create("test")
            .withUpgradeSlot(ComponentBuilder.SlotTypes.GEM)
            .build();

        assertDoesNotThrow(() ->
            ComponentAssertions.assertThat(component).isCustomizable()
        );
    }

    @Test
    void hasAttributes_shouldPassWhenAttributesExist() {
        Component component = ComponentBuilder.create("test")
            .withAttribute(PropertyFixtures.attackDamage(10.0f))
            .build();

        assertDoesNotThrow(() ->
            ComponentAssertions.assertThat(component).hasAttributes()
        );
    }

    @Test
    void hasAttributes_shouldFailWhenNoAttributes() {
        Component component = ComponentBuilder.create("test").build();

        assertThrows(AssertionError.class, () ->
            ComponentAssertions.assertThat(component).hasAttributes()
        );
    }

    @Test
    void hasAttributeCount_shouldPassForCorrectCount() {
        Component component = ComponentBuilder.create("test")
            .withAttribute(PropertyFixtures.attackDamage(10.0f))
            .withAttribute(PropertyFixtures.durability(100))
            .build();

        assertDoesNotThrow(() ->
            ComponentAssertions.assertThat(component).hasAttributeCount(2)
        );
    }

    @Test
    void hasAttributeType_shouldPassWhenTypeExists() {
        Component component = ComponentBuilder.create("test")
            .withAttribute(PropertyFixtures.attackDamage(10.0f))
            .build();

        assertDoesNotThrow(() ->
            ComponentAssertions.assertThat(component)
                .hasAttributeType(PropertyFixtures.AttributeTypes.ATTACK_DAMAGE)
        );
    }

    @Test
    void asStructured_shouldReturnStructuredAssertion() {
        Component component = ComponentBuilder.create("test")
            .withPart("material", MaterialFixtures.iron())
            .build();

        var structuredAssertion = ComponentAssertions.assertThat(component).asStructured();

        assertNotNull(structuredAssertion);
    }

    @Test
    void asCustomizable_shouldReturnCustomizableAssertion() {
        Component component = ComponentBuilder.create("test")
            .withUpgradeSlot(ComponentBuilder.SlotTypes.GEM)
            .build();

        var customizableAssertion = ComponentAssertions.assertThat(component).asCustomizable();

        assertNotNull(customizableAssertion);
    }

    @Test
    void structuredAssertion_hasPartCount_shouldPassForCorrectCount() {
        Component component = ComponentBuilder.create("test")
            .withPart("head", MaterialFixtures.iron())
            .withPart("handle", MaterialFixtures.oak())
            .build();

        assertDoesNotThrow(() ->
            ComponentAssertions.assertThat(component)
                .asStructured()
                .hasPartCount(2)
        );
    }

    @Test
    void structuredAssertion_hasParts_shouldPassWhenPartsExist() {
        Component component = ComponentBuilder.create("test")
            .withPart("material", MaterialFixtures.iron())
            .build();

        assertDoesNotThrow(() ->
            ComponentAssertions.assertThat(component)
                .asStructured()
                .hasParts()
        );
    }

    @Test
    void structuredAssertion_hasPartWithId_shouldPassForExistingPart() {
        Component iron = MaterialFixtures.iron();
        Component component = ComponentBuilder.create("test")
            .withPart("material", iron)
            .build();

        assertDoesNotThrow(() ->
            ComponentAssertions.assertThat(component)
                .asStructured()
                .hasPartWithId("iron")
        );
    }

    @Test
    void customizableAssertion_hasSlotCount_shouldPassForCorrectCount() {
        Component component = ComponentBuilder.create("test")
            .withUpgradeSlot(ComponentBuilder.SlotTypes.GEM)
            .withUpgradeSlot(ComponentBuilder.SlotTypes.BINDING)
            .build();

        assertDoesNotThrow(() ->
            ComponentAssertions.assertThat(component)
                .asCustomizable()
                .hasSlotCount(2)
        );
    }

    @Test
    void customizableAssertion_hasEmptySlot_shouldPassForEmptySlot() {
        Component component = ComponentBuilder.create("test")
            .withUpgradeSlot(ComponentBuilder.SlotTypes.GEM)
            .build();

        assertDoesNotThrow(() ->
            ComponentAssertions.assertThat(component)
                .asCustomizable()
                .hasEmptySlot(ComponentBuilder.SlotTypes.GEM)
        );
    }

    @Test
    void customizableAssertion_hasFilledSlot_shouldPassForFilledSlot() {
        Component gem = ComponentBuilder.create("ruby").build();
        Component component = ComponentBuilder.create("test")
            .withUpgrade(ComponentBuilder.SlotTypes.GEM, gem)
            .build();

        assertDoesNotThrow(() ->
            ComponentAssertions.assertThat(component)
                .asCustomizable()
                .hasFilledSlot(ComponentBuilder.SlotTypes.GEM)
        );
    }

    @Test
    void fluentChaining_shouldSupportMultipleAssertions() {
        Component component = ComponentBuilder.create("iron_pickaxe_head")
            .withType(ComponentBuilder.ToolTypes.PICKAXE_HEAD)
            .withPart("material", MaterialFixtures.iron())
            .withAttribute(PropertyFixtures.attackDamage(5.0f))
            .withUpgradeSlot(ComponentBuilder.SlotTypes.GEM)
            .build();

        assertDoesNotThrow(() ->
            ComponentAssertions.assertThat(component)
                .hasId("iron_pickaxe_head")
                .hasTag(ComponentBuilder.ToolTypes.PICKAXE_HEAD)
                .hasAttributes()
                .isStructured()
                .isCustomizable()
                .asStructured()
                .hasPartCount(1)
        );
    }

    @Test
    void assertSameId_shouldPassForSameIds() {
        Component comp1 = ComponentBuilder.create("test").build();
        Component comp2 = ComponentBuilder.create("test").build();

        assertDoesNotThrow(() ->
            ComponentAssertions.assertSameId(comp1, comp2)
        );
    }

    @Test
    void assertSameId_shouldFailForDifferentIds() {
        Component comp1 = ComponentBuilder.create("test1").build();
        Component comp2 = ComponentBuilder.create("test2").build();

        assertThrows(AssertionError.class, () ->
            ComponentAssertions.assertSameId(comp1, comp2)
        );
    }
}
