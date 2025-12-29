package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke tests to verify the component system and item registration are functional.
 * These tests fail fast when core functionality is broken.
 */
public class ComponentSmokeTest implements ForgeroGameTest {

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void component_system_is_functional(TestContext context) {
        var ctx = forgero(context);

        // Verify component registry is initialized
        assertNotNull(services().componentRegistry(), "Component registry must be initialized");

        // Verify components are loaded
        assertTrue(services().componentRegistry().all().size() > 0,
                "At least some components must be loaded");

        // Verify a known component exists (iron material)
        var iron = ctx.component("forgero:iron");
        assertTrue(iron.isPresent(), "Iron material must exist");

        // Verify a known vanilla tool exists
        var ironPickaxe = ctx.component("forgero:iron_pickaxe");
        assertTrue(ironPickaxe.isPresent(), "Iron pickaxe must exist");

        context.complete();
    }

    /**
     * CRITICAL TEST: Validates that items are actually registered and usable.
     * This catches the case where components exist but items were never registered.
     *
     * Note: Template-generated tools use hyphen format (iron-pickaxe), while
     * vanilla-upgrade wrapped tools use underscore format (iron_pickaxe).
     * This test validates the template-generated tools are properly registered.
     */
    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void items_are_registered_and_usable(TestContext context) {
        var ctx = forgero(context);

        // Get a known tool component (template-generated use hyphen format)
        var ironPickaxe = ctx.component("forgero:iron-pickaxe");
        assertTrue(ironPickaxe.isPresent(), "Iron pickaxe component must exist (forgero:iron-pickaxe)");

        // CRITICAL: Verify the component can be converted to an ItemStack
        // This fails if item registration was skipped
        var stack = ctx.toStack(ironPickaxe.get());
        assertTrue(stack.isPresent(),
                "Iron pickaxe must be convertible to ItemStack - item registration may have failed");

        // Verify the ItemStack is not empty
        ItemStack itemStack = stack.get();
        assertFalse(itemStack.isEmpty(),
                "Iron pickaxe ItemStack must not be empty");

        // Verify the item has the expected properties of a tool
        assertTrue(itemStack.isDamageable(),
                "Iron pickaxe must be damageable (indicates proper tool registration)");

        // Verify the ItemStack uses a Forgero item (not air or some vanilla fallback)
        Identifier itemId = Registries.ITEM.getId(itemStack.getItem());
        assertTrue(itemId.getNamespace().equals("forgero"),
                "Iron pickaxe ItemStack must use a Forgero item, got: " + itemId);

        context.complete();
    }

    /**
     * Validates that multiple pickaxe tiers can be converted to items.
     * Currently only pickaxe ItemCreator is registered.
     */
    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void multiple_tools_are_registered(TestContext context) {
        var ctx = forgero(context);

        // Template-generated pickaxes use hyphen format
        String[] tools = {
                "forgero:iron-pickaxe",
                "forgero:diamond-pickaxe",
                "forgero:netherite-pickaxe",
                "forgero:gold-pickaxe"
        };

        for (String toolId : tools) {
            var component = ctx.component(toolId);
            assertTrue(component.isPresent(), toolId + " component must exist");

            var stack = ctx.toStack(component.get());
            assertTrue(stack.isPresent(),
                    toolId + " must be convertible to ItemStack");
            assertFalse(stack.get().isEmpty(),
                    toolId + " ItemStack must not be empty");
        }

        context.complete();
    }

    /**
     * CRITICAL TEST: Validates that Forgero's core dynamic items are registered.
     *
     * Forgero uses NBT-backed dynamic items (dynamic_tool, dynamic_sword, dynamic_item)
     * where the actual tool type is determined by NBT data, not individual registered items.
     * If these core items are missing, the entire mod is broken.
     */
    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void forgero_dynamic_items_are_registered(TestContext context) {
        // These are the core dynamic items that MUST be registered
        String[] requiredItems = {
                "forgero:dynamic_tool",
                "forgero:dynamic_sword",
                "forgero:dynamic_item"
        };

        StringBuilder missingItems = new StringBuilder();

        for (String itemId : requiredItems) {
            Identifier id = new Identifier(itemId);
            if (!Registries.ITEM.containsId(id)) {
                if (missingItems.length() > 0) missingItems.append(", ");
                missingItems.append(itemId);
            }
        }

        // ALL required items must be present - if any are missing, item registration is broken
        assertTrue(missingItems.length() == 0,
                "Required Forgero dynamic items missing from Minecraft registry: " + missingItems +
                ". This indicates item registration is broken.");

        context.complete();
    }

