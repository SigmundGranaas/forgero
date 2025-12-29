package com.sigmundgranaas.repairkit;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Repair Kit - Tiered repair kits for tool and equipment maintenance.
 *
 * Features:
 * - 4 tiers: Scrappy, Standard, Refined, Mastercraft
 * - Fill kits with repair materials (up to 64)
 * - Repair via crafting grid or offhand interaction
 * - Works with vanilla tools and armor
 * - Extensible provider system for mod compatibility
 */
public class RepairKitMod implements ModInitializer {
    public static final String MOD_ID = "repair-kit";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Register items
        RepairKitItems.register();

        // Register recipes
        RepairKitRecipes.register();

        // Initialize repair material providers
        RepairMaterialProviders.register();

        LOGGER.info("Repair Kit initialized - 4 tiers of repair kits available!");
    }
}
