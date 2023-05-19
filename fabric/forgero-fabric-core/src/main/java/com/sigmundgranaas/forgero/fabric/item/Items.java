package com.sigmundgranaas.forgero.fabric.item;


import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;

import static com.sigmundgranaas.forgero.minecraft.common.item.Items.EMPTY_REPAIR_KIT;

public class Items {
    static {
	    ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(EMPTY_REPAIR_KIT));
    }
}
