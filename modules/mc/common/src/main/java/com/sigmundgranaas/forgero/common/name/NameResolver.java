package com.sigmundgranaas.forgero.common.name;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.property.namereplacement.NameReplacementProperty;
import com.sigmundgranaas.forgero.core.component.api.Component;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Resolves display names from Forgero component IDs.
 * <p>
 * Splits component ID paths by '-' and translates each element using
 * Minecraft's translation system with "item.forgero.{element}" keys.
 * <p>
 * Example: Component ID "iron-katana" produces a Text combining:
 * <ul>
 *   <li>Text.translatable("item.forgero.iron") → "Iron"</li>
 *   <li>Text.translatable("util.forgero.name_separator") → " "</li>
 *   <li>Text.translatable("item.forgero.katana") → "Katana"</li>
 * </ul>
 * Result: "Iron Katana"
 * <p>
 * If a component has {@link NameReplacementProperty} properties, the replacements
 * are applied before translation. For example, with a property that replaces
 * "sword" with "katana", a component ID of "iron-sword" would display as "Iron Katana".
 */
public final class NameResolver {
	private static final String ELEMENT_SEPARATOR = "-";
	private static final String TRANSLATION_KEY_PREFIX = "item.forgero.";
	private static final String SEPARATOR_KEY = "util.forgero.name_separator";

	private NameResolver() {
		// Private constructor to prevent instantiation
	}

	/**
	 * Resolves a display name from a name path string.
	 * <p>
	 * Splits by '-' and translates each element.
	 *
	 * @param namePath The name path (e.g., "iron-katana")
	 * @return A Text combining translated elements
	 */
	public static Text resolve(String namePath) {
		MutableText text = Text.literal("");
		String[] elements = namePath.split(ELEMENT_SEPARATOR);
		for (int i = 0; i < elements.length; i++) {
			if (i > 0) {
				text.append(Text.translatable(SEPARATOR_KEY));
			}
			text.append(Text.translatable(TRANSLATION_KEY_PREFIX + elements[i]));
		}
		return text;
	}

	/**
	 * Resolves a display name from a component's ID.
	 *
	 * @param componentId The component identifier
	 * @return A Text combining translated elements
	 */
	public static Text resolve(OpenIdentifier componentId) {
		return resolve(componentId.path());
	}

	/**
	 * Resolves a display name from a component.
	 * <p>
	 * If the component has {@link NameReplacementProperty} properties,
	 * the replacements are applied to the name path before translation.
	 *
	 * @param component The component to resolve the name for
	 * @return A Text combining translated elements with any replacements applied
	 */
	public static Text resolve(Component component) {
		String namePath = component.id().path();

		// Apply all name replacement properties in order
		List<NameReplacementProperty> replacements = component.properties(NameReplacementProperty.PROPERTY_KEY);
		for (NameReplacementProperty replacement : replacements) {
			namePath = namePath.replace(replacement.from(), replacement.to());
		}

		return resolve(namePath);
	}
}
