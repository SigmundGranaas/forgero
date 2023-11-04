package com.sigmundgranaas.forgero.repairkit;

import static net.minecraft.util.registry.Registry.ITEM;

import com.sigmundgranaas.forgero.fabric.api.entrypoint.ForgeroInitializedEntryPoint;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import com.sigmundgranaas.forgero.repairkit.item.RepairKitItem;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RepairKitInitializer implements ForgeroInitializedEntryPoint {
	public static Item REPAIR_KIT_SUPER;

	@Override
	public void onInitialized(StateService service) {
		REPAIR_KIT_SUPER = Registry.register(ITEM, new Identifier("forgero:super_repair"), new RepairKitItem(new Item.Settings().group(ItemGroup.TOOLS)));
	}
}
