package com.sigmundgranaas.forgero.testcommon.builders;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotValidator;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.impl.*;

import java.util.*;
import java.util.function.Predicate;

import static com.sigmundgranaas.forgero.core.attribute.api.Attribute.KEY;

/**
 * Fluent builder for creating test Components with sensible defaults.
 * Simplifies component creation in tests by providing a clean API.
 *
 * <p>Example usage:
 * <pre>{@code
 * // Simple component with attributes
 * Component material = ComponentBuilder.create("iron")
 *     .withAttribute(PropertyFixtures.attackDamage(10))
 *     .build();
 *
 * // Structured component with parts
 * Component head = ComponentBuilder.create("pickaxe_head")
 *     .withType(ToolTypes.PICKAXE_HEAD)
 *     .withPart("material", iron)
 *     .build();
 *
 * // Component with upgrade slots
 * Component tool = ComponentBuilder.create("iron_pickaxe")
 *     .withType(ToolTypes.PICKAXE)
 *     .withPart("head", head)
 *     .withPart("handle", handle)
 *     .withUpgradeSlot(SlotTypes.BINDING)
 *     .build();
 * }</pre>
 */
public class ComponentBuilder {
    private final OpenIdentifier id;
    private final Set<OpenIdentifier> tags = new HashSet<>();
    private final Map<String, List<?>> properties = new HashMap<>();
    private final List<ComponentPart> parts = new ArrayList<>();
    private final List<ComponentUpgradeSlot> upgradeSlots = new ArrayList<>();

    private ComponentBuilder(OpenIdentifier id) {
        this.id = id;
    }

    // ========== Factory Methods ==========

    /**
     * Creates a new component builder with the specified ID.
     *
     * @param id the component identifier (will be prefixed with "forgero:" if no namespace)
     * @return a new ComponentBuilder instance
     */
    public static ComponentBuilder create(String id) {
        return new ComponentBuilder(ensureNamespace(id));
    }

    /**
     * Creates a new component builder with the specified identifier.
     *
     * @param id the component identifier
     * @return a new ComponentBuilder instance
     */
    public static ComponentBuilder create(OpenIdentifier id) {
        return new ComponentBuilder(id);
    }

    // ========== Tag Methods ==========

    /**
     * Adds a tag to this component.
     *
     * @param tag the tag to add (will be prefixed with "forgero:" if no namespace)
     * @return this builder for chaining
     */
    public ComponentBuilder withTag(String tag) {
        this.tags.add(ensureNamespace(tag));
        return this;
    }

    /**
     * Adds a tag to this component.
     *
     * @param tag the tag identifier to add
     * @return this builder for chaining
     */
    public ComponentBuilder withTag(OpenIdentifier tag) {
        this.tags.add(tag);
        return this;
    }

    /**
     * Sets the primary type tag for this component.
     * Convenience method for {@link #withTag(OpenIdentifier)}.
     *
     * @param type the type identifier
     * @return this builder for chaining
     */
    public ComponentBuilder withType(OpenIdentifier type) {
        return withTag(type);
    }

    // ========== Attribute/Property Methods ==========

    /**
     * Adds an attribute to this component.
     *
     * @param attribute the attribute to add
     * @return this builder for chaining
     */
    public ComponentBuilder withAttribute(Attribute attribute) {
        addToPropertyList(KEY.key(), attribute);
        return this;
    }

    /**
     * Adds an attribute with the specified type and value.
     *
     * @param type the attribute type
     * @param value the attribute value
     * @return this builder for chaining
     */
    public ComponentBuilder withAttribute(OpenIdentifier type, float value) {
        return withAttribute(new com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute(
                type,
                value,
                com.sigmundgranaas.forgero.core.condition.api.Condition.ALWAYS_TRUE
        ));
    }

    /**
     * Adds multiple attributes to this component.
     *
     * @param attributes the attributes to add
     * @return this builder for chaining
     */
    public ComponentBuilder withAttributes(Attribute... attributes) {
        for (Attribute attr : attributes) {
            withAttribute(attr);
        }
        return this;
    }

    // ========== Structure Methods ==========

    /**
     * Adds a structural part to this component.
     *
     * @param slotId the slot identifier
     * @param content the component to place in this slot
     * @return this builder for chaining
     */
    public ComponentBuilder withPart(String slotId, Component content) {
        return withPart(slotId, id, content);
    }

    /**
     * Adds a structural part to this component with a specific type.
     *
     * @param slotId the slot identifier
     * @param slotType the type of this slot
     * @param content the component to place in this slot
     * @return this builder for chaining
     */
    public ComponentBuilder withPart(String slotId, OpenIdentifier slotType, Component content) {
        ComponentPart part = new ComponentPart(
                ensureNamespace(slotId),
                slotType,
                "",
                SlotValidator.ACCEPT_ALL,
                content
        );
        this.parts.add(part);
        return this;
    }

    /**
     * Adds a structural part to this component.
     *
     * @param part the component part to add
     * @return this builder for chaining
     */
    public ComponentBuilder withPart(ComponentPart part) {
        this.parts.add(part);
        return this;
    }

    // ========== Upgrade Slot Methods ==========

    /**
     * Adds an empty upgrade slot to this component.
     *
     * @param slotType the type of upgrade this slot accepts
     * @return this builder for chaining
     */
    public ComponentBuilder withUpgradeSlot(OpenIdentifier slotType) {
        ComponentUpgradeSlot slot = new ComponentUpgradeSlot(
                OpenIdentifier.of("slot_" + upgradeSlots.size()),
                slotType,
                "",
                SlotValidator.ACCEPT_ALL,
                Optional.empty()
        );
        this.upgradeSlots.add(slot);
        return this;
    }

