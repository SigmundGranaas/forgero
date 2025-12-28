package com.sigmundgranaas.vanillaupgrades.tests;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.loader.api.ForgeroInitializedCallback;
import com.sigmundgranaas.forgero.loader.api.ForgeroServices;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for vanilla tools in vanilla-upgrades mod.
 * Validates that vanilla tools convert to components and have upgrade slots.
 */
public class VanillaToolTest {

    private static final String BATCH = "vanilla_upgrades_tools";

    private static ForgeroServices getServices() {
        return ForgeroInitializedCallback.getServices()
                .orElseThrow(() -> new AssertionError("ForgeroServices not available"));
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void iron_pickaxe_converts_to_component(TestContext context) {
        ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
        Optional<Component> component = getServices().converter().toComponent(pickaxe);

        assertTrue(component.isPresent(), "Iron pickaxe must convert to component");
        assertNotNull(component.get().id(), "Component must have ID");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void diamond_sword_converts_to_component(TestContext context) {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        Optional<Component> component = getServices().converter().toComponent(sword);

        assertTrue(component.isPresent(), "Diamond sword must convert to component");
        assertTrue(component.get().getTags().stream()
                        .anyMatch(tag -> tag.name().contains("sword")),
                "Diamond sword should have sword tag");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void iron_pickaxe_has_upgrade_slots(TestContext context) {
        ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
        Optional<Component> component = getServices().converter().toComponent(pickaxe);

        assertTrue(component.isPresent(), "Iron pickaxe must convert to component");
        assertTrue(component.get() instanceof CustomizableComponent,
                "Iron pickaxe must be customizable");

        CustomizableComponent customizable = (CustomizableComponent) component.get();
        assertFalse(customizable.upgrades().isEmpty(),
                "Iron pickaxe must have upgrade slots");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void netherite_pickaxe_converts_to_component(TestContext context) {
        ItemStack pickaxe = new ItemStack(Items.NETHERITE_PICKAXE);
        Optional<Component> component = getServices().converter().toComponent(pickaxe);

        assertTrue(component.isPresent(), "Netherite pickaxe must convert to component");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void golden_axe_converts_to_component(TestContext context) {
        ItemStack axe = new ItemStack(Items.GOLDEN_AXE);
        Optional<Component> component = getServices().converter().toComponent(axe);

        assertTrue(component.isPresent(), "Golden axe must convert to component");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void component_converts_back_to_itemstack(TestContext context) {
        ItemStack original = new ItemStack(Items.IRON_PICKAXE);
        Optional<Component> component = getServices().converter().toComponent(original);

        assertTrue(component.isPresent(), "Must convert to component");

        Optional<ItemStack> convertedOpt = getServices().converter().toStack(component.get());

        assertTrue(convertedOpt.isPresent(), "Must convert back");
        assertEquals(Items.IRON_PICKAXE, convertedOpt.get().getItem(),
                "Component must convert back to correct item");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void diamond_pickaxe_has_binding_slot(TestContext context) {
        ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        Optional<Component> component = getServices().converter().toComponent(pickaxe);

        assertTrue(component.isPresent(), "Diamond pickaxe must convert");
        assertTrue(component.get() instanceof CustomizableComponent, "Must be customizable");

        CustomizableComponent custom = (CustomizableComponent) component.get();
        boolean hasBindingSlot = custom.upgrades().allUpgradeSlots().stream()
                .anyMatch(slot -> slot.slotType().name().contains("binding") ||
                                  slot.description().contains("binding"));

        assertTrue(hasBindingSlot, "Diamond pickaxe should have binding upgrade slot");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void iron_sword_converts(TestContext context) {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        Optional<Component> component = getServices().converter().toComponent(sword);

        assertTrue(component.isPresent(), "Iron sword must convert to component");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void diamond_axe_converts(TestContext context) {
        ItemStack axe = new ItemStack(Items.DIAMOND_AXE);
        Optional<Component> component = getServices().converter().toComponent(axe);

        assertTrue(component.isPresent(), "Diamond axe must convert to component");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void wooden_shovel_converts(TestContext context) {
        ItemStack shovel = new ItemStack(Items.WOODEN_SHOVEL);
        Optional<Component> component = getServices().converter().toComponent(shovel);

        assertTrue(component.isPresent(), "Wooden shovel must convert to component");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void stone_hoe_converts(TestContext context) {
        ItemStack hoe = new ItemStack(Items.STONE_HOE);
        Optional<Component> component = getServices().converter().toComponent(hoe);

        assertTrue(component.isPresent(), "Stone hoe must convert to component");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void all_upgrade_slots_are_empty_initially(TestContext context) {
        ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
        Optional<Component> component = getServices().converter().toComponent(pickaxe);

        assertTrue(component.isPresent(), "Must convert");
        assertTrue(component.get() instanceof CustomizableComponent, "Must be customizable");

        CustomizableComponent custom = (CustomizableComponent) component.get();
        boolean allEmpty = custom.upgrades().allUpgradeSlots().stream()
                .allMatch(slot -> slot.isEmpty());

        assertTrue(allEmpty, "All upgrade slots should be empty initially");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void vanilla_tools_have_correct_namespace(TestContext context) {
        ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
        Optional<Component> component = getServices().converter().toComponent(pickaxe);

        assertTrue(component.isPresent(), "Must convert");
        assertTrue(component.get().id().namespace().equals("forgero") ||
                        component.get().id().namespace().equals("minecraft"),
                "Component namespace should be forgero or minecraft");

        context.complete();
    }
}
