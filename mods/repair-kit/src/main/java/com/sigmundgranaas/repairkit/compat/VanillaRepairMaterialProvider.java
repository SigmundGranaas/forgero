package com.sigmundgranaas.repairkit.compat;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.ShearsItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.TridentItem;
import net.minecraft.recipe.Ingredient;

import java.util.Optional;

/**
 * Provider for vanilla Minecraft repair materials.
 *
 * Handles:
 * - ToolItem: Uses ToolMaterial.getRepairIngredient()
 * - ArmorItem: Uses ArmorMaterial.getRepairIngredient()
 * - SwordItem: Uses ToolMaterial.getRepairIngredient()
 * - Other damageable items: Returns empty (can be handled by other providers)
 */
public class VanillaRepairMaterialProvider implements RepairMaterialProvider {

    public static final VanillaRepairMaterialProvider INSTANCE = new VanillaRepairMaterialProvider();

    private VanillaRepairMaterialProvider() {
    }

    @Override
    public Optional<Ingredient> getRepairIngredient(ItemStack stack) {
        Item item = stack.getItem();

        // Check ToolItem (pickaxe, axe, shovel, hoe)
        if (item instanceof ToolItem toolItem) {
            ToolMaterial material = toolItem.getMaterial();
            Ingredient repairIngredient = material.getRepairIngredient();
            if (repairIngredient != null && !repairIngredient.isEmpty()) {
                return Optional.of(repairIngredient);
            }
        }

        // Check SwordItem
        if (item instanceof SwordItem swordItem) {
            ToolMaterial material = swordItem.getMaterial();
            Ingredient repairIngredient = material.getRepairIngredient();
            if (repairIngredient != null && !repairIngredient.isEmpty()) {
                return Optional.of(repairIngredient);
            }
        }

        // Check ArmorItem
        if (item instanceof ArmorItem armorItem) {
            ArmorMaterial material = armorItem.getMaterial();
            Ingredient repairIngredient = material.getRepairIngredient();
            if (repairIngredient != null && !repairIngredient.isEmpty()) {
                return Optional.of(repairIngredient);
            }
        }

        // Check TridentItem - uses prismarine
        if (item instanceof TridentItem) {
            return Optional.of(Ingredient.ofItems(Items.PRISMARINE_SHARD));
        }

        // Check ShieldItem - uses planks
        if (item instanceof ShieldItem) {
            return Optional.of(Ingredient.fromTag(net.minecraft.registry.tag.ItemTags.PLANKS));
        }

        // Check Elytra - uses phantom membrane
        if (item instanceof ElytraItem) {
            return Optional.of(Ingredient.ofItems(Items.PHANTOM_MEMBRANE));
        }

        // Check CrossbowItem and BowItem - uses string
        if (item instanceof CrossbowItem || item instanceof BowItem) {
            return Optional.of(Ingredient.ofItems(Items.STRING));
        }

        // Check FishingRodItem - uses string
        if (item instanceof FishingRodItem) {
            return Optional.of(Ingredient.ofItems(Items.STRING));
        }

        // Check Shears - uses iron ingots
        if (item instanceof ShearsItem) {
            return Optional.of(Ingredient.ofItems(Items.IRON_INGOT));
        }

        // Check FlintAndSteelItem - uses iron ingots
        if (item instanceof FlintAndSteelItem) {
            return Optional.of(Ingredient.ofItems(Items.IRON_INGOT));
        }

        return Optional.empty();
    }

    @Override
    public int getPriority() {
        return 0; // Base priority for vanilla provider
    }
}
