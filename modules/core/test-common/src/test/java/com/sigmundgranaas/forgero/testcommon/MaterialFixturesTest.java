package com.sigmundgranaas.forgero.testcommon;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.testcommon.fixtures.MaterialFixtures;
import com.sigmundgranaas.forgero.testcommon.fixtures.PropertyFixtures;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for MaterialFixtures utility class.
 */
class MaterialFixturesTest {

    @Test
    void iron_shouldCreateMaterialWithCorrectId() {
        Component iron = MaterialFixtures.iron();

        assertEquals(OpenIdentifier.of("iron"), iron.id());
    }

    @Test
    void iron_shouldHaveMetalTag() {
        Component iron = MaterialFixtures.iron();

        assertTrue(iron.getTags().contains(MaterialFixtures.MaterialTypes.METAL));
    }

    @Test
    void iron_shouldHaveCorrectAttributes() {
        Component iron = MaterialFixtures.iron();

        var attributes = iron.properties(Attribute.KEY);
        assertEquals(4, attributes.size());

        assertTrue(attributes.stream()
            .anyMatch(attr -> attr.type().equals(PropertyFixtures.AttributeTypes.ATTACK_DAMAGE)
                && attr.value() == 6.0f));
        assertTrue(attributes.stream()
            .anyMatch(attr -> attr.type().equals(PropertyFixtures.AttributeTypes.MINING_SPEED)
                && attr.value() == 6.0f));
        assertTrue(attributes.stream()
            .anyMatch(attr -> attr.type().equals(PropertyFixtures.AttributeTypes.DURABILITY)
                && (int) attr.value() == 250));
        assertTrue(attributes.stream()
            .anyMatch(attr -> attr.type().equals(PropertyFixtures.AttributeTypes.MINING_LEVEL)
                && (int) attr.value() == 2));
    }

    @Test
    void diamond_shouldHaveGemTag() {
        Component diamond = MaterialFixtures.diamond();

        assertTrue(diamond.getTags().contains(MaterialFixtures.MaterialTypes.GEM));
    }

    @Test
    void oak_shouldHaveWoodTag() {
        Component oak = MaterialFixtures.oak();

        assertTrue(oak.getTags().contains(MaterialFixtures.MaterialTypes.WOOD));
    }

    @Test
    void stone_shouldHaveStoneTag() {
        Component stone = MaterialFixtures.stone();

        assertTrue(stone.getTags().contains(MaterialFixtures.MaterialTypes.STONE));
    }

    @Test
    void material_shouldCreateCustomMaterialWithAttributes() {
        Attribute customAttr = PropertyFixtures.attackDamage(15.0f);
        Component custom = MaterialFixtures.material("mythril", customAttr);

        assertEquals(OpenIdentifier.of("mythril"), custom.id());
        assertTrue(custom.getTags().contains(MaterialFixtures.MaterialTypes.MATERIAL));

        var attributes = custom.properties(Attribute.KEY);
        assertEquals(1, attributes.size());
        assertEquals(15.0f, attributes.get(0).value(), 0.001f);
    }

    @Test
    void materialBuilder_shouldCreateCustomMaterial() {
        Component custom = MaterialFixtures.builder()
            .name("adamantium")
            .namespace("test")
            .type(MaterialFixtures.MaterialTypes.METAL)
            .attribute(PropertyFixtures.attackDamage(20.0f))
            .attribute(PropertyFixtures.durability(5000))
            .build();

        assertEquals(OpenIdentifier.of("test", "adamantium"), custom.id());
        assertTrue(custom.getTags().contains(MaterialFixtures.MaterialTypes.METAL));

        var attributes = custom.properties(Attribute.KEY);
        assertEquals(2, attributes.size());
    }

    @Test
    void materialBuilder_shouldThrowWhenNameNotSet() {
        MaterialFixtures.MaterialBuilder builder = MaterialFixtures.builder()
            .attribute(PropertyFixtures.attackDamage(10.0f));

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void netherite_shouldHaveHighestStats() {
        Component netherite = MaterialFixtures.netherite();

        var attributes = netherite.properties(Attribute.KEY);

        assertTrue(attributes.stream()
            .anyMatch(attr -> attr.type().equals(PropertyFixtures.AttributeTypes.ATTACK_DAMAGE)
                && attr.value() == 10.0f));
        assertTrue(attributes.stream()
            .anyMatch(attr -> attr.type().equals(PropertyFixtures.AttributeTypes.DURABILITY)
                && (int) attr.value() == 2031));
        assertTrue(attributes.stream()
            .anyMatch(attr -> attr.type().equals(PropertyFixtures.AttributeTypes.MINING_LEVEL)
                && (int) attr.value() == 4));
    }
}
