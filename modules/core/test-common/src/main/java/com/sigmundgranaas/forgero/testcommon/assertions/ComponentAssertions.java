package com.sigmundgranaas.forgero.testcommon.assertions;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Assertion utilities for Component testing.
 * Provides fluent, readable assertions for component validation.
 *
 * <p>Example usage:
 * <pre>{@code
 * ComponentAssertions.assertThat(component)
 *     .hasId("forgero:iron_pickaxe")
 *     .hasTag("forgero:pickaxe")
 *     .isStructured()
 *     .asStructured()
 *     .hasPartCount(2);
 * }</pre>
 */
public final class ComponentAssertions {

    private ComponentAssertions() {
        // Prevent instantiation
    }

    /**
     * Start a fluent assertion chain on a component.
     *
     * @param component the component to assert on
     * @return a ComponentAssertion instance for fluent assertions
     */
    public static ComponentAssertion assertThat(Component component) {
        return new ComponentAssertion(component);
    }

    /**
     * Fluent assertion API for Component testing.
     */
    public static class ComponentAssertion {
        private final Component component;

        ComponentAssertion(Component component) {
            assertNotNull(component, "Component should not be null");
            this.component = component;
        }

        // ========== Identity Assertions ==========

        /**
         * Asserts that the component has the specified ID.
         *
         * @param expectedId the expected identifier (with or without namespace)
         * @return this assertion for chaining
         */
        public ComponentAssertion hasId(String expectedId) {
            String actualId = component.id().toString();
            String normalizedExpected = expectedId.contains(":") ? expectedId : "forgero:" + expectedId;

            org.junit.jupiter.api.Assertions.assertEquals(normalizedExpected, actualId,
                    "Component should have ID '" + normalizedExpected + "' but has '" + actualId + "'");
            return this;
        }

        /**
         * Asserts that the component has the specified ID.
         *
         * @param expectedId the expected identifier
         * @return this assertion for chaining
         */
        public ComponentAssertion hasId(OpenIdentifier expectedId) {
            org.junit.jupiter.api.Assertions.assertEquals(expectedId, component.id(),
                    "Component should have ID " + expectedId + " but has " + component.id());
            return this;
        }

        /**
         * Asserts that the component's ID contains the specified substring.
         *
         * @param substring the substring to search for
         * @return this assertion for chaining
         */
        public ComponentAssertion idContains(String substring) {
            assertTrue(component.id().toString().contains(substring),
                    "Component ID '" + component.id() + "' should contain '" + substring + "'");
            return this;
        }

        // ========== Tag Assertions ==========

        /**
         * Asserts that the component has the specified tag.
         *
         * @param tagName the tag name (will be prefixed with "forgero:" if no namespace)
         * @return this assertion for chaining
         */
        public ComponentAssertion hasTag(String tagName) {
            OpenIdentifier tag = tagName.contains(":")
                ? OpenIdentifier.parse(tagName)
                : OpenIdentifier.of(tagName);

            assertTrue(component.getTags().contains(tag),
                    "Component should have tag '" + tag + "' but tags are: " + component.getTags());
            return this;
        }

        /**
         * Asserts that the component has the specified tag.
         *
         * @param tag the tag identifier
         * @return this assertion for chaining
         */
        public ComponentAssertion hasTag(OpenIdentifier tag) {
            assertTrue(component.getTags().contains(tag),
                    "Component should have tag " + tag + " but tags are: " + component.getTags());
            return this;
        }

        /**
         * Asserts that the component has all of the specified tags.
         *
         * @param tags the tags that should be present
         * @return this assertion for chaining
         */
        public ComponentAssertion hasTags(String... tags) {
            for (String tag : tags) {
                hasTag(tag);
            }
            return this;
        }

