package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for attribute leak bugs.
 *
 * These tests guard against specific bugs that were found and fixed:
 * 1. Tools having non-zero armor values (armor should only apply to armor pieces)
 * 2. Durability values being wildly inflated due to wrong default component selection
 * 3. Default handle selection using wrong component
 *
 * If any of these tests fail, it indicates a regression in the attribute system
 * or component template configuration.
 *
 * @see <a href="https://github.com/sigmundgranaas/forgero/issues/XXX">Original bug report</a>
 */
public class AttributeLeakRegressionTest implements ForgeroGameTest {

    private float getAttributeValue(Component component, String attributeType) {
        return component.properties(Attribute.KEY).stream()
                .filter(attr -> attr.type().toString().equals(attributeType))
                .findFirst()
                .map(Attribute::value)
                .orElse(0f);
    }

    private Optional<Float> getOptionalAttributeValue(Component component, String attributeType) {
        return component.properties(Attribute.KEY).stream()
                .filter(attr -> attr.type().toString().equals(attributeType))
                .findFirst()
                .map(Attribute::value);
    }

    // ==================== Armor Leak Tests ====================
    // Bug: Tools were showing non-zero armor values (e.g., pickaxe with 5 armor)
    // Root cause: Armor attributes from materials were leaking to tools due to missing conditions

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void tools_must_have_zero_armor(TestContext context) {
        String[] tools = {
            "iron_pickaxe", "iron_sword", "iron_axe", "iron_shovel", "iron_hoe",
            "diamond_pickaxe", "diamond_sword", "diamond_axe", "diamond_shovel",
            "netherite_pickaxe", "netherite_sword", "netherite_axe",
            "golden_pickaxe", "golden_sword", "golden_axe",
            "stone_pickaxe", "stone_sword", "stone_axe",
            "oak_pickaxe", "oak_sword", "oak_axe"
        };

        for (String toolName : tools) {
            var toolOpt = forgero(context).component("forgero:" + toolName);
            if (toolOpt.isEmpty()) {
                continue; // Skip tools that don't exist in this configuration
            }

            Component tool = toolOpt.get();
            float armor = getAttributeValue(tool, "forgero:armor");

            assertEquals(0f, armor,
                    String.format("REGRESSION: %s has armor value %.1f but tools must have 0 armor! " +
                            "This indicates attribute leak from materials.", toolName, armor));
        }

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void tools_must_have_zero_armor_toughness(TestContext context) {
        String[] tools = {
            "iron_pickaxe", "diamond_pickaxe", "netherite_pickaxe",
            "iron_sword", "diamond_sword", "netherite_sword"
        };

        for (String toolName : tools) {
            var toolOpt = forgero(context).component("forgero:" + toolName);
            if (toolOpt.isEmpty()) {
                continue;
            }

            Component tool = toolOpt.get();
            float toughness = getAttributeValue(tool, "forgero:armor_toughness");

            assertEquals(0f, toughness,
                    String.format("REGRESSION: %s has armor_toughness value %.1f but tools must have 0! " +
                            "This indicates attribute leak from armor materials.", toolName, toughness));
        }

        context.complete();
    }

    // ==================== Durability Sanity Tests ====================
    // Bug: Oak pickaxe had 1600 durability instead of ~59
    // Root cause: Wrong default handle was selected (diamond instead of wooden)

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void wood_tier_tools_have_reasonable_durability(TestContext context) {
        String[] woodTools = {"oak_pickaxe", "oak_sword", "oak_axe", "oak_shovel", "oak_hoe"};

        for (String toolName : woodTools) {
            var toolOpt = forgero(context).component("forgero:" + toolName);
            if (toolOpt.isEmpty()) {
                continue;
            }

            Component tool = toolOpt.get();
            float durability = getAttributeValue(tool, "forgero:durability");

            // Wood tier durability should be between 50-150 (vanilla wood is 59)
            // Allow some buffer for wooden handle contribution
            assertTrue(durability >= 50 && durability <= 200,
                    String.format("REGRESSION: %s has durability %.0f which is outside reasonable range [50-200]. " +
                            "Vanilla wood tier is 59. This may indicate wrong default component selection.",
                            toolName, durability));
        }

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void iron_tier_tools_have_reasonable_durability(TestContext context) {
        String[] ironTools = {"iron_pickaxe", "iron_sword", "iron_axe", "iron_shovel", "iron_hoe"};

        for (String toolName : ironTools) {
            var toolOpt = forgero(context).component("forgero:" + toolName);
            if (toolOpt.isEmpty()) {
                continue;
            }

            Component tool = toolOpt.get();
            float durability = getAttributeValue(tool, "forgero:durability");

            // Iron tier durability should be between 200-400 (vanilla iron is 250)
            assertTrue(durability >= 200 && durability <= 400,
                    String.format("REGRESSION: %s has durability %.0f which is outside reasonable range [200-400]. " +
                            "Vanilla iron tier is 250. This may indicate attribute leak or wrong component selection.",
                            toolName, durability));
        }

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void diamond_tier_tools_have_reasonable_durability(TestContext context) {
        String[] diamondTools = {"diamond_pickaxe", "diamond_sword", "diamond_axe", "diamond_shovel"};

        for (String toolName : diamondTools) {
            var toolOpt = forgero(context).component("forgero:" + toolName);
            if (toolOpt.isEmpty()) {
                continue;
            }

            Component tool = toolOpt.get();
            float durability = getAttributeValue(tool, "forgero:durability");

            // Diamond tier durability should be between 1400-1700 (vanilla diamond is 1561)
            assertTrue(durability >= 1400 && durability <= 1700,
                    String.format("REGRESSION: %s has durability %.0f which is outside reasonable range [1400-1700]. " +
                            "Vanilla diamond tier is 1561. This may indicate attribute issues.",
                            toolName, durability));
        }

        context.complete();
    }

    // ==================== Default Handle Tests ====================
    // Bug: Tools were using random material handles instead of wooden_handle
    // Root cause: Template used default_tag instead of explicit default component

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void tools_use_wooden_handle_as_default(TestContext context) {
        // Template-generated tools use hyphen format (iron-pickaxe)
        String[] tools = {"iron-pickaxe", "diamond-pickaxe", "netherite-pickaxe"};

        for (String toolName : tools) {
            var toolOpt = forgero(context).component("forgero:" + toolName);
            if (toolOpt.isEmpty()) {
                continue;
            }

            Component tool = toolOpt.get();

            // Check the component tree for wooden_handle
            boolean hasWoodenHandle = containsWoodenHandle(tool);

            assertTrue(hasWoodenHandle,
                    String.format("REGRESSION: %s does not use wooden_handle as its default handle! " +
                            "All tools should use the static wooden_handle component. " +
                            "Children found: %s",
                            toolName,
                            tool.getChildren().stream()
                                    .map(c -> c.id().toString())
                                    .toList()));
        }

        context.complete();
    }

    /**
     * Recursively checks if a component or any of its children is the wooden_handle.
     */
    private boolean containsWoodenHandle(Component component) {
        if (component.id().toString().contains("wooden_handle")) {
            return true;
        }
        return component.getChildren().stream()
                .anyMatch(this::containsWoodenHandle);
    }

    // ==================== Attribute Magnitude Tests ====================
    // General sanity checks to catch wild attribute values

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void no_tool_has_extreme_durability(TestContext context) {
        // Maximum reasonable durability is netherite (2031) + some upgrades
        // Anything above 3000 is suspicious
        float maxReasonableDurability = 3000f;

        String[] allTools = {
            "iron_pickaxe", "iron_sword", "iron_axe",
            "diamond_pickaxe", "diamond_sword", "diamond_axe",
            "netherite_pickaxe", "netherite_sword", "netherite_axe",
            "golden_pickaxe", "golden_sword",
            "oak_pickaxe", "oak_sword", "oak_axe"
        };

        for (String toolName : allTools) {
            var toolOpt = forgero(context).component("forgero:" + toolName);
            if (toolOpt.isEmpty()) {
                continue;
            }

            Component tool = toolOpt.get();
            float durability = getAttributeValue(tool, "forgero:durability");

            assertTrue(durability <= maxReasonableDurability,
                    String.format("REGRESSION: %s has extreme durability %.0f (max reasonable is %.0f). " +
                            "This likely indicates attribute stacking bug or wrong component selection.",
                            toolName, durability, maxReasonableDurability));
        }

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void no_tool_has_extreme_attack_damage(TestContext context) {
        // Maximum reasonable attack damage is netherite sword (8) + some bonuses
        // Anything above 15 is suspicious for a base tool
        float maxReasonableDamage = 15f;

        String[] allTools = {
            "iron_pickaxe", "iron_sword", "iron_axe",
            "diamond_pickaxe", "diamond_sword", "diamond_axe",
            "netherite_pickaxe", "netherite_sword", "netherite_axe"
        };

        for (String toolName : allTools) {
            var toolOpt = forgero(context).component("forgero:" + toolName);
            if (toolOpt.isEmpty()) {
                continue;
            }

            Component tool = toolOpt.get();
            float damage = getAttributeValue(tool, "forgero:attack_damage");

            assertTrue(damage <= maxReasonableDamage,
                    String.format("REGRESSION: %s has extreme attack damage %.1f (max reasonable is %.1f). " +
                            "This likely indicates attribute stacking bug.",
                            toolName, damage, maxReasonableDamage));
        }

        context.complete();
    }
}
