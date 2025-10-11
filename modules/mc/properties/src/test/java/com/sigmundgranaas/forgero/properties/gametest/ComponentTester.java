package com.sigmundgranaas.forgero.properties.gametest;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.impl.StaticComponent;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.loader.api.ForgeroApi;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.BlockBreakingProperty;
import com.sigmundgranaas.forgero.properties.minecraft.loot.LootProperty;
import com.sigmundgranaas.forgero.properties.minecraft.onhit.OnHitProperty;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ComponentTester {

	/**
	 * Creates a dynamic ItemStack on-the-fly for testing purposes.
	 *
	 * @param name       A unique name for the test component.
	 * @param tags       A set of tags to determine the base item (e.g., "tool", "sword").
	 * @param properties A list of Forgero properties to attach to the component.
	 * @return An ItemStack of a dynamic item with the component serialized into its NBT.
	 */
	public static ItemStack createStack(String name, Set<String> tags, List<Property> properties) {
		OpenIdentifier id = new OpenIdentifier("forgero-test", name);
		Set<OpenIdentifier> openTags = tags.stream()
				.map(tag -> new OpenIdentifier("forgero", tag))
				.collect(Collectors.toSet());

		// Group properties by their key, as required by the Component constructor
		Map<String, List<?>> propertiesMap = properties.stream()
				.collect(Collectors.collectingAndThen(
						Collectors.groupingBy(p -> {
							if (p instanceof BlockBreakingProperty) {
								return BlockBreakingProperty.PROPERTY_KEY.key();
							} else if (p instanceof OnHitProperty) {
								return OnHitProperty.PROPERTY_KEY.key();
							} else if (p instanceof LootProperty) {
								return LootProperty.PROPERTY_KEY.key();
							}
							throw new IllegalArgumentException("Unsupported property type for testing: " + p.getClass().getName());
						}),
						HashMap::new
				));

		Component component = new StaticComponent(id, openTags, propertiesMap);

		return ForgeroApi.converter().toStack(component)
				.orElse(ItemStack.EMPTY);
	}
}
