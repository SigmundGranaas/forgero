package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.registry.Registries;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class BowContentGameTest implements FabricGameTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(BowContentGameTest.class);

    private Optional<Component> findComponent(String id) {
        return ForgeroApi.services().componentRegistry().get(OpenIdentifier.parse(id));
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = false)
    public void bow_limb_parts_are_registered(TestContext context) {
        String[] woodMaterials = {"oak", "birch", "spruce", "jungle", "acacia", "dark_oak"};

        for (String wood : woodMaterials) {
            String partId = "forgero:" + wood + "-bow_limb";
            var part = findComponent(partId);
            assertTrue(part.isPresent(), "Bow limb part must exist: " + partId);
            LOGGER.info("Found bow limb part: {}", partId);
        }

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = false)
    public void bow_string_part_is_registered(TestContext context) {
        var bowString = findComponent("forgero:string-bow_string");
        assertTrue(bowString.isPresent(), "Bow string part must exist: forgero:string-bow_string");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = false)
    public void arrow_head_parts_are_registered(TestContext context) {
        var flintArrowHead = findComponent("forgero:flint-arrow_head");
        assertTrue(flintArrowHead.isPresent(), "Flint arrow head must exist: forgero:flint-arrow_head");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = false)
    public void fletching_parts_are_registered(TestContext context) {
        var fletching = findComponent("forgero:feather-fletching");
        assertTrue(fletching.isPresent(), "Fletching must exist: forgero:feather-fletching");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void bow_materials_are_registered(TestContext context) {
        var feather = findComponent("forgero:feather");
        assertTrue(feather.isPresent(), "Feather material must exist");

        var string = findComponent("forgero:string");
        assertTrue(string.isPresent(), "String material must exist");

        var flint = findComponent("forgero:flint");
        assertTrue(flint.isPresent(), "Flint material must exist");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = false)
    public void shortbow_schematic_is_registered(TestContext context) {
        var schematic = findComponent("forgero:shortbow_limb_schematic");
        assertTrue(schematic.isPresent(), "Shortbow limb schematic must exist: forgero:shortbow_limb_schematic");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = false)
    public void longbow_schematic_is_registered(TestContext context) {
        var schematic = findComponent("forgero:longbow_limb_schematic");
        assertTrue(schematic.isPresent(), "Longbow limb schematic must exist: forgero:longbow_limb_schematic");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = false)
    public void bow_equipment_is_registered(TestContext context) {
        String[] woodMaterials = {"oak", "birch", "spruce"};

        for (String wood : woodMaterials) {
            String bowId = "forgero:" + wood + "-bow";
            var bow = findComponent(bowId);
            assertTrue(bow.isPresent(), "Bow must exist: " + bowId);
            LOGGER.info("Found bow: {}", bowId);
        }

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = false)
    public void oak_bow_item_is_registered(TestContext context) {
        Identifier oakBowId = new Identifier("forgero", "oak-bow");
        assertTrue(Registries.ITEM.containsId(oakBowId),
                "Oak bow item must be registered: " + oakBowId);
        
        LOGGER.info("Verified bow item registration: {}", oakBowId);
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = false)
    public void flint_arrow_item_is_registered(TestContext context) {
        Identifier flintArrowId = new Identifier("forgero", "flint-arrow");
        assertTrue(Registries.ITEM.containsId(flintArrowId),
                "Flint arrow item must be registered: " + flintArrowId);
        
        LOGGER.info("Verified arrow item registration: {}", flintArrowId);
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = false)
    public void debug_list_bow_components(TestContext context) {
        LOGGER.info("=== Bow-related components in registry ===");

        int bowCount = 0;
        int partCount = 0;

        for (var comp : ForgeroApi.services().componentRegistry().all()) {
            String id = comp.id().toString();
            if (id.contains("bow") || id.contains("arrow") || id.contains("fletching") || id.contains("limb")) {
                LOGGER.info("  Component: {}", id);
                if (id.contains("-bow") && !id.contains("bow_")) {
                    bowCount++;
                } else {
                    partCount++;
                }
            }
        }

        LOGGER.info("Total: {} bows, {} parts", bowCount, partCount);

        context.complete();
    }
}
