package com.sigmundgranaas.repairkit;

import com.sigmundgranaas.repairkit.item.RepairKitItem;
import com.sigmundgranaas.repairkit.item.RepairKitTier;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Registry for all Repair Kit items.
 */
public final class RepairKitItems {

    public static final RepairKitItem SCRAPPY_REPAIR_KIT = new RepairKitItem(
            RepairKitTier.SCRAPPY,
            new FabricItemSettings()
    );

    public static final RepairKitItem REPAIR_KIT = new RepairKitItem(
            RepairKitTier.STANDARD,
            new FabricItemSettings()
    );

    public static final RepairKitItem REFINED_REPAIR_KIT = new RepairKitItem(
            RepairKitTier.REFINED,
            new FabricItemSettings()
    );

    public static final RepairKitItem MASTERCRAFT_REPAIR_KIT = new RepairKitItem(
            RepairKitTier.MASTERCRAFT,
            new FabricItemSettings()
    );

    private RepairKitItems() {
    }

    public static void register() {
        registerItem("scrappy_repair_kit", SCRAPPY_REPAIR_KIT);
        registerItem("repair_kit", REPAIR_KIT);
        registerItem("refined_repair_kit", REFINED_REPAIR_KIT);
        registerItem("mastercraft_repair_kit", MASTERCRAFT_REPAIR_KIT);

        // Add to creative tab
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(content -> {
            content.add(SCRAPPY_REPAIR_KIT);
            content.add(REPAIR_KIT);
            content.add(REFINED_REPAIR_KIT);
            content.add(MASTERCRAFT_REPAIR_KIT);
        });

        RepairKitMod.LOGGER.info("Registered 4 repair kit items");
    }

    private static void registerItem(String name, Item item) {
        Registry.register(Registries.ITEM, new Identifier(RepairKitMod.MOD_ID, name), item);
    }
}
