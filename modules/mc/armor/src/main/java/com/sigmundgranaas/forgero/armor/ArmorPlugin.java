package com.sigmundgranaas.forgero.armor;

import com.sigmundgranaas.forgero.armor.item.ForgeroArmorItem;
import com.sigmundgranaas.forgero.armor.item.ForgeroArmorMaterial;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.data.loading.api.data.host.CreateData;
import com.sigmundgranaas.forgero.loader.api.DataPlugin;
import com.sigmundgranaas.forgero.loader.api.PluginRegistrationContext;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;

import java.util.stream.Collectors;

/**
 * Forgero Armor data plugin.
 * Registers all armor item creators with the common loader.
 */
public class ArmorPlugin implements DataPlugin {
	public static final String ARMOR_ITEM_CLASS = "forgero:armor_item";

	@Override
	public void register(PluginRegistrationContext context) {
		context.registerItemCreator(ARMOR_ITEM_CLASS, this::createArmorItem);
	}

	@Override
	public String getId() {
		return "forgero:armor-plugin";
	}

	private Item createArmorItem(Component component, CreateData data, Resolver resolver) {
		var armorType = determineArmorType(component);
		if (armorType == null) {
			throw new IllegalArgumentException("Failed to create armor item for component '" + component.id() + "'. Could not determine armor type from tags.");
		}
		ForgeroArmorMaterial material = new ForgeroArmorMaterial(component, resolver);
		return new ForgeroArmorItem(material, armorType, new Item.Settings(), component);
	}

	private ArmorItem.Type determineArmorType(Component component) {
		var tags = component.getTags().stream().map(OpenIdentifier::name).collect(Collectors.toSet());
		if (tags.contains("helmet")) return ArmorItem.Type.HELMET;
		if (tags.contains("chestplate")) return ArmorItem.Type.CHESTPLATE;
		if (tags.contains("leggings")) return ArmorItem.Type.LEGGINGS;
		if (tags.contains("boots")) return ArmorItem.Type.BOOTS;
		return null;
	}
}
