package com.sigmundgranaas.forgero.common.tooltip.section;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tooltip.TooltipDescriptor;
import com.sigmundgranaas.forgero.common.tooltip.api.TooltipSection;
import com.sigmundgranaas.forgero.common.tooltip.comparison.ComparisonContext;
import com.sigmundgranaas.forgero.common.tooltip.comparison.DifferenceCalculator;
import com.sigmundgranaas.forgero.common.tooltip.comparison.DifferenceFormatter;
import com.sigmundgranaas.forgero.common.tooltip.value.TooltipValueResolver;
import com.sigmundgranaas.forgero.common.tooltip.api.TooltipRenderConfig;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.component.api.Component;

import java.util.List;

/**
 * Provides all context needed by a section writer.
 * <p>
 * This record bundles together all dependencies and data needed to render
 * a tooltip section, keeping section writer constructors simple.
 *
 * <h2>Accessing Data</h2>
 * <pre>{@code
 * public class MySectionWriter implements SectionWriter {
 *     private final SectionWriterContext ctx;
 *
 *     public MySectionWriter(SectionWriterContext ctx) {
 *         this.ctx = ctx;
 *     }
 *
 *     @Override
 *     public void append(List<Text> tooltip, TooltipContext context) {
 *         // Get descriptors for this section
 *         for (TooltipDescriptor desc : ctx.sectionDescriptors()) {
 *             String resolved = ctx.resolveTemplate(desc.template());
 *             tooltip.add(Text.translatable(resolved));
 *         }
 *
 *         // Access attributes
 *         float damage = ctx.attributes().getValue(OpenIdentifier.parse("forgero:attack_damage"));
 *     }
 * }
 * }</pre>
 *
 * @param section              The section this writer is creating content for
 * @param component            The component being rendered
 * @param attributes           The resolved attributes for the component
 * @param descriptors          All tooltip descriptors for the component
 * @param comparisonContext    The comparison mode for showing differences
 * @param valueResolver        Resolver for placeholder values in templates
 * @param differenceCalculator Calculator for attribute differences
 * @param differenceFormatter  Formatter for difference display
 * @param renderConfig         Configuration for rendering behavior
 */
public record SectionWriterContext(
		TooltipSection section,
		Component component,
		AttributeQueryResult attributes,
		List<TooltipDescriptor> descriptors,
		ComparisonContext comparisonContext,
		TooltipValueResolver valueResolver,
		DifferenceCalculator differenceCalculator,
		DifferenceFormatter differenceFormatter,
		TooltipRenderConfig renderConfig
) {

	/**
	 * Gets descriptors filtered to this section, sorted by priority.
	 *
	 * @return List of descriptors for this section in priority order
	 */
	public List<TooltipDescriptor> sectionDescriptors() {
		return descriptors.stream()
				.filter(d -> d.section().equals(section.id()))
				.sorted((a, b) -> Integer.compare(a.priority(), b.priority()))
				.toList();
	}

	/**
	 * Gets descriptors for this section that target a specific property.
	 *
	 * @param targetId The property ID to filter by
	 * @return List of descriptors targeting that property
	 */
	public List<TooltipDescriptor> descriptorsForTarget(OpenIdentifier targetId) {
		return sectionDescriptors().stream()
				.filter(d -> targetId.equals(d.target()))
				.toList();
	}

	/**
	 * Gets descriptors for this section that have no specific target.
	 *
	 * @return List of standalone descriptors
	 */
	public List<TooltipDescriptor> standaloneDescriptors() {
		return sectionDescriptors().stream()
				.filter(d -> !d.hasTarget())
				.toList();
	}

	/**
	 * Resolves a template string with placeholders.
	 *
	 * @param template The template to resolve
	 * @return The resolved string
	 */
	public String resolveTemplate(String template) {
		return valueResolver.resolve(template);
	}

	/**
	 * Gets the base indentation level from config.
	 */
	public int baseIndent() {
		return renderConfig.baseIndent();
	}

	/**
	 * Checks if zero/default values should be hidden.
	 */
	public boolean hideZeroValues() {
		return renderConfig.hideZeroValues();
	}

	/**
	 * Checks if difference arrows should be shown.
	 */
	public boolean showDifferenceArrows() {
		return renderConfig.showDifferenceArrows();
	}

	/**
	 * Checks if difference values should be shown.
	 */
	public boolean showDifferenceValues() {
		return renderConfig.showDifferenceValues();
	}

	/**
	 * Checks if advanced info should be shown (e.g., when Shift is held).
	 */
	public boolean showAdvancedInfo() {
		return renderConfig.showAdvancedInfo();
	}
}
