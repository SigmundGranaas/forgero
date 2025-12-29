package com.sigmundgranaas.forgero.tools;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.data.loading.api.data.host.CreateData;
import com.sigmundgranaas.forgero.loader.api.DataPlugin;
import com.sigmundgranaas.forgero.loader.api.PluginRegistrationContext;
import com.sigmundgranaas.forgero.tools.item.ForgeroPartItem;
import com.sigmundgranaas.forgero.tools.item.ForgeroPickaxeItem;
import com.sigmundgranaas.forgero.tools.item.ForgeroToolMaterial;

import net.minecraft.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToolPlugin implements DataPlugin {
	private static final Logger LOGGER = LoggerFactory.getLogger(ToolPlugin.class);
	public static final String PICKAXE_ITEM_CLASS = "forgero:pickaxe_item";
	public static final String PART_ITEM_CLASS = "forgero:part_item";

	@Override
	public String getId() {
		return "forgero:tools-plugin";
	}

	@Override
	public void register(PluginRegistrationContext context) {
		context.registerItemCreator(PICKAXE_ITEM_CLASS, this::createPickaxeItem);
		context.registerItemCreator(PART_ITEM_CLASS, this::createPartItem);
	}


	private Item createPickaxeItem(Component component, CreateData data, Resolver resolver) {
		ForgeroToolMaterial material = new ForgeroToolMaterial(component, resolver);
		return new ForgeroPickaxeItem(material, new Item.Settings(), component, resolver);
	}

	private Item createPartItem(Component component, CreateData data, Resolver resolver) {
		return new ForgeroPartItem(new Item.Settings(), component, resolver);
	}
}