    /**
     * Adds an upgrade slot with a filter to this component.
     *
     * @param slotType the type of upgrade this slot accepts
     * @param filter the filter predicate for valid upgrades
     * @return this builder for chaining
     */
    public ComponentBuilder withUpgradeSlot(OpenIdentifier slotType, Predicate<Component> filter) {
        ComponentUpgradeSlot slot = new ComponentUpgradeSlot(
                OpenIdentifier.of("slot_" + upgradeSlots.size()),
                slotType,
                "",
                SlotValidator.custom(filter),
                Optional.empty()
        );
        this.upgradeSlots.add(slot);
        return this;
    }

    /**
     * Adds an upgrade slot with an installed upgrade.
     *
     * @param slotType the type of upgrade this slot accepts
     * @param upgrade the upgrade component to install
     * @return this builder for chaining
     */
    public ComponentBuilder withUpgrade(OpenIdentifier slotType, Component upgrade) {
        ComponentUpgradeSlot slot = new ComponentUpgradeSlot(
                OpenIdentifier.of("slot_" + upgradeSlots.size()),
                slotType,
                "",
                SlotValidator.ACCEPT_ALL,
                Optional.of(upgrade)
        );
        this.upgradeSlots.add(slot);
        return this;
    }

    /**
     * Adds a custom upgrade slot.
     *
     * @param slot the upgrade slot to add
     * @return this builder for chaining
     */
    public ComponentBuilder withUpgradeSlot(ComponentUpgradeSlot slot) {
        this.upgradeSlots.add(slot);
        return this;
    }

    // ========== Build Method ==========

    /**
     * Builds the component based on the configured structure.
     * Automatically selects the appropriate component implementation:
     * - StaticComponent: No structure or upgrades
     * - ExtensiblePart: Only upgrades, no structure
     * - StructuredPart: Only structure, no upgrades
     * - StructuredExtensiblePart: Both structure and upgrades
     *
     * @return the built Component
     */
    public Component build() {
        boolean hasStructure = !parts.isEmpty();
        boolean hasUpgrades = !upgradeSlots.isEmpty();

        if (hasStructure && hasUpgrades) {
            return new StructuredExtensiblePart(
                    id,
                    tags,
                    properties,
                    ComponentStructure.of(parts),
                    ComponentUpgrades.of(upgradeSlots)
            );
        }
        if (hasStructure) {
            return new StructuredPart(
                    id,
                    tags,
                    properties,
                    ComponentStructure.of(parts)
            );
        }
        if (hasUpgrades) {
            return new ExtensiblePart(
                    id,
                    tags,
                    properties,
                    ComponentUpgrades.of(upgradeSlots)
            );
        }
        return new StaticComponent(id, tags, properties);
    }

    // ========== Helper Methods ==========

    @SuppressWarnings("unchecked")
    private <T> void addToPropertyList(String key, T value) {
        if (properties.containsKey(key)) {
            List<T> list = (List<T>) properties.get(key);
            list.add(value);
        } else {
            properties.put(key, new ArrayList<>(List.of(value)));
        }
    }

    private static OpenIdentifier ensureNamespace(String id) {
        return id.contains(":") ? OpenIdentifier.parse(id) : OpenIdentifier.of(id);
    }

    // ========== Common Component Types ==========

    /**
     * Common component type identifiers.
     */
    public static class ComponentTypes {
        public static final OpenIdentifier PART = OpenIdentifier.of("part");
        public static final OpenIdentifier TOOL = OpenIdentifier.of("tool");
        public static final OpenIdentifier WEAPON = OpenIdentifier.of("weapon");
        public static final OpenIdentifier ARMOR = OpenIdentifier.of("armor");
        public static final OpenIdentifier UPGRADE = OpenIdentifier.of("upgrade");
    }

    /**
     * Common tool type identifiers.
     */
    public static class ToolTypes {
        public static final OpenIdentifier PICKAXE = OpenIdentifier.of("pickaxe");
        public static final OpenIdentifier AXE = OpenIdentifier.of("axe");
        public static final OpenIdentifier SHOVEL = OpenIdentifier.of("shovel");
        public static final OpenIdentifier HOE = OpenIdentifier.of("hoe");
        public static final OpenIdentifier SWORD = OpenIdentifier.of("sword");

        public static final OpenIdentifier PICKAXE_HEAD = OpenIdentifier.of("pickaxe_head");
        public static final OpenIdentifier AXE_HEAD = OpenIdentifier.of("axe_head");
        public static final OpenIdentifier SHOVEL_HEAD = OpenIdentifier.of("shovel_head");
        public static final OpenIdentifier HOE_HEAD = OpenIdentifier.of("hoe_head");
        public static final OpenIdentifier SWORD_BLADE = OpenIdentifier.of("sword_blade");

        public static final OpenIdentifier HANDLE = OpenIdentifier.of("handle");
        public static final OpenIdentifier BINDING = OpenIdentifier.of("binding");
    }

    /**
     * Common slot type identifiers.
     */
    public static class SlotTypes {
        public static final OpenIdentifier MATERIAL = OpenIdentifier.of("material");
        public static final OpenIdentifier GEM = OpenIdentifier.of("gem");
        public static final OpenIdentifier BINDING = OpenIdentifier.of("binding");
        public static final OpenIdentifier SCHEMATIC = OpenIdentifier.of("schematic");
    }
}
