package com.sigmundgranaas.forgero.tools;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.data.loading.api.data.host.CreateData;
import com.sigmundgranaas.forgero.common.api.DataPlugin;
import com.sigmundgranaas.forgero.common.api.PluginRegistrationContext;
import com.sigmundgranaas.forgero.tools.item.ForgeroAxeItem;
import com.sigmundgranaas.forgero.tools.item.ForgeroHoeItem;
import com.sigmundgranaas.forgero.tools.item.ForgeroPartItem;
import com.sigmundgranaas.forgero.tools.item.ForgeroPickaxeItem;
import com.sigmundgranaas.forgero.tools.item.ForgeroShovelItem;
import com.sigmundgranaas.forgero.tools.item.ForgeroSwordItem;
import com.sigmundgranaas.forgero.tools.item.ForgeroToolMaterial;

import net.minecraft.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToolPlugin implements DataPlugin {
	private static final Logger LOGGER = LoggerFactory.getLogger(ToolPlugin.class);
	public static final String PICKAXE_ITEM_CLASS = "forgero:pickaxe_item";
	public static final String SWORD_ITEM_CLASS = "forgero:sword_item";
	public static final String AXE_ITEM_CLASS = "forgero:axe_item";
	public static final String SHOVEL_ITEM_CLASS = "forgero:shovel_item";
	public static final String HOE_ITEM_CLASS = "forgero:hoe_item";
	public static final String PART_ITEM_CLASS = "forgero:part_item";

	@Override
	public String getId() {
		return "forgero:tools-plugin";
	}

	@Override
	public void register(PluginRegistrationContext context) {
		context.registerItemCreator(PICKAXE_ITEM_CLASS, this::createPickaxeItem);
		context.registerItemCreator(SWORD_ITEM_CLASS, this::createSwordItem);
		context.registerItemCreator(AXE_ITEM_CLASS, this::createAxeItem);
		context.registerItemCreator(SHOVEL_ITEM_CLASS, this::createShovelItem);
		context.registerItemCreator(HOE_ITEM_CLASS, this::createHoeItem);
		context.registerItemCreator(PART_ITEM_CLASS, this::createPartItem);
	}

	private Item createPickaxeItem(Component component, CreateData data) {
		ForgeroToolMaterial material = new ForgeroToolMaterial(component);
		return new ForgeroPickaxeItem(material, new Item.Settings(), component);
	}

	private Item createSwordItem(Component component, CreateData data) {
		ForgeroToolMaterial material = new ForgeroToolMaterial(component);
		return new ForgeroSwordItem(material, new Item.Settings(), component);
	}

	private Item createAxeItem(Component component, CreateData data) {
		ForgeroToolMaterial material = new ForgeroToolMaterial(component);
		return new ForgeroAxeItem(material, new Item.Settings(), component);
	}

	private Item createShovelItem(Component component, CreateData data) {
		ForgeroToolMaterial material = new ForgeroToolMaterial(component);
		return new ForgeroShovelItem(material, new Item.Settings(), component);
	}

	private Item createHoeItem(Component component, CreateData data) {
		ForgeroToolMaterial material = new ForgeroToolMaterial(component);
		return new ForgeroHoeItem(material, new Item.Settings(), component);
	}

	private Item createPartItem(Component component, CreateData data) {
		return new ForgeroPartItem(new Item.Settings(), component);
	}
}
