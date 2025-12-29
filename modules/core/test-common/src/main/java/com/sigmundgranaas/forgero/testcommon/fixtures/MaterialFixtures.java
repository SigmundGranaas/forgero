package com.sigmundgranaas.forgero.testcommon.fixtures;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.impl.StaticComponent;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Pre-built material component fixtures for testing.
 * Provides factory methods for creating common material components with realistic attributes.
 *
 * <p>Example usage:
 * <pre>{@code
 * Component iron = MaterialFixtures.iron();
 * Component diamond = MaterialFixtures.diamond();
 * Component custom = MaterialFixtures.material("mythril",
 *     PropertyFixtures.attackDamage(12.0f),
 *     PropertyFixtures.durability(3000));
 * }</pre>
 */
public final class MaterialFixtures {

    private MaterialFixtures() {
        // Prevent instantiation
    }

    // ========== Common Material Types ==========

    /**
     * Material type identifiers.
     */
    public static class MaterialTypes {
        public static final OpenIdentifier WOOD = OpenIdentifier.of("wood");
        public static final OpenIdentifier STONE = OpenIdentifier.of("stone");
        public static final OpenIdentifier METAL = OpenIdentifier.of("metal");
        public static final OpenIdentifier GEM = OpenIdentifier.of("gem");
        public static final OpenIdentifier MATERIAL = OpenIdentifier.of("material");
    }

    // ========== Wood Materials ==========

    /**
     * Creates an oak wood material component.
     * Attack damage: 1.0, Mining speed: 2.0, Durability: 59
     */
    public static Component oak() {
        return material("oak", MaterialTypes.WOOD,
                PropertyFixtures.woodAttackDamage(),
                PropertyFixtures.woodMiningSpeed(),
                PropertyFixtures.woodDurability()
        );
    }

    /**
     * Creates a spruce wood material component.
     * Attack damage: 1.0, Mining speed: 2.0, Durability: 59
     */
    public static Component spruce() {
        return material("spruce", MaterialTypes.WOOD,
                PropertyFixtures.woodAttackDamage(),
                PropertyFixtures.woodMiningSpeed(),
                PropertyFixtures.woodDurability()
        );
    }

    /**
     * Creates a birch wood material component.
     * Attack damage: 1.0, Mining speed: 2.0, Durability: 59
     */
    public static Component birch() {
        return material("birch", MaterialTypes.WOOD,
                PropertyFixtures.woodAttackDamage(),
                PropertyFixtures.woodMiningSpeed(),
                PropertyFixtures.woodDurability()
        );
    }

    // ========== Stone Materials ==========

    /**
     * Creates a stone material component.
     * Attack damage: 3.0, Mining speed: 4.0, Durability: 131, Mining level: 1
     */
    public static Component stone() {
        return material("stone", MaterialTypes.STONE,
                PropertyFixtures.stoneAttackDamage(),
                PropertyFixtures.stoneMiningSpeed(),
                PropertyFixtures.stoneDurability(),
                PropertyFixtures.miningLevel(1)
        );
    }

    /**
     * Creates a cobblestone material component.
     * Attack damage: 3.0, Mining speed: 4.0, Durability: 131, Mining level: 1
     */
    public static Component cobblestone() {
        return material("cobblestone", MaterialTypes.STONE,
                PropertyFixtures.stoneAttackDamage(),
                PropertyFixtures.stoneMiningSpeed(),
                PropertyFixtures.stoneDurability(),
                PropertyFixtures.miningLevel(1)
        );
    }

    // ========== Metal Materials ==========

    /**
     * Creates an iron material component.
     * Attack damage: 6.0, Mining speed: 6.0, Durability: 250, Mining level: 2
     */
    public static Component iron() {
        return material("iron", MaterialTypes.METAL,
                PropertyFixtures.ironAttackDamage(),
                PropertyFixtures.ironMiningSpeed(),
                PropertyFixtures.ironDurability(),
                PropertyFixtures.miningLevel(2)
        );
    }

    /**
     * Creates a gold material component.
     * Attack damage: 6.0, Mining speed: 12.0, Durability: 32, Mining level: 0
     */
    public static Component gold() {
        return material("gold", MaterialTypes.METAL,
                PropertyFixtures.attackDamage(6.0f),
                PropertyFixtures.miningSpeed(12.0f),
                PropertyFixtures.durability(32),
                PropertyFixtures.miningLevel(0)
        );
    }

    // ========== Gem Materials ==========

