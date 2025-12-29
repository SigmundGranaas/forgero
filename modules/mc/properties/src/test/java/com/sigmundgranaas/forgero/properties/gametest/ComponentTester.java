package com.sigmundgranaas.forgero.properties.gametest;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.impl.StaticComponent;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.loader.api.ForgeroInitializedCallback;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.BlockBreakingProperty;
import com.sigmundgranaas.forgero.properties.minecraft.blockuse.BlockUseProperty;
import com.sigmundgranaas.forgero.properties.minecraft.entityuse.EntityUseProperty;
import com.sigmundgranaas.forgero.properties.minecraft.loot.LootProperty;
import com.sigmundgranaas.forgero.properties.minecraft.onhit.OnHitProperty;
import com.sigmundgranaas.forgero.properties.minecraft.onhitblock.OnHitBlockProperty;
import com.sigmundgranaas.forgero.properties.minecraft.ontick.OnTickProperty;
import com.sigmundgranaas.forgero.properties.minecraft.swing.SwingHandProperty;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.UseInteractionProperty;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ComponentTester {

	/**
	 * Gets the ComponentConverter using the static accessor.
	 */
	private static ComponentConverter getConverter() {
		return ForgeroInitializedCallback.getServices()
				.map(s -> s.converter())
				.orElse(null);
	}

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
							} else if (p instanceof OnHitBlockProperty) {
								return OnHitBlockProperty.PROPERTY_KEY.key();
							} else if (p instanceof OnTickProperty) {
								return OnTickProperty.PROPERTY_KEY.key();
							} else if (p instanceof SwingHandProperty) {
								return SwingHandProperty.PROPERTY_KEY.key();
							} else if (p instanceof EntityUseProperty) {
								return EntityUseProperty.PROPERTY_KEY.key();
							} else if (p instanceof BlockUseProperty) {
								return BlockUseProperty.PROPERTY_KEY.key();
							} else if (p instanceof LootProperty) {
								return LootProperty.PROPERTY_KEY.key();
							} else if (p instanceof UseInteractionProperty) {
								return UseInteractionProperty.PROPERTY_KEY.key();
							}
							throw new IllegalArgumentException("Unsupported property type for testing: " + p.getClass().getName());
						}),
						HashMap::new
				));

		Component component = new StaticComponent(id, openTags, propertiesMap);

		ComponentConverter converter = getConverter();
		if (converter == null) {
			return ItemStack.EMPTY;
		}
		return converter.toStack(component)
				.orElse(ItemStack.EMPTY);
	}

	/**
	 * Creates a dynamic ItemStack with both properties and attributes.
	 *
	 * @param name       A unique name for the test component.
	 * @param tags       A set of tags to determine the base item (e.g., "tool", "sword").
	 * @param properties A list of Forgero properties to attach to the component.
	 * @param attributes A map of attribute identifiers to their values.
	 * @return An ItemStack of a dynamic item with the component serialized into its NBT.
	 */
	public static ItemStack createStackWithAttributes(String name, Set<String> tags, List<Property> properties, Map<OpenIdentifier, Float> attributes) {
		OpenIdentifier id = new OpenIdentifier("forgero-test", name);
		Set<OpenIdentifier> openTags = tags.stream()
				.map(tag -> new OpenIdentifier("forgero", tag))
				.collect(Collectors.toSet());

		// Group properties by their key
		Map<String, List<?>> propertiesMap = new HashMap<>();

		if (!properties.isEmpty()) {
			propertiesMap = properties.stream()
					.collect(Collectors.collectingAndThen(
							Collectors.groupingBy(p -> {
								if (p instanceof BlockBreakingProperty) {
									return BlockBreakingProperty.PROPERTY_KEY.key();
								} else if (p instanceof OnHitProperty) {
									return OnHitProperty.PROPERTY_KEY.key();
								} else if (p instanceof OnHitBlockProperty) {
									return OnHitBlockProperty.PROPERTY_KEY.key();
								} else if (p instanceof OnTickProperty) {
									return OnTickProperty.PROPERTY_KEY.key();
								} else if (p instanceof SwingHandProperty) {
									return SwingHandProperty.PROPERTY_KEY.key();
								} else if (p instanceof EntityUseProperty) {
									return EntityUseProperty.PROPERTY_KEY.key();
								} else if (p instanceof BlockUseProperty) {
									return BlockUseProperty.PROPERTY_KEY.key();
								} else if (p instanceof LootProperty) {
									return LootProperty.PROPERTY_KEY.key();
								} else if (p instanceof UseInteractionProperty) {
									return UseInteractionProperty.PROPERTY_KEY.key();
								}
								throw new IllegalArgumentException("Unsupported property type for testing: " + p.getClass().getName());
							}),
							HashMap::new
					));
		}

		// Add attributes to the properties map
		if (!attributes.isEmpty()) {
			List<Attribute> attributeList = new ArrayList<>();
			for (Map.Entry<OpenIdentifier, Float> entry : attributes.entrySet()) {
				attributeList.add(new SimpleAttribute(entry.getKey(), entry.getValue()));
			}
			propertiesMap.put(Attribute.KEY.key(), attributeList);
		}

		Component component = new StaticComponent(id, openTags, propertiesMap);

		ComponentConverter converter = getConverter();
		if (converter == null) {
			return ItemStack.EMPTY;
		}
		return converter.toStack(component)
				.orElse(ItemStack.EMPTY);
	}

	/**
	 * Creates a dynamic ItemStack with only attributes (no properties).
	 *
	 * @param name       A unique name for the test component.
	 * @param tags       A set of tags to determine the base item.
	 * @param attributes A map of attribute identifiers to their values.
	 * @return An ItemStack of a dynamic item with the component serialized into its NBT.
	 */
	public static ItemStack createStackWithAttributes(String name, Set<String> tags, Map<OpenIdentifier, Float> attributes) {
		return createStackWithAttributes(name, tags, List.of(), attributes);
	}
}
