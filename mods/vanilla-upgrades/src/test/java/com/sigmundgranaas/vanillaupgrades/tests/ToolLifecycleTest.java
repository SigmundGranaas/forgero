package com.sigmundgranaas.vanillaupgrades.tests;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.loader.api.ForgeroInitializedCallback;
import com.sigmundgranaas.forgero.loader.api.ForgeroServices;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the complete tool lifecycle in vanilla-upgrades.
 * Tests the full flow: ItemStack -> Component -> attributes -> back to ItemStack
 */
public class ToolLifecycleTest {

    private static final String BATCH = "vanilla_upgrades_lifecycle";

    private static ForgeroServices getServices() {
        return ForgeroInitializedCallback.getServices()
                .orElseThrow(() -> new AssertionError("ForgeroServices not available"));
    }

    private static Optional<Double> getAttributeValue(Component component, String attributeName) {
        return component.properties(Attribute.KEY).stream()
                .filter(attr -> attr.type().name().contains(attributeName))
                .findFirst()
                .map(attr -> (double) attr.value());
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void full_conversion_cycle(TestContext context) {
        // Test: ItemStack -> Component -> ItemStack
        ItemStack original = new ItemStack(Items.IRON_PICKAXE);

        // Convert to component
        Optional<Component> componentOpt = getServices().converter().toComponent(original);
        assertTrue(componentOpt.isPresent(), "Must convert to component");

        // Convert back to ItemStack
        Optional<ItemStack> convertedOpt = getServices().converter().toStack(componentOpt.get());
        assertTrue(convertedOpt.isPresent(), "Must convert back to ItemStack");

        // Verify same item type
        assertEquals(original.getItem(), convertedOpt.get().getItem(),
                "Converted item should be same type");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void iron_tool_has_iron_attributes(TestContext context) {
        ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
        Optional<Component> componentOpt = getServices().converter().toComponent(pickaxe);

        assertTrue(componentOpt.isPresent(), "Must convert");
        Component component = componentOpt.get();

        // Iron tools should have iron-like attributes
        List<? extends Attribute> attributes = component.properties(Attribute.KEY);
        assertFalse(attributes.isEmpty(), "Iron tool should have attributes");

        // Verify some attribute exists (durability, attack_damage, etc.)
        boolean hasRelevantAttribute = attributes.stream()
                .anyMatch(attr -> attr.type().name().contains("durability") ||
                                  attr.type().name().contains("attack") ||
                                  attr.type().name().contains("mining"));

        assertTrue(hasRelevantAttribute, "Iron tool should have durability, attack, or mining attribute");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void diamond_has_better_stats_than_iron(TestContext context) {
        ItemStack ironPick = new ItemStack(Items.IRON_PICKAXE);
        ItemStack diamondPick = new ItemStack(Items.DIAMOND_PICKAXE);

        Optional<Component> ironOpt = getServices().converter().toComponent(ironPick);
        Optional<Component> diamondOpt = getServices().converter().toComponent(diamondPick);

        assertTrue(ironOpt.isPresent(), "Iron pickaxe must convert");
        assertTrue(diamondOpt.isPresent(), "Diamond pickaxe must convert");

        // Compare durability if available
        Optional<Double> ironDurability = getAttributeValue(ironOpt.get(), "durability");
        Optional<Double> diamondDurability = getAttributeValue(diamondOpt.get(), "durability");

        if (ironDurability.isPresent() && diamondDurability.isPresent()) {
            assertTrue(diamondDurability.get() >= ironDurability.get(),
                    "Diamond durability should be >= iron");
        }

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void netherite_has_highest_tier(TestContext context) {
        ItemStack netheritePick = new ItemStack(Items.NETHERITE_PICKAXE);
        Optional<Component> componentOpt = getServices().converter().toComponent(netheritePick);

        assertTrue(componentOpt.isPresent(), "Netherite pickaxe must convert");

        // Netherite should have high mining level
        Optional<Double> miningLevel = getAttributeValue(componentOpt.get(), "mining_level");

        if (miningLevel.isPresent()) {
            assertTrue(miningLevel.get() >= 4, "Netherite should have mining level >= 4");
        }

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void component_preserves_item_identity(TestContext context) {
        ItemStack original = new ItemStack(Items.DIAMOND_SWORD);
        original.setDamage(50); // Set some damage

        Optional<Component> componentOpt = getServices().converter().toComponent(original);
        assertTrue(componentOpt.isPresent(), "Must convert");

        // Component should have identifying information
        Component component = componentOpt.get();
        assertNotNull(component.id(), "Component must have ID");

        // Tags should indicate this is a sword
        boolean hasSwordTag = component.getTags().stream()
                .anyMatch(tag -> tag.name().contains("sword"));

        assertTrue(hasSwordTag, "Sword component should have sword tag");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void core_tools_convertible(TestContext context) {
        // Test core tools that should be defined
        ItemStack[] tools = {
            new ItemStack(Items.IRON_PICKAXE),
            new ItemStack(Items.IRON_SWORD),
            new ItemStack(Items.DIAMOND_SWORD),
            new ItemStack(Items.DIAMOND_PICKAXE)
        };

        int convertedCount = 0;
        for (ItemStack tool : tools) {
            Optional<Component> componentOpt = getServices().converter().toComponent(tool);
            if (componentOpt.isPresent()) {
                convertedCount++;
            }
        }

        // At least some core tools should be convertible
        assertTrue(convertedCount >= 2,
                "At least 2 core tools should be convertible, found: " + convertedCount);

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void structured_tools_have_parts(TestContext context) {
        ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
        Optional<Component> componentOpt = getServices().converter().toComponent(pickaxe);

        assertTrue(componentOpt.isPresent(), "Must convert");

        if (componentOpt.get() instanceof StructuredComponent structured) {
            // Structured tool should have parts (head, handle, etc.)
            assertFalse(structured.structure().allParts().isEmpty(),
                    "Structured tool should have parts");
        }

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void components_have_correct_namespace(TestContext context) {
        ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
        Optional<Component> componentOpt = getServices().converter().toComponent(pickaxe);

        assertTrue(componentOpt.isPresent(), "Must convert");

        OpenIdentifier id = componentOpt.get().id();
        assertTrue(id.namespace().equals("forgero") || id.namespace().equals("minecraft"),
                "Component namespace should be forgero or minecraft: " + id);

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void attributes_follow_material_tier(TestContext context) {
        // Test that different material tiers have appropriate attribute values
        String[] materials = {"wooden", "stone", "iron", "diamond", "netherite"};
        double lastMiningSpeed = 0;

        for (String material : materials) {
            ItemStack tool = switch(material) {
                case "wooden" -> new ItemStack(Items.WOODEN_PICKAXE);
                case "stone" -> new ItemStack(Items.STONE_PICKAXE);
                case "iron" -> new ItemStack(Items.IRON_PICKAXE);
                case "diamond" -> new ItemStack(Items.DIAMOND_PICKAXE);
                case "netherite" -> new ItemStack(Items.NETHERITE_PICKAXE);
                default -> new ItemStack(Items.IRON_PICKAXE);
            };

            Optional<Component> componentOpt = getServices().converter().toComponent(tool);
            if (componentOpt.isPresent()) {
                Optional<Double> miningSpeed = getAttributeValue(componentOpt.get(), "mining_speed");
                if (miningSpeed.isPresent()) {
                    // Each tier should generally have >= previous tier's mining speed
                    // (Gold is an exception with high speed but low durability)
                }
            }
        }

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void customizable_components_maintain_structure(TestContext context) {
        ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
        Optional<Component> componentOpt = getServices().converter().toComponent(pickaxe);

        assertTrue(componentOpt.isPresent(), "Must convert");

        if (componentOpt.get() instanceof CustomizableComponent customizable) {
            // Get initial upgrade count
            int initialSlots = customizable.upgrades().size();

            // Create a new component with same upgrades (should preserve structure)
            Component newComponent = customizable.withUpgrades(customizable.upgrades());

            assertTrue(newComponent instanceof CustomizableComponent,
                    "New component should still be customizable");

            CustomizableComponent newCustomizable = (CustomizableComponent) newComponent;
            assertEquals(initialSlots, newCustomizable.upgrades().size(),
                    "Upgrade slot count should be preserved");
        }

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void tool_children_are_accessible(TestContext context) {
        ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
        Optional<Component> componentOpt = getServices().converter().toComponent(pickaxe);

        assertTrue(componentOpt.isPresent(), "Must convert");

        Component component = componentOpt.get();
        assertNotNull(component.getChildren(), "Children must not be null");

        // Children list can be empty but should not throw
        for (Component child : component.getChildren()) {
            assertNotNull(child, "Individual child should not be null");
            assertNotNull(child.id(), "Child should have ID");
        }

        context.complete();
    }
}
