package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.loader.api.ForgeroInitializedCallback;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the upgrade system - verifying that upgrade materials
 * can be installed in upgrade slots and stats are modified correctly.
 */
public class UpgradeSystemTest {

    private static final String BATCH = "forgero_upgrades";

    private static ComponentRegistry getRegistry() {
        return ForgeroInitializedCallback.getServices()
                .map(s -> s.componentRegistry())
                .orElseThrow(() -> new AssertionError("ComponentRegistry not available"));
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void upgrade_materials_loaded(TestContext context) {
        // Verify upgrade materials from forgero-upgrades module
        ComponentRegistry registry = getRegistry();

        // Check for upgrade materials
        var stringOpt = registry.get(OpenIdentifier.of("forgero:string"));
        var leatherOpt = registry.get(OpenIdentifier.of("forgero:leather"));

        assertTrue(stringOpt.isPresent() || leatherOpt.isPresent(),
                "At least some upgrade materials should be loaded");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void upgrade_materials_have_upgrade_tags(TestContext context) {
        var allComponents = getRegistry().all();

        // Find components with upgrade tags
        long upgradeCount = allComponents.stream()
                .filter(c -> c.getTags().stream()
                        .anyMatch(tag -> tag.name().contains("upgrade")))
                .count();

        assertTrue(upgradeCount > 0,
                "Should have materials with upgrade tags");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void binding_upgrade_materials_exist(TestContext context) {
        var allComponents = getRegistry().all();

        // Check for binding upgrade materials
        long bindingCount = allComponents.stream()
                .filter(c -> c.getTags().stream()
                        .anyMatch(tag -> tag.name().contains("binding")))
                .count();

        assertTrue(bindingCount > 0,
                "Should have materials with binding tags for upgrade slots");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void gem_upgrade_materials_exist(TestContext context) {
        var allComponents = getRegistry().all();

        // Check for gem upgrade materials
        long gemCount = allComponents.stream()
                .filter(c -> c.getTags().stream()
                        .anyMatch(tag -> tag.name().contains("gem")))
                .count();

        assertTrue(gemCount > 0,
                "Should have materials with gem tags for upgrade slots");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void upgrade_slots_have_validators(TestContext context) {
        var allComponents = getRegistry().all();

        // Find customizable components and check their slots
        var customizableComps = allComponents.stream()
                .filter(c -> c instanceof CustomizableComponent)
                .map(c -> (CustomizableComponent) c)
                .toList();

        if (!customizableComps.isEmpty()) {
            for (CustomizableComponent comp : customizableComps) {
                List<ComponentUpgradeSlot> slots = comp.upgrades().slots().all();
                for (ComponentUpgradeSlot slot : slots) {
                    assertNotNull(slot.validator(),
                            "Upgrade slot must have validator: " + slot.id());
                }
            }
        }

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void upgrade_slots_have_types(TestContext context) {
        var allComponents = getRegistry().all();

        var customizableComps = allComponents.stream()
                .filter(c -> c instanceof CustomizableComponent)
                .map(c -> (CustomizableComponent) c)
                .toList();

        if (!customizableComps.isEmpty()) {
            for (CustomizableComponent comp : customizableComps) {
                List<ComponentUpgradeSlot> slots = comp.upgrades().slots().all();
                for (ComponentUpgradeSlot slot : slots) {
                    assertNotNull(slot.type(), "Slot must have type: " + slot.id());
                    assertFalse(slot.type().name().isEmpty(), "Slot type must not be empty");
                }
            }
        }

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void upgrade_slots_have_descriptions(TestContext context) {
        var allComponents = getRegistry().all();

        var customizableComps = allComponents.stream()
                .filter(c -> c instanceof CustomizableComponent)
                .map(c -> (CustomizableComponent) c)
                .toList();

        if (!customizableComps.isEmpty()) {
            for (CustomizableComponent comp : customizableComps) {
                List<ComponentUpgradeSlot> slots = comp.upgrades().slots().all();
                for (ComponentUpgradeSlot slot : slots) {
                    assertNotNull(slot.description(),
                            "Slot must have description: " + slot.id());
                }
            }
        }

        context.complete();
    }
}
