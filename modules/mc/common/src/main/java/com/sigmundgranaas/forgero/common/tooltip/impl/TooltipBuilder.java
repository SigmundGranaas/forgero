package com.sigmundgranaas.forgero.common.tooltip.impl;

import com.sigmundgranaas.forgero.common.tooltip.TooltipDescriptor;
import com.sigmundgranaas.forgero.common.tooltip.api.writer.SectionWriter;
import com.sigmundgranaas.forgero.common.tooltip.comparison.ComparisonContext;
import com.sigmundgranaas.forgero.common.tooltip.comparison.DifferenceCalculator;
import com.sigmundgranaas.forgero.common.tooltip.comparison.DifferenceFormatter;
import com.sigmundgranaas.forgero.common.tooltip.section.SectionRegistry;
import com.sigmundgranaas.forgero.common.tooltip.section.SectionWriterContext;
import com.sigmundgranaas.forgero.common.tooltip.value.TooltipValueResolver;
import com.sigmundgranaas.forgero.common.tooltip.api.TooltipRenderConfig;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.core.component.api.Component;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Internal implementation for building tooltips.
 * <p>
 * This class is not part of the public API. Use {@link com.sigmundgranaas.forgero.common.tooltip.api.TooltipApi}
 * to build tooltips.
 */
public final class TooltipBuilder {

	private TooltipBuilder() {
	}

	/**
	 * Builds a complete tooltip for the given component.
	 *
	 * @param component           The component to build tooltip for
	 * @param comparisonContext   The comparison context
	 * @param renderConfig        The render configuration
	 * @param differenceFormatter The difference formatter
	 * @param tooltipContext      The Minecraft tooltip context
	 * @return List of text lines for the tooltip
	 */
	public static List<Text> build(
			Component component,
			ComparisonContext comparisonContext,
			TooltipRenderConfig renderConfig,
			DifferenceFormatter differenceFormatter,
			TooltipContext tooltipContext
	) {
		List<Text> tooltip = new ArrayList<>();

		// Resolve all needed data - uses O(1) lookup for EquipmentComponent
		AttributeQueryResult attributes = AttributeEngine.resolveAttributes(component);

		TooltipDescriptor.Engine descriptorEngine = new TooltipDescriptor.Engine();
		List<TooltipDescriptor> descriptors = descriptorEngine.resolve(component);

		// Create shared utilities
		TooltipValueResolver valueResolver = new TooltipValueResolver(component, attributes);
		DifferenceCalculator diffCalc = new DifferenceCalculator();

		// Create and run section writers
		for (var registered : SectionRegistry.getAllSorted()) {
			SectionWriterContext writerContext = new SectionWriterContext(
					registered.section(),
					component,
					attributes,
					descriptors,
					comparisonContext,
					valueResolver,
					diffCalc,
					differenceFormatter,
					renderConfig
			);

			SectionWriter writer = registered.factory().create(writerContext);
			if (writer.shouldShow()) {
				writer.append(tooltip, tooltipContext);
			}
		}

		return tooltip;
	}
}
