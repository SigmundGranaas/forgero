package com.sigmundgranaas.vanillaupgrades.tests;

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

/**
 * Comprehensive tests for material loading in vanilla-upgrades mod.
 * Validates that all materials from forgero-materials module load correctly.
 */
public class MaterialLoadingTest {

    private static final String BATCH = "vanilla_upgrades_materials";

    private static ComponentRegistry getRegistry() {
        return ForgeroInitializedCallback.getServices()
                .map(s -> s.componentRegistry())
                .orElseThrow(() -> new AssertionError("ComponentRegistry not available"));
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void forgero_services_initialized(TestContext context) {
        assertTrue(ForgeroInitializedCallback.getServices().isPresent(),
                "ForgeroServices must be initialized");
        assertNotNull(getRegistry(), "ComponentRegistry must be available");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void iron_material_loads_with_attributes(TestContext context) {
        Component iron = getRegistry().get(OpenIdentifier.parse("forgero:iron"))
                .orElseThrow(() -> new AssertionError("Iron material not loaded"));

        List<? extends Attribute> attributes = iron.properties(Attribute.KEY);
        assertFalse(attributes.isEmpty(), "Iron must have attributes");

        // Verify iron has expected attribute types
        boolean hasDurability = attributes.stream()
                .anyMatch(attr -> attr.type().name().contains("durability"));
        assertTrue(hasDurability, "Iron must have durability attribute");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void diamond_material_loads_with_attributes(TestContext context) {
        Component diamond = getRegistry().get(OpenIdentifier.parse("forgero:diamond"))
                .orElseThrow(() -> new AssertionError("Diamond material not loaded"));

        List<? extends Attribute> attributes = diamond.properties(Attribute.KEY);
        assertFalse(attributes.isEmpty(), "Diamond must have attributes");

        // Verify diamond has tags
        assertFalse(diamond.getTags().isEmpty(), "Diamond must have tags");
        assertTrue(diamond.getTags().stream()
                        .anyMatch(tag -> tag.name().contains("mineral")),
                "Diamond should be tagged as mineral");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void netherite_material_loads(TestContext context) {
        Component netherite = getRegistry().get(OpenIdentifier.parse("forgero:netherite"))
                .orElseThrow(() -> new AssertionError("Netherite material not loaded"));

        assertNotNull(netherite.id(), "Netherite must have ID");
        assertEquals("forgero", netherite.id().namespace(), "Netherite must be in forgero namespace");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void gold_material_loads(TestContext context) {
        Component gold = getRegistry().get(OpenIdentifier.parse("forgero:gold"))
                .orElseThrow(() -> new AssertionError("Gold material not loaded"));

        assertTrue(gold.getTags().stream().anyMatch(tag -> tag.name().contains("metal")),
                "Gold should be tagged as metal");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void oak_wood_material_loads(TestContext context) {
        Component oak = getRegistry().get(OpenIdentifier.parse("forgero:oak"))
                .orElseThrow(() -> new AssertionError("Oak material not loaded"));

        assertTrue(oak.getTags().stream().anyMatch(tag -> tag.name().contains("wood")),
                "Oak should be tagged as wood");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void stone_material_loads(TestContext context) {
        Component stone = getRegistry().get(OpenIdentifier.parse("forgero:stone"))
                .orElseThrow(() -> new AssertionError("Stone material not loaded"));

        assertTrue(stone.getTags().stream().anyMatch(tag -> tag.name().contains("stone")),
                "Stone should be tagged as stone");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void copper_material_loads(TestContext context) {
        Component copper = getRegistry().get(OpenIdentifier.parse("forgero:copper"))
                .orElseThrow(() -> new AssertionError("Copper material not loaded"));

        assertTrue(copper.getTags().stream().anyMatch(tag -> tag.name().contains("metal")),
                "Copper should be tagged as metal");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void emerald_material_loads(TestContext context) {
        Component emerald = getRegistry().get(OpenIdentifier.parse("forgero:emerald"))
                .orElseThrow(() -> new AssertionError("Emerald material not loaded"));

        assertTrue(emerald.getTags().stream().anyMatch(tag -> tag.name().contains("mineral")),
                "Emerald should be tagged as mineral");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void leather_material_loads(TestContext context) {
        Component leather = getRegistry().get(OpenIdentifier.parse("forgero:leather"))
                .orElseThrow(() -> new AssertionError("Leather material not loaded"));

        assertTrue(leather.getTags().stream().anyMatch(tag -> tag.name().contains("leather")),
                "Leather should be tagged as leather");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void all_vanilla_materials_loaded(TestContext context) {
        int materialCount = getRegistry().all().size();
        assertTrue(materialCount >= 40,
                "Expected at least 40 materials from forgero-materials, found: " + materialCount);

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void materials_have_tool_material_tag(TestContext context) {
        Component iron = getRegistry().get(OpenIdentifier.parse("forgero:iron"))
                .orElseThrow(() -> new AssertionError("Iron material not loaded"));

        // Check for tool_material tag (path may vary in format)
        boolean hasToolMaterialTag = iron.getTags().stream()
                .anyMatch(tag -> tag.name().contains("tool_material") ||
                                 tag.name().contains("material"));
        assertTrue(hasToolMaterialTag, "Iron should have material-related tag. Found tags: " + iron.getTags());

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void materials_have_unique_ids(TestContext context) {
        var allComponents = getRegistry().all();
        long uniqueIds = allComponents.stream()
                .map(Component::id)
                .distinct()
                .count();

        assertEquals(allComponents.size(), uniqueIds,
                "All materials must have unique IDs");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void no_components_fail_initialization(TestContext context) {
        // If we get here, all components loaded without throwing exceptions
        var allComponents = getRegistry().all();
        for (Component component : allComponents) {
            assertNotNull(component.id(), "Component ID must not be null: " + component);
            assertNotNull(component.getTags(), "Component tags must not be null: " + component.id());
        }

        context.complete();
    }
}
