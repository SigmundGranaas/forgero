package com.sigmundgranaas.forgero.minecraft.common.registry.registrar;

import static com.sigmundgranaas.forgero.minecraft.common.item.Items.EMPTY_REPAIR_KIT;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class DynamicItemsRegistrar implements Registrar {
	public void registerRepairKits() {
		ForgeroStateRegistry.TREE.find(Type.TOOL_MATERIAL)
				.map(node -> node.getResources(State.class))
				.orElse(ImmutableList.<State>builder().build())
				.stream()
				.map(material -> new Identifier(Forgero.NAMESPACE, material.name() + "_repair_kit"))
				.forEach(identifier -> Registry.register(Registry.ITEM, identifier, new Item(new Item.Settings().group(ItemGroup.MISC).recipeRemainder(EMPTY_REPAIR_KIT))));
	}

	@Override
	public void register() {
		if (ForgeroConfigurationLoader.configuration.enableRepairKits) {
			registerRepairKits();
		}
	}
}
