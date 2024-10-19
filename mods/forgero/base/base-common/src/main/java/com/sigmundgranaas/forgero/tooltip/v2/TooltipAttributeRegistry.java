package com.sigmundgranaas.forgero.tooltip.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.type.Type;

/**
 * Central registry for tooltip filters and writable attributes.
 */
public class TooltipAttributeRegistry {

	private static final Map<Type, List<String>> FILTERS = new ConcurrentHashMap<>();
	private static final Map<String, Predicate<PropertyContainer>> ATTRIBUTE_CONDITIONS = new ConcurrentHashMap<>();

	private TooltipAttributeRegistry() {
	}

	/**
	 * Creates and returns a new filter builder for defining tooltip filters.
	 *
	 * <p>Example:
	 * <pre>
	 * TooltipAttributeRegistry.filterBuilder()
	 *                          .type(SWORD)
	 *                          .attributes(List.of("ATTACK_DAMAGE", "ATTACK_SPEED"))
	 *                          .register();
	 * </pre>
	 *
	 * @return A new FilterBuilder instance.
	 */
	public static FilterBuilder filterBuilder() {
		return new FilterBuilder();
	}

	/**
	 * Creates and returns a new attribute builder for defining writable attributes.
	 * When no condition is set, they are enabled by default
	 *
	 * <p>Example:
	 * <pre>
	 * TooltipAttributeRegistry.attributeBuilder()
	 *                          .attribute("RARITY")
	 *                          .condition(container -> !ForgeroConfigurationLoader.configuration.hideRarity)
	 *                          .register();
	 * </pre>
	 *
	 * @return A new AttributeBuilder instance.
	 */
	public static AttributeBuilder attributeBuilder() {
		return new AttributeBuilder();
	}

	/**
	 * Retrieves a combined list of registered attributes for the provided type.
	 *
	 * @param type The type for which to fetch the attributes.
	 * @return List of attributes combined from all matching registered filters for the type.
	 */
	public static List<String> getFilterForType(Type type) {
		return FILTERS.entrySet().stream()
				.filter(entry -> type.test(entry.getKey()))
				.flatMap(entry -> entry.getValue().stream())
				.distinct()
				.collect(Collectors.toList());
	}

	/**
	 * Retrieves a list of registered attributes for a given property container.
	 *
	 * @param container The PropertyContainer to check.
	 * @return List of attributes that satisfy their conditions for the provided container.
	 */
	public static List<String> getWritableAttributes(PropertyContainer container) {
		return ATTRIBUTE_CONDITIONS.entrySet().stream()
				.filter(entry -> entry.getValue().test(container))
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
	}

	/**
	 * Helper class for defining and registering tooltip filters.
	 */
	public static final class FilterBuilder {
		private Type type;
		private List<String> attributes;

		private FilterBuilder() {
		}

		/**
		 * Sets the type for the filter.
		 *
		 * @param type The type to associate with the filter.
		 * @return The builder instance.
		 */
		public FilterBuilder type(Type type) {
			this.type = type;
			return this;
		}

		/**
		 * Sets the attributes for the filter.
		 *
		 * @param attributes List of attributes to register with the filter.
		 * @return The builder instance.
		 */
		public FilterBuilder attributes(List<String> attributes) {
			this.attributes = attributes;
			return this;
		}

		/**
		 * Registers the filter. If a filter for the type already exists, attributes are combined.
		 */
		public void register() {
			if (type == null || attributes == null) {
				throw new IllegalStateException("Both type and attributes must be set before registering");
			}

			FILTERS.merge(type, attributes, (oldAttributes, newAttributes) -> {
				List<String> combinedAttributes = new ArrayList<>(oldAttributes);
				combinedAttributes.addAll(newAttributes);
				return combinedAttributes.stream().distinct().collect(Collectors.toList());
			});
		}
	}

	/**
	 * Helper class for defining and registering writable attributes.
	 */
	public static final class AttributeBuilder {
		private String attribute;
		private Predicate<PropertyContainer> condition = container -> true;

		private AttributeBuilder() {
		}

		/**
		 * Sets the attribute name.
		 *
		 * @param attribute The name of the attribute.
		 * @return The builder instance.
		 */
		public AttributeBuilder attribute(String attribute) {
			this.attribute = attribute;
			return this;
		}

		/**
		 * Defines a condition for the attribute.
		 *
		 * @param condition The condition to satisfy for the attribute to be considered.
		 * @return The builder instance.
		 */
		public AttributeBuilder condition(Predicate<PropertyContainer> condition) {
			this.condition = condition;
			return this;
		}

		/**
		 * Registers the attribute with the specified condition.
		 */
		public void register() {
			if (attribute == null) {
				throw new IllegalStateException("Attribute must be set before registering");
			}
			ATTRIBUTE_CONDITIONS.put(attribute, condition);
		}
	}
}
