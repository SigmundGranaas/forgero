package com.sigmundgranaas.forgero.testcommon.fixtures;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.condition.api.Condition;

/**
 * Pre-built attribute fixtures for testing.
 * Provides factory methods for creating common test attributes with sensible defaults.
 *
 * <p>Example usage:
 * <pre>{@code
 * Attribute damage = PropertyFixtures.attackDamage(10.0f);
 * Attribute speed = PropertyFixtures.miningSpeed(6.0f);
 * Attribute durability = PropertyFixtures.durability(1561);
 * }</pre>
 */
public final class PropertyFixtures {

    private PropertyFixtures() {
        // Prevent instantiation
    }

    // ========== Common Attribute Types ==========

    /**
     * Standard attribute type identifiers used in Forgero.
     */
    public static class AttributeTypes {
        public static final OpenIdentifier ATTACK_DAMAGE = OpenIdentifier.of("attack_damage");
        public static final OpenIdentifier MINING_SPEED = OpenIdentifier.of("mining_speed");
        public static final OpenIdentifier DURABILITY = OpenIdentifier.of("durability");
        public static final OpenIdentifier MINING_LEVEL = OpenIdentifier.of("mining_level");
        public static final OpenIdentifier ATTACK_SPEED = OpenIdentifier.of("attack_speed");
        public static final OpenIdentifier ARMOR = OpenIdentifier.of("armor");
        public static final OpenIdentifier ARMOR_TOUGHNESS = OpenIdentifier.of("armor_toughness");
        public static final OpenIdentifier KNOCKBACK_RESISTANCE = OpenIdentifier.of("knockback_resistance");
    }

    // ========== Factory Methods ==========

    /**
     * Creates an attack damage attribute with the specified value.
     *
     * @param value the attack damage value
     * @return an Attribute representing attack damage
     */
    public static Attribute attackDamage(float value) {
        return new SimpleAttribute(AttributeTypes.ATTACK_DAMAGE, value, Condition.ALWAYS_TRUE);
    }

    /**
     * Creates a mining speed attribute with the specified value.
     *
     * @param value the mining speed value
     * @return an Attribute representing mining speed
     */
    public static Attribute miningSpeed(float value) {
        return new SimpleAttribute(AttributeTypes.MINING_SPEED, value, Condition.ALWAYS_TRUE);
    }

    /**
     * Creates a durability attribute with the specified value.
     *
     * @param value the durability value
     * @return an Attribute representing durability
     */
    public static Attribute durability(int value) {
        return new SimpleAttribute(AttributeTypes.DURABILITY, value, Condition.ALWAYS_TRUE);
    }

    /**
     * Creates a mining level attribute with the specified value.
     *
     * @param value the mining level value (0-4: wood/stone/iron/diamond/netherite)
     * @return an Attribute representing mining level
     */
    public static Attribute miningLevel(int value) {
        return new SimpleAttribute(AttributeTypes.MINING_LEVEL, value, Condition.ALWAYS_TRUE);
    }

    /**
     * Creates an attack speed attribute with the specified value.
     *
     * @param value the attack speed value
     * @return an Attribute representing attack speed
     */
    public static Attribute attackSpeed(float value) {
        return new SimpleAttribute(AttributeTypes.ATTACK_SPEED, value, Condition.ALWAYS_TRUE);
    }

    /**
     * Creates an armor attribute with the specified value.
     *
     * @param value the armor value
     * @return an Attribute representing armor
     */
    public static Attribute armor(float value) {
        return new SimpleAttribute(AttributeTypes.ARMOR, value, Condition.ALWAYS_TRUE);
    }

    /**
     * Creates an armor toughness attribute with the specified value.
     *
     * @param value the armor toughness value
     * @return an Attribute representing armor toughness
     */
    public static Attribute armorToughness(float value) {
        return new SimpleAttribute(AttributeTypes.ARMOR_TOUGHNESS, value, Condition.ALWAYS_TRUE);
    }

    /**
     * Creates a knockback resistance attribute with the specified value.
     *
     * @param value the knockback resistance value
     * @return an Attribute representing knockback resistance
     */
    public static Attribute knockbackResistance(float value) {
        return new SimpleAttribute(AttributeTypes.KNOCKBACK_RESISTANCE, value, Condition.ALWAYS_TRUE);
    }

    /**
     * Creates a custom attribute with a condition.
     *
     * @param type the attribute type
     * @param value the attribute value
     * @param condition the condition for this attribute to apply
     * @return a conditional Attribute
     */
    public static Attribute conditional(OpenIdentifier type, float value, Condition condition) {
        return new SimpleAttribute(type, value, condition);
    }

    // ========== Material-Based Presets ==========

    /**
     * Preset: Wood tool attack damage (1.0)
     */
    public static Attribute woodAttackDamage() {
        return attackDamage(1.0f);
    }

    /**
     * Preset: Stone tool attack damage (3.0)
     */
    public static Attribute stoneAttackDamage() {
        return attackDamage(3.0f);
    }

    /**
     * Preset: Iron tool attack damage (6.0)
     */
    public static Attribute ironAttackDamage() {
        return attackDamage(6.0f);
    }

    /**
     * Preset: Diamond tool attack damage (8.0)
     */
    public static Attribute diamondAttackDamage() {
        return attackDamage(8.0f);
    }

    /**
     * Preset: Netherite tool attack damage (10.0)
     */
    public static Attribute netheriteAttackDamage() {
        return attackDamage(10.0f);
    }

    /**
     * Preset: Wood tool durability (59)
     */
    public static Attribute woodDurability() {
        return durability(59);
    }

    /**
     * Preset: Stone tool durability (131)
     */
    public static Attribute stoneDurability() {
        return durability(131);
    }

    /**
     * Preset: Iron tool durability (250)
     */
    public static Attribute ironDurability() {
        return durability(250);
    }

    /**
     * Preset: Diamond tool durability (1561)
     */
    public static Attribute diamondDurability() {
        return durability(1561);
    }

    /**
     * Preset: Netherite tool durability (2031)
     */
    public static Attribute netheriteDurability() {
        return durability(2031);
    }

    /**
     * Preset: Wood mining speed (2.0)
     */
    public static Attribute woodMiningSpeed() {
        return miningSpeed(2.0f);
    }

    /**
     * Preset: Stone mining speed (4.0)
     */
    public static Attribute stoneMiningSpeed() {
        return miningSpeed(4.0f);
    }

    /**
     * Preset: Iron mining speed (6.0)
     */
    public static Attribute ironMiningSpeed() {
        return miningSpeed(6.0f);
    }

    /**
     * Preset: Diamond mining speed (8.0)
     */
    public static Attribute diamondMiningSpeed() {
        return miningSpeed(8.0f);
    }

    /**
     * Preset: Netherite mining speed (9.0)
     */
    public static Attribute netheriteMiningSpeed() {
        return miningSpeed(9.0f);
    }
}
