package com.sigmundgranaas.forgero.armor;

import com.sigmundgranaas.forgero.armor.item.ForgeroArmorItem;
import com.sigmundgranaas.forgero.armor.item.ForgeroArmorMaterial;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.registrar.ForgeroDataRegistrar;
import com.sigmundgranaas.forgero.common.registrar.ItemCreator;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.data.loading.api.data.host.CreateData;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataBundle;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataInitializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ArmorInitializer implements ModInitializer {
	public static final String MOD_NAMESPACE = "forgero";
	public static final Logger LOGGER = LoggerFactory.getLogger(ArmorInitializer.class);
	public static final String ARMOR_ITEM_CLASS = "forgero:armor_item";

	@Override
	public void onInitialize() {
		long totalStartTime = System.currentTimeMillis();
		LOGGER.info("Starting Forgero Armor module initialization.");

		// In a mature architecture, this data loading step would be centralized
		// to avoid each module re-loading the same data.
		ForgeroDataInitializer dataInitializer = new ForgeroDataInitializer(MOD_NAMESPACE);
		ForgeroDataBundle bundle = dataInitializer.getDataBundle();
		LOGGER.info("Data bundle loaded with {} component entries.", bundle.componentRegistry().all().size());

		ForgeroDataRegistrar registrar = new ForgeroDataRegistrar(LOGGER);

		Map<String, ItemCreator> creators = new HashMap<>();
		creators.put(ARMOR_ITEM_CLASS, this::createArmorItem);

		registrar.process(bundle, creators);

		long totalEndTime = System.currentTimeMillis();
		LOGGER.info("Forgero Armor module initialization complete. Took {}ms.", totalEndTime - totalStartTime);
	}

	private Item createArmorItem(Component component, CreateData data, Resolver resolver) {
		var armorType = determineArmorType(component);
		if (armorType == null) {
			// Throw an exception to be caught by the registrar, ensuring clear error logging.
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