    /**
     * Lists all Forgero items in the registry for debugging.
     * This test always passes but provides useful diagnostic output.
     */
    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void debug_list_forgero_items_in_registry(TestContext context) {
        int count = 0;
        System.out.println("=== Forgero items in Minecraft registry ===");
        for (Identifier id : Registries.ITEM.getIds()) {
            if (id.getNamespace().equals("forgero")) {
                System.out.println("  - " + id);
                count++;
            }
        }
        System.out.println("Total Forgero items: " + count);

        // This is a diagnostic test - always pass but warn if no items found
        if (count == 0) {
            System.out.println("WARNING: No Forgero items found in Minecraft registry!");
        }

        context.complete();
    }

    /**
     * Lists pickaxe slot types for debugging default handle selection.
     */
    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void debug_pickaxe_slot_types(TestContext context) {
        var ctx = forgero(context);

        var diamondPickaxe = ctx.component("forgero:diamond-pickaxe");
        if (diamondPickaxe.isPresent()) {
            System.out.println("Diamond pickaxe slot types:");
            var component = diamondPickaxe.get();
            if (component instanceof com.sigmundgranaas.forgero.core.component.api.StructuredComponent structured) {
                for (var slot : structured.structure().allParts()) {
                    System.out.println("  - " + slot.partType());
                }
            }
        }

        context.complete();
    }

    /**
     * CRITICAL: Validates that tools have reasonable attribute values.
     * This test catches attribute calculation bugs like armor leaking to tools.
     */
    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void tool_attributes_are_reasonable(TestContext context) {
        var ctx = forgero(context);
        var query = com.sigmundgranaas.forgero.loader.api.ForgeroApi.itemQuery();

        // Test iron pickaxe attributes
        var ironPickaxe = ctx.component("forgero:iron-pickaxe");
        assertTrue(ironPickaxe.isPresent(), "Iron pickaxe must exist");

        // DEBUG: Print component tree
        System.out.println("=== Iron Pickaxe Component Tree ===");
        printComponentTree(ironPickaxe.get(), 0);

        var stack = ctx.toStack(ironPickaxe.get());
        assertTrue(stack.isPresent(), "Iron pickaxe must convert to ItemStack");

        ItemStack ironStack = stack.get();

        // Iron pickaxe should NOT have armor (it's a tool, not armor!)
        int armor = query.getArmor(ironStack);
        System.out.println("Iron pickaxe armor: " + armor);
        assertEquals(0, armor, "Iron pickaxe should have 0 armor, but has: " + armor);

        // Iron pickaxe should have reasonable durability (iron = 250, not 1600!)
        int durability = query.getMaxDurability(ironStack);
        System.out.println("Iron pickaxe durability: " + durability);
        assertTrue(durability > 100 && durability < 1000,
                "Iron pickaxe durability should be ~250, but is: " + durability);

        // Iron pickaxe should have mining speed (iron = 6)
        float miningSpeed = query.getMiningSpeed(ironStack);
        System.out.println("Iron pickaxe mining speed: " + miningSpeed);
        assertTrue(miningSpeed > 3.0f && miningSpeed < 20.0f,
                "Iron pickaxe mining speed should be ~6, but is: " + miningSpeed);

        context.complete();
    }

    private void printComponentTree(com.sigmundgranaas.forgero.core.component.api.Component comp, int depth) {
        String indent = "  ".repeat(depth);
        System.out.println(indent + "- " + comp.id() + " (type: " + comp.getTypeIdentifier() + ")");
        // Print direct attributes
        var attrs = comp.properties(com.sigmundgranaas.forgero.core.attribute.api.Attribute.KEY);
        if (!attrs.isEmpty()) {
            System.out.println(indent + "  Attributes:");
            for (var attr : attrs) {
                System.out.println(indent + "    " + attr.type().path() + " = " + attr.value() +
                    (attr.condition().isPresent() ? " (conditioned)" : " (NO CONDITION!)"));
            }
        }
        // Recurse into children
        for (var child : comp.getChildren()) {
            printComponentTree(child, depth + 1);
        }
    }

    /**
     * CRITICAL: Validates oak pickaxe has correct wood-tier attributes.
     */
    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void oak_pickaxe_has_wood_tier_attributes(TestContext context) {
        var ctx = forgero(context);
        var query = com.sigmundgranaas.forgero.loader.api.ForgeroApi.itemQuery();

        var oakPickaxe = ctx.component("forgero:oak-pickaxe");
        assertTrue(oakPickaxe.isPresent(), "Oak pickaxe must exist");

        var stack = ctx.toStack(oakPickaxe.get());
        assertTrue(stack.isPresent(), "Oak pickaxe must convert to ItemStack");

        ItemStack oakStack = stack.get();

        // Oak pickaxe should have wood-tier durability (~59, NOT 1600!)
        int durability = query.getMaxDurability(oakStack);
        System.out.println("Oak pickaxe durability: " + durability);
        assertTrue(durability > 30 && durability < 200,
                "Oak pickaxe durability should be ~59 (wood tier), but is: " + durability);

        // Oak pickaxe should NOT have armor
        int armor = query.getArmor(oakStack);
        assertEquals(0, armor, "Oak pickaxe should have 0 armor, but has: " + armor);

        context.complete();
    }
}
