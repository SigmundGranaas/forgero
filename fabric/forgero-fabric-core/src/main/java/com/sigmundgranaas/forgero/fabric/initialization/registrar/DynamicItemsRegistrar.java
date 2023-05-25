package com.sigmundgranaas.forgero.fabric.initialization.registrar;

import static com.sigmundgranaas.forgero.minecraft.common.item.Items.EMPTY_REPAIR_KIT;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.minecraft.common.registry.registrar.Registrar;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;


public class DynamicItemsRegistrar implements Registrar {

	public void registerDynamicItems() {
		if (ForgeroConfigurationLoader.configuration.enableRepairKits) {
			var kits = registerRepairKits();
		}
	}

	public List<Item> registerRepairKits() {
		return ForgeroStateRegistry.TREE.find(Type.TOOL_MATERIAL)
				.map(node -> node.getResources(State.class))
				.orElse(ImmutableList.<State>builder().build())
				.stream()
				.map(material -> new Identifier(Forgero.NAMESPACE, material.name() + "_repair_kit"))
				.map(identifier -> Registry.register(Registry.ITEM, identifier, new Item(new Item.Settings().group(ItemGroup.TOOLS).recipeRemainder(EMPTY_REPAIR_KIT))))
				.toList();
	}

	@Override
	public void register() {
		if (ForgeroConfigurationLoader.configuration.enableRepairKits) {
			registerDynamicItems();
		}
	}
}