    /**
     * Creates a diamond material component.
     * Attack damage: 8.0, Mining speed: 8.0, Durability: 1561, Mining level: 3
     */
    public static Component diamond() {
        return material("diamond", MaterialTypes.GEM,
                PropertyFixtures.diamondAttackDamage(),
                PropertyFixtures.diamondMiningSpeed(),
                PropertyFixtures.diamondDurability(),
                PropertyFixtures.miningLevel(3)
        );
    }

    /**
     * Creates an emerald material component.
     * Attack damage: 8.0, Mining speed: 8.0, Durability: 1561, Mining level: 3
     */
    public static Component emerald() {
        return material("emerald", MaterialTypes.GEM,
                PropertyFixtures.attackDamage(8.0f),
                PropertyFixtures.miningSpeed(8.0f),
                PropertyFixtures.durability(1561),
                PropertyFixtures.miningLevel(3)
        );
    }

    // ========== Advanced Materials ==========

    /**
     * Creates a netherite material component.
     * Attack damage: 10.0, Mining speed: 9.0, Durability: 2031, Mining level: 4
     */
    public static Component netherite() {
        return material("netherite", MaterialTypes.MATERIAL,
                PropertyFixtures.netheriteAttackDamage(),
                PropertyFixtures.netheriteMiningSpeed(),
                PropertyFixtures.netheriteDurability(),
                PropertyFixtures.miningLevel(4)
        );
    }

    // ========== Factory Methods ==========

    /**
     * Creates a material component with the specified name and attributes.
     *
     * @param name the material name
     * @param attributes the attributes for this material
     * @return a Component representing the material
     */
    public static Component material(String name, Attribute... attributes) {
        return material(name, MaterialTypes.MATERIAL, attributes);
    }

    /**
     * Creates a material component with the specified name, type, and attributes.
     *
     * @param name the material name
     * @param type the material type tag
     * @param attributes the attributes for this material
     * @return a Component representing the material
     */
    public static Component material(String name, OpenIdentifier type, Attribute... attributes) {
        OpenIdentifier id = OpenIdentifier.of(name);

        // Convert attributes to property map
        Map<String, List<?>> properties = Map.of(
                Attribute.KEY.key(), List.of(attributes)
        );

        return new StaticComponent(id, Set.of(type), properties);
    }

    /**
     * Creates a material component with full customization.
     *
     * @param id the component identifier
     * @param tags the tags for this material
     * @param attributes the attributes for this material
     * @return a Component representing the material
     */
    public static Component customMaterial(OpenIdentifier id, Set<OpenIdentifier> tags, Attribute... attributes) {
        Map<String, List<?>> properties = Map.of(
                Attribute.KEY.key(), List.of(attributes)
        );

        return new StaticComponent(id, tags, properties);
    }

    /**
     * Material builder for fluent component creation.
     */
    public static class MaterialBuilder {
        private String name;
        private String namespace = "forgero";
        private OpenIdentifier type = MaterialTypes.MATERIAL;
        private final Set<OpenIdentifier> additionalTags = Set.of();
        private final List<Attribute> attributes = new java.util.ArrayList<>();

        public MaterialBuilder() {
        }

        /**
         * Sets the material name.
         */
        public MaterialBuilder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the namespace for the identifier.
         */
        public MaterialBuilder namespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        /**
         * Sets the material type tag.
         */
        public MaterialBuilder type(OpenIdentifier type) {
            this.type = type;
            return this;
        }

        /**
         * Adds an attribute to this material.
         */
        public MaterialBuilder attribute(Attribute attribute) {
            this.attributes.add(attribute);
            return this;
        }

        /**
         * Adds multiple attributes to this material.
         */
        public MaterialBuilder attributes(Attribute... attrs) {
            this.attributes.addAll(List.of(attrs));
            return this;
        }

        /**
         * Builds the material component.
         */
        public Component build() {
            if (name == null) {
                throw new IllegalStateException("Material name must be set");
            }

            OpenIdentifier id = OpenIdentifier.of(namespace, name);
            Set<OpenIdentifier> allTags = new java.util.HashSet<>(Set.of(type));
            allTags.addAll(additionalTags);

            Map<String, List<?>> properties = Map.of(
                    Attribute.KEY.key(), new java.util.ArrayList<>(attributes)
            );

            return new StaticComponent(id, allTags, properties);
        }
    }

    /**
     * Creates a new material builder for fluent customization.
     *
     * @return a new MaterialBuilder instance
     */
    public static MaterialBuilder builder() {
        return new MaterialBuilder();
    }
}
