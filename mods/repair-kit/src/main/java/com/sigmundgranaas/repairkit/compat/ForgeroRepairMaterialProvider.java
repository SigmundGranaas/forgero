package com.sigmundgranaas.repairkit.compat;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.loader.api.ForgeroApi;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.Set;

/**
 * Repair material provider for Forgero tools and equipment.
 *
 * This provider extracts the primary material from Forgero components
 * and returns the corresponding repair ingredient based on the material's
 * host item mapping.
 *
 * Priority is set higher than vanilla (10) so Forgero items are handled
 * before the vanilla fallback.
 */
public class ForgeroRepairMaterialProvider implements RepairMaterialProvider {

    public static final ForgeroRepairMaterialProvider INSTANCE = new ForgeroRepairMaterialProvider();

    /**
     * Part types that are considered "primary" for repair material determination.
     * Order matters - first match wins.
     */
    private static final Set<String> PRIMARY_PART_TYPES = Set.of(
            "head", "axe_head", "pickaxe_head", "shovel_head", "hoe_head",
            "blade", "sword_blade",
            "plate", "chestplate", "helmet", "leggings", "boots"
    );

    private ForgeroRepairMaterialProvider() {
    }

    @Override
    public Optional<Ingredient> getRepairIngredient(ItemStack stack) {
        // Check if Forgero API is available and initialized
        if (!ForgeroApi.isInitialized()) {
            return Optional.empty();
        }

        // Try to convert the item to a Forgero component
        Optional<Component> componentOpt = ForgeroApi.converter().toComponent(stack);
        if (componentOpt.isEmpty()) {
            return Optional.empty();
        }

        Component component = componentOpt.get();

        // Extract the primary material from the component
        Optional<Item> repairItem = extractPrimaryMaterial(component);

        return repairItem.map(Ingredient::ofItems);
    }

    /**
     * Extract the primary repair material from a Forgero component.
     *
     * For structured components (tools, weapons, armor), this looks for the
     * "head" or primary part and gets its material.
     *
     * For simple components (materials, parts), it gets the material directly.
     */
    private Optional<Item> extractPrimaryMaterial(Component component) {
        // If it's a structured component, find the primary part
        if (component instanceof StructuredComponent structured) {
            return extractFromStructure(structured);
        }

        // For simple components, try to get the material directly from host mapping
        return getMaterialItem(component);
    }

    /**
     * Extract repair material from a structured component (tool, weapon, armor).
     */
    private Optional<Item> extractFromStructure(StructuredComponent structured) {
        ComponentStructure structure = structured.structure();

        // Try to find a primary part (head, blade, plate, etc.)
        for (ComponentPart part : structure.allParts()) {
            String partType = part.partType().path().toLowerCase();

            if (isPrimaryPartType(partType)) {
                Optional<Item> materialItem = getMaterialItem(part.getContent());
                if (materialItem.isPresent()) {
                    return materialItem;
                }
            }
        }

        // Fallback: try any part that has a material
        for (ComponentPart part : structure.allParts()) {
            Optional<Item> materialItem = getMaterialItem(part.getContent());
            if (materialItem.isPresent()) {
                return materialItem;
            }
        }

        return Optional.empty();
    }

    /**
     * Check if a part type is considered "primary" for repair purposes.
     */
    private boolean isPrimaryPartType(String partType) {
        for (String primary : PRIMARY_PART_TYPES) {
            if (partType.contains(primary)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the repair material item from a component.
     *
     * This uses the component's name/ID to find the corresponding material
     * and then maps it to a Minecraft item via the host identifiers.
     */
    private Optional<Item> getMaterialItem(Component component) {
        // Get the component's identifier
        String componentId = component.id().path();

        // Extract the material prefix (e.g., "iron" from "iron-axe_head")
        String materialName = extractMaterialName(componentId);
        if (materialName == null) {
            return Optional.empty();
        }

        // Try common material -> item mappings
        return resolveVanillaMaterial(materialName);
    }

    /**
     * Extract the material name from a component ID.
     * E.g., "iron-axe_head" -> "iron", "diamond-sword" -> "diamond"
     */
    private String extractMaterialName(String componentId) {
        // Forgero uses format: "material-part_type" or "material-equipment"
        int dashIndex = componentId.indexOf('-');
        if (dashIndex > 0) {
            return componentId.substring(0, dashIndex).toLowerCase();
        }
        return null;
    }

    /**
     * Resolve a material name to its vanilla repair item.
     *
     * This handles common vanilla materials. Modded materials would need
     * additional mappings or a registry lookup.
     */
    private Optional<Item> resolveVanillaMaterial(String materialName) {
        // Map common material names to their repair items
        String itemId = switch (materialName) {
            case "iron" -> "minecraft:iron_ingot";
            case "gold", "golden" -> "minecraft:gold_ingot";
            case "diamond" -> "minecraft:diamond";
            case "netherite" -> "minecraft:netherite_ingot";
            case "stone", "cobblestone" -> "minecraft:cobblestone";
            case "wood", "wooden", "oak", "birch", "spruce", "jungle", "acacia", "dark_oak", "mangrove", "cherry" ->
                    "minecraft:oak_planks"; // Any planks work
            case "leather" -> "minecraft:leather";
            case "chainmail", "chain" -> "minecraft:iron_ingot"; // Chainmail uses iron
            case "copper" -> "minecraft:copper_ingot";
            case "amethyst" -> "minecraft:amethyst_shard";
            case "lapis" -> "minecraft:lapis_lazuli";
            case "redstone" -> "minecraft:redstone";
            case "emerald" -> "minecraft:emerald";
            case "quartz" -> "minecraft:quartz";
            case "obsidian" -> "minecraft:obsidian";
            case "flint" -> "minecraft:flint";
            case "bone" -> "minecraft:bone";
            case "blaze" -> "minecraft:blaze_rod";
            case "ender", "enderpearl" -> "minecraft:ender_pearl";
            case "prismarine" -> "minecraft:prismarine_shard";
            default -> null;
        };

        if (itemId != null) {
            Item item = Registries.ITEM.get(new Identifier(itemId));
            if (item != null && item != Registries.ITEM.get(new Identifier("minecraft:air"))) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    @Override
    public int getPriority() {
        // Higher than vanilla (0) so Forgero items are handled first
        return 10;
    }
}