        /**
         * Asserts that the component does not have the specified tag.
         *
         * @param tagName the tag name that should not be present
         * @return this assertion for chaining
         */
        public ComponentAssertion doesNotHaveTag(String tagName) {
            OpenIdentifier tag = tagName.contains(":")
                ? OpenIdentifier.parse(tagName)
                : OpenIdentifier.of(tagName);

            assertFalse(component.getTags().contains(tag),
                    "Component should not have tag '" + tag + "' but tags are: " + component.getTags());
            return this;
        }

        // ========== Type Assertions ==========

        /**
         * Asserts that the component is a structured component.
         *
         * @return this assertion for chaining
         */
        public ComponentAssertion isStructured() {
            assertTrue(component instanceof StructuredComponent,
                    "Component should be a StructuredComponent but is " + component.getClass().getSimpleName());
            return this;
        }

        /**
         * Asserts that the component is a customizable component.
         *
         * @return this assertion for chaining
         */
        public ComponentAssertion isCustomizable() {
            assertTrue(component instanceof CustomizableComponent,
                    "Component should be a CustomizableComponent but is " + component.getClass().getSimpleName());
            return this;
        }

        /**
         * Asserts that the component is not structured.
         *
         * @return this assertion for chaining
         */
        public ComponentAssertion isNotStructured() {
            assertFalse(component instanceof StructuredComponent,
                    "Component should not be a StructuredComponent");
            return this;
        }

        // ========== Property Assertions ==========

        /**
         * Asserts that the component has at least one attribute.
         *
         * @return this assertion for chaining
         */
        public ComponentAssertion hasAttributes() {
            List<Attribute> attributes = component.properties(Attribute.KEY);
            assertFalse(attributes.isEmpty(), "Component should have at least one attribute");
            return this;
        }

        /**
         * Asserts that the component has the specified number of attributes.
         *
         * @param count the expected attribute count
         * @return this assertion for chaining
         */
        public ComponentAssertion hasAttributeCount(int count) {
            List<Attribute> attributes = component.properties(Attribute.KEY);
            int actual = attributes.size();
            org.junit.jupiter.api.Assertions.assertEquals(count, actual,
                    "Component should have " + count + " attributes but has " + actual);
            return this;
        }

        /**
         * Asserts that the component has an attribute of the specified type.
         *
         * @param attributeType the attribute type identifier
         * @return this assertion for chaining
         */
        public ComponentAssertion hasAttributeType(OpenIdentifier attributeType) {
            List<Attribute> attributes = component.properties(Attribute.KEY);

            boolean hasType = attributes.stream()
                    .anyMatch(attr -> attr.type().equals(attributeType));

            assertTrue(hasType,
                    "Component should have attribute of type " + attributeType);
            return this;
        }

        // ========== Conversion Methods ==========

        /**
         * Converts this assertion to a StructuredComponentAssertion.
         * The component must be structured.
         *
         * @return a StructuredComponentAssertion for fluent assertions on structure
         */
        public StructuredComponentAssertion asStructured() {
            isStructured();
            return new StructuredComponentAssertion((StructuredComponent) component);
        }

        /**
         * Converts this assertion to a CustomizableComponentAssertion.
         * The component must be customizable.
         *
         * @return a CustomizableComponentAssertion for fluent assertions on customization
         */
        public CustomizableComponentAssertion asCustomizable() {
            isCustomizable();
            return new CustomizableComponentAssertion((CustomizableComponent) component);
        }

        /**
         * Returns the underlying component.
         *
         * @return the component being asserted on
         */
        public Component get() {
            return component;
        }
    }

    /**
     * Fluent assertion API for structured components.
     */
    public static class StructuredComponentAssertion {
        private final StructuredComponent component;

        StructuredComponentAssertion(StructuredComponent component) {
            this.component = component;
        }

        /**
         * Asserts that the component has the specified number of parts.
         *
         * @param count the expected part count
         * @return this assertion for chaining
         */
        public StructuredComponentAssertion hasPartCount(int count) {
            int actualCount = component.structure().parts().values().size();
            org.junit.jupiter.api.Assertions.assertEquals(count, actualCount,
                    "Component should have " + count + " parts but has " + actualCount);
            return this;
        }

