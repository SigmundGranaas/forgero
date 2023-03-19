package com.sigmundgranaas.forgero.fabric.item;

import static com.sigmundgranaas.forgero.minecraft.common.item.Items.EMPTY_REPAIR_KIT;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;


import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

public class DynamicItems {

	public static void registerDynamicItems() {
		if (ForgeroConfigurationLoader.configuration.enableRepairKits) {
			var kits = registerRepairKits();

			kits.forEach(kit -> ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(kit)));
		}
	}

	public static List<Item> registerRepairKits() {
		return ForgeroStateRegistry.TREE.find(Type.TOOL_MATERIAL)
				.map(node -> node.getResources(State.class))
				.orElse(ImmutableList.<State>builder().build())
				.stream()
				.map(material -> new Identifier(Forgero.NAMESPACE, material.name() + "_repair_kit"))
				.map(identifier -> Registry.register(Registries.ITEM, identifier, new Item(new Item.Settings().recipeRemainder(EMPTY_REPAIR_KIT))))
				.toList();
	}
}
