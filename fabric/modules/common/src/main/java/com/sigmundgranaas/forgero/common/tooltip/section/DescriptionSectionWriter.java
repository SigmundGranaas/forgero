package com.sigmundgranaas.forgero.common.tooltip.section;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.property.tooltip.TooltipProperty;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A writer dedicated to rendering custom tooltip sections defined by {@link TooltipProperty}.
 * It groups properties by their key (e.g., "description", "lore") and displays them
 * with a translatable section header and translatable content lines.
 */
public class DescriptionSectionWriter implements TooltipSectionWriter {
	private final List<TooltipProperty> properties;
	private final List<Text> cachedEntries = new ArrayList<>();

	public DescriptionSectionWriter(Component component, Resolver resolver) {
		this.properties = resolver.resolve(component, new TooltipProperty.Engine());
	}

	@Override
	public void append(List<Text> tooltip, TooltipContext context) {
		if (!cachedEntries.isEmpty()) {
			tooltip.addAll(cachedEntries);
			tooltip.add(Text.of("")); // Padding
		}
	}

	@Override
	public boolean shouldShow() {
		if (properties.isEmpty()) {
			return false;
		}
		cachedEntries.clear();

		Map<OpenIdentifier, List<TooltipProperty>> groupedProperties = properties.stream()
				.collect(Collectors.groupingBy(TooltipProperty::key));

		groupedProperties.entrySet().stream()
				.sorted(Map.Entry.comparingByKey(Comparator.comparing(OpenIdentifier::toString)))
				.forEach(entry -> {
					OpenIdentifier sectionKey = entry.getKey();
					List<TooltipProperty> sectionProperties = entry.getValue();

					String headerKey = "tooltip.forgero.section." + sectionKey.path();
					cachedEntries.add(Text.translatable(headerKey).append(":").formatted(Formatting.GRAY));

					for (TooltipProperty prop : sectionProperties) {
						cachedEntries.add(
								Text.literal(" ") // Indentation
										.append(Text.translatable(prop.value()).formatted(Formatting.DARK_GRAY))
						);
					}
				});

		return !cachedEntries.isEmpty();
	}
}