        /**
         * Asserts that the component has at least one part.
         *
         * @return this assertion for chaining
         */
        public StructuredComponentAssertion hasParts() {
            assertFalse(component.structure().parts().values().isEmpty(),
                    "Component should have at least one part");
            return this;
        }

        /**
         * Asserts that the component has a part with the specified ID.
         *
         * @param partId the expected part ID
         * @return this assertion for chaining
         */
        public StructuredComponentAssertion hasPartWithId(String partId) {
            OpenIdentifier id = partId.contains(":")
                ? OpenIdentifier.parse(partId)
                : OpenIdentifier.of(partId);
            boolean hasPart = component.structure().parts().values().stream()
                    .anyMatch(part -> part.content().id().equals(id));

            assertTrue(hasPart,
                    "Component should have part with ID '" + id + "'");
            return this;
        }

        /**
         * Returns the underlying structured component.
         *
         * @return the structured component being asserted on
         */
        public StructuredComponent get() {
            return component;
        }
    }

    /**
     * Fluent assertion API for customizable components.
     */
    public static class CustomizableComponentAssertion {
        private final CustomizableComponent component;

        CustomizableComponentAssertion(CustomizableComponent component) {
            this.component = component;
        }

        /**
         * Asserts that the component has the specified number of upgrade slots.
         *
         * @param count the expected slot count
         * @return this assertion for chaining
         */
        public CustomizableComponentAssertion hasSlotCount(int count) {
            int actualCount = component.upgrades().slots().asList().size();
            org.junit.jupiter.api.Assertions.assertEquals(count, actualCount,
                    "Component should have " + count + " upgrade slots but has " + actualCount);
            return this;
        }

        /**
         * Asserts that the component has at least one upgrade slot.
         *
         * @return this assertion for chaining
         */
        public CustomizableComponentAssertion hasSlots() {
            assertFalse(component.upgrades().slots().asList().isEmpty(),
                    "Component should have at least one upgrade slot");
            return this;
        }

        /**
         * Asserts that the component has an empty slot of the specified type.
         *
         * @param slotType the slot type identifier
         * @return this assertion for chaining
         */
        public CustomizableComponentAssertion hasEmptySlot(OpenIdentifier slotType) {
            boolean hasEmpty = component.getUpgradeSlots().stream()
                    .anyMatch(slot -> slot.slotType().equals(slotType) && slot.content().isEmpty());

            assertTrue(hasEmpty,
                    "Component should have an empty slot of type " + slotType);
            return this;
        }

        /**
         * Asserts that the component has a filled slot of the specified type.
         *
         * @param slotType the slot type identifier
         * @return this assertion for chaining
         */
        public CustomizableComponentAssertion hasFilledSlot(OpenIdentifier slotType) {
            boolean hasFilled = component.getUpgradeSlots().stream()
                    .anyMatch(slot -> slot.slotType().equals(slotType) && slot.content().isPresent());

            assertTrue(hasFilled,
                    "Component should have a filled slot of type " + slotType);
            return this;
        }

        /**
         * Returns the underlying customizable component.
         *
         * @return the customizable component being asserted on
         */
        public CustomizableComponent get() {
            return component;
        }
    }

    // ========== Convenience Assertions ==========

    /**
     * Asserts that two components have the same ID.
     *
     * @param expected the expected component
     * @param actual the actual component
     */
    public static void assertSameId(Component expected, Component actual) {
        assertNotNull(expected, "Expected component should not be null");
        assertNotNull(actual, "Actual component should not be null");
        org.junit.jupiter.api.Assertions.assertEquals(expected.id(), actual.id(),
                "Component IDs should match");
    }

    /**
     * Asserts that a component is not null.
     *
     * @param component the component to check
     * @param message the failure message
     */
    public static void assertNotNull(Component component, String message) {
        org.junit.jupiter.api.Assertions.assertNotNull(component, message);
    }
}
