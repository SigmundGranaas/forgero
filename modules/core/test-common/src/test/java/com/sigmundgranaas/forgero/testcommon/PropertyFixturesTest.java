package com.sigmundgranaas.forgero.testcommon;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.testcommon.fixtures.PropertyFixtures;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PropertyFixtures utility class.
 */
class PropertyFixturesTest {

    @Test
    void attackDamage_shouldCreateAttributeWithCorrectValue() {
        Attribute attribute = PropertyFixtures.attackDamage(10.0f);

        assertEquals(PropertyFixtures.AttributeTypes.ATTACK_DAMAGE, attribute.type());
        assertEquals(10.0f, attribute.value(), 0.001f);
    }

    @Test
    void miningSpeed_shouldCreateAttributeWithCorrectValue() {
        Attribute attribute = PropertyFixtures.miningSpeed(6.0f);

        assertEquals(PropertyFixtures.AttributeTypes.MINING_SPEED, attribute.type());
        assertEquals(6.0f, attribute.value(), 0.001f);
    }

    @Test
    void durability_shouldCreateAttributeWithCorrectValue() {
        Attribute attribute = PropertyFixtures.durability(1561);

        assertEquals(PropertyFixtures.AttributeTypes.DURABILITY, attribute.type());
        assertEquals(1561, (int) attribute.value());
    }

    @Test
    void miningLevel_shouldCreateAttributeWithCorrectValue() {
        Attribute attribute = PropertyFixtures.miningLevel(3);

        assertEquals(PropertyFixtures.AttributeTypes.MINING_LEVEL, attribute.type());
        assertEquals(3, (int) attribute.value());
    }

    @Test
    void woodAttackDamage_shouldReturnCorrectPresetValue() {
        Attribute attribute = PropertyFixtures.woodAttackDamage();

        assertEquals(PropertyFixtures.AttributeTypes.ATTACK_DAMAGE, attribute.type());
        assertEquals(1.0f, attribute.value(), 0.001f);
    }

    @Test
    void ironAttackDamage_shouldReturnCorrectPresetValue() {
        Attribute attribute = PropertyFixtures.ironAttackDamage();

        assertEquals(PropertyFixtures.AttributeTypes.ATTACK_DAMAGE, attribute.type());
        assertEquals(6.0f, attribute.value(), 0.001f);
    }

    @Test
    void diamondDurability_shouldReturnCorrectPresetValue() {
        Attribute attribute = PropertyFixtures.diamondDurability();

        assertEquals(PropertyFixtures.AttributeTypes.DURABILITY, attribute.type());
        assertEquals(1561, (int) attribute.value());
    }

    @Test
    void conditional_shouldCreateAttributeWithCustomCondition() {
        OpenIdentifier customType = OpenIdentifier.of("test", "custom");
        Attribute attribute = PropertyFixtures.conditional(
            customType,
            5.0f,
            com.sigmundgranaas.forgero.core.condition.api.Condition.ALWAYS_TRUE
        );

        assertEquals(customType, attribute.type());
        assertEquals(5.0f, attribute.value(), 0.001f);
        assertTrue(attribute.condition().isPresent());
    }
}
