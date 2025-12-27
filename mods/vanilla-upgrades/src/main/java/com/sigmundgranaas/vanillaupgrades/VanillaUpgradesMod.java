package com.sigmundgranaas.vanillaupgrades;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Vanilla Upgrades - Add upgrade slots to vanilla Minecraft tools.
 *
 * This mod allows players to enhance their vanilla tools with:
 * - Binding materials (leather, silk) for improved grip and durability
 * - Tip reinforcements (metals, blaze rods) for special effects
 * - Gems (diamond, emerald) for magical properties
 *
 * All upgrade slot definitions are loaded from the vanilla-upgrades-base content pack.
 * Materials are provided by the shared-materials content pack.
 */
public class VanillaUpgradesMod implements ModInitializer {
    public static final String MOD_ID = "vanilla-upgrades";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Vanilla Upgrades initialized - upgrade slots enabled for vanilla tools!");
    }
}
