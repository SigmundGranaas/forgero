package com.sigmundgranaas.forgero.core.attribute;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeContext;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.attribute.api.operator.AdditionOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.MultiplicationOperator;
import com.sigmundgranaas.forgero.core.attribute.impl.CompositeAttributeBakingStrategy;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.impl.*;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotValidator;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

import static com.sigmundgranaas.forgero.core.attribute.api.Attribute.KEY;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test demonstrating the bug in CompositeAttributeBakingStrategy.
 *
 * <p><b>Bug Description:</b></p>
 * When composePartAttributes() is called on a StructuredEquipment (like iron-pickaxe),
 * it only looks at the immediate children's properties, not their nested structure.
 *
 * <p><b>Example:</b></p>
 * <pre>
 * iron-pickaxe (StructuredEquipment)
 *   structure:
 *     head → iron-pickaxe_head (StructuredPart)
 *              properties: {durability_mult: 1.0}  ← Only template multiplier
 *              structure:
 *                material → iron
 *                             properties: {durability: 240}  ← Base value is HERE!
 * </pre>
 *
 * composePartAttributes() sees iron-pickaxe_head's properties (multiplier only),
 * but misses the iron material's base value because it's nested in structure.
 */
public class CompositeAttributeResolutionBugTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompositeAttributeResolutionBugTest.class);

    private static final OpenIdentifier IRON_ID = new OpenIdentifier("forgero", "iron");
    private static final OpenIdentifier PICKAXE_HEAD_ID = new OpenIdentifier("forgero", "iron-pickaxe_head");
    private static final OpenIdentifier PICKAXE_ID = new OpenIdentifier("forgero", "iron-pickaxe");
    private static final OpenIdentifier HANDLE_ID = new OpenIdentifier("forgero", "wooden_handle");

    @Test
    public void testNestedStructureComposition() {
        LOGGER.info("=== Testing Nested Structure Attribute Composition ===");

        // Build component tree manually
        Component iron = createIronMaterial();
        Component pickaxeHead = createPickaxeHead(iron);
        Component handle = createHandle();
        Component pickaxe = createPickaxe(pickaxeHead, handle);

        LOGGER.info("Created component tree:");
        LOGGER.info("  iron: {}", getPartCompositeAttrs(iron));
        LOGGER.info("  pickaxe_head: {}", getPartCompositeAttrs(pickaxeHead));
        LOGGER.info("  handle: {}", getPartCompositeAttrs(handle));
        LOGGER.info("  pickaxe: {}", getPartCompositeAttrs(pickaxe));

        // Simulate what ResolverEngine.traverse() would return
        List<Component> allComponents = List.of(pickaxe, pickaxeHead, iron, handle);

        // Run the baking strategy
        CompositeAttributeBakingStrategy strategy = new CompositeAttributeBakingStrategy();
        List<Attribute> bakedAttributes = strategy.bake(allComponents.stream());

        LOGGER.info("Baked attributes: {}", bakedAttributes);
        LOGGER.info("Baked attributes count: {}", bakedAttributes.size());
        for (Attribute attr : bakedAttributes) {
            LOGGER.info("  - {}: {}", attr.type(), attr.value());
        }

        // Check for durability attribute
        Optional<Attribute> durability = bakedAttributes.stream()
                .filter(attr -> attr.type().equals(DefaultAttributes.DURABILITY))
                .findFirst();

        LOGGER.info("Durability attribute found: {}", durability);

        if (durability.isPresent()) {
            float value = durability.get().value();
            LOGGER.info("Durability value: {}", value);

            // Expected: 240 (iron base) × 1.0 (pickaxe_head multiplier) = 240
            // Actual (buggy): Probably missing because multiplier and base come from different nesting levels
            assertTrue(value > 200, "Durability should be > 200 (from iron material), but was: " + value);
        } else {
            fail("Durability attribute is MISSING! This is the bug - nested structure composition failed.");
        }
    }

    private Component createIronMaterial() {
        // Iron material with durability base value in part-composite context
        Attribute durability = SimpleAttribute.withContext(
                DefaultAttributes.DURABILITY,
                240f,
                AdditionOperator.getInstance(),
                AttributeContext.PART_COMPOSITE
        );

        Map<String, List<?>> properties = Map.of(KEY.key(), List.of(durability));

        return new StaticComponent(
                IRON_ID,
                Set.of(new OpenIdentifier("forgero", "materials/metal")),
                properties
        );
    }

    private Component createPickaxeHead(Component iron) {
        // Pickaxe head template with durability multiplier in part-composite context
        Attribute durabilityMult = SimpleAttribute.withContext(
                DefaultAttributes.DURABILITY,
                1.0f,
                MultiplicationOperator.getInstance(),
                AttributeContext.PART_COMPOSITE
        );

        Map<String, List<?>> properties = Map.of(KEY.key(), List.of(durabilityMult));

        // Structure contains the iron material
        ComponentPart materialPart = new ComponentPart(
                new OpenIdentifier("forgero", "material"),
                new OpenIdentifier("forgero", "tool_material"),
                null,
                SlotValidator.ACCEPT_ALL,
                iron
        );

        ComponentStructure structure = ComponentStructure.of(materialPart);

        return new StructuredPart(
                PICKAXE_HEAD_ID,
                Set.of(new OpenIdentifier("forgero", "parts/pickaxe_head")),
                properties,
                structure
        );
    }

    private Component createHandle() {
        // Handle with base durability
        Attribute durability = SimpleAttribute.withContext(
                DefaultAttributes.DURABILITY,
                50f,
                AdditionOperator.getInstance(),
                AttributeContext.PART_COMPOSITE
        );

        Map<String, List<?>> properties = Map.of(KEY.key(), List.of(durability));

        return new StaticComponent(
                HANDLE_ID,
                Set.of(new OpenIdentifier("forgero", "parts/handle")),
                properties
        );
    }

    private Component createPickaxe(Component head, Component handle) {
        // Pickaxe template with attack speed (no part-composite context)
        Attribute attackSpeed = new SimpleAttribute(
                DefaultAttributes.ATTACK_SPEED,
                -2.8f
        );

        Map<String, List<?>> properties = Map.of(KEY.key(), List.of(attackSpeed));

        // Structure contains head and handle
        ComponentPart headPart = new ComponentPart(
                new OpenIdentifier("forgero", "head"),
                new OpenIdentifier("forgero", "parts/types/pickaxe_head"),
                null,
                SlotValidator.ACCEPT_ALL,
                head
        );

        ComponentPart handlePart = new ComponentPart(
                new OpenIdentifier("forgero", "handle"),
                new OpenIdentifier("forgero", "parts/categories/handle"),
                null,
                SlotValidator.ACCEPT_ALL,
                handle
        );

        ComponentStructure structure = ComponentStructure.of(headPart, handlePart);

        return new StructuredEquipment(
                PICKAXE_ID,
                Set.of(new OpenIdentifier("forgero", "tools/pickaxe")),
                properties,
                structure
        );
    }

    private List<Attribute> getPartCompositeAttrs(Component component) {
        return component.properties(KEY).stream()
                .filter(attr -> attr.context().map(ctx -> ctx.equals(AttributeContext.PART_COMPOSITE)).orElse(false))
                .toList();
    }
}
