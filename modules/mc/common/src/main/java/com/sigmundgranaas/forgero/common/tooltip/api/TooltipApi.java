package com.sigmundgranaas.forgero.common.tooltip.api;

import com.sigmundgranaas.forgero.common.tooltip.api.writer.SectionWriter;
import com.sigmundgranaas.forgero.common.tooltip.api.writer.SectionWriterFactory;
import com.sigmundgranaas.forgero.common.tooltip.comparison.ComparisonContext;
import com.sigmundgranaas.forgero.common.tooltip.comparison.DifferenceFormatter;
import com.sigmundgranaas.forgero.common.tooltip.section.SectionRegistry;
import com.sigmundgranaas.forgero.common.tooltip.value.PlaceholderRegistry;
import com.sigmundgranaas.forgero.common.tooltip.api.TooltipRenderConfig;
import com.sigmundgranaas.forgero.core.component.api.Component;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Main entry point for the Forgero tooltip system.
 * <p>
 * This API provides:
 * <ul>
 *   <li>Tooltip building via {@link #builder(Component)}</li>
 *   <li>Section registration via {@link #registerSection(TooltipSection, SectionWriterFactory)}</li>
 *   <li>Placeholder registration via {@link #registerPlaceholder(String, PlaceholderResolver)}</li>
 *   <li>Inverse attribute registration via {@link #registerInverseAttribute(com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier)}</li>
 * </ul>
 *
 * <h2>Building Tooltips</h2>
 * <pre>{@code
 * List<Text> tooltip = TooltipApi.builder(component)
 *     .withComparison(ComparisonContext.strippedItem(stripped))
 *     .build(tooltipContext);
 * }</pre>
 *
 * <h2>Registering Custom Sections</h2>
 * <pre>{@code
 * TooltipSection mySection = TooltipSection.of("mymod:custom", 450, true);
 * TooltipApi.registerSection(mySection, ctx -> new MySectionWriter(ctx));
 * }</pre>
 *
 * <h2>Registering Custom Placeholders</h2>
 * <pre>{@code
 * TooltipApi.registerPlaceholder("rarity", comp ->
 *     Optional.of(RarityHelper.getRarity(comp).name()));
 * }</pre>
 *
 * @see TooltipSection
 * @see SectionWriter
 * @see ComparisonContext
 */
public final class TooltipApi {

	private TooltipApi() {
	}

	// ==================== TOOLTIP BUILDING ====================

	/**
	 * Creates a new tooltip builder for the given component.
	 *
	 * @param component The component to build tooltips for
	 * @return A new builder instance
	 */
	public static Builder builder(Component component) {
		return new Builder(component);
	}

	// ==================== SECTION REGISTRATION ====================

	/**
	 * Registers a tooltip section with its writer factory.
	 * <p>
	 * Sections are rendered in priority order (lower priority = earlier).
	 *
	 * @param section The section definition
	 * @param factory Factory for creating section writers
	 */
	public static void registerSection(TooltipSection section, SectionWriterFactory factory) {
		SectionRegistry.register(section, factory);
	}

	/**
	 * Unregisters a tooltip section.
	 *
	 * @param sectionId The section identifier
	 * @return true if the section was registered
	 */
	public static boolean unregisterSection(com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier sectionId) {
		return SectionRegistry.unregister(sectionId);
	}

	/**
	 * Overrides the priority of a registered section.
	 * <p>
	 * This allows configuration-driven reordering of sections.
	 *
	 * @param sectionId The section identifier
	 * @param priority  The new priority (lower = earlier)
	 */
	public static void setSectionPriority(com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier sectionId, int priority) {
		SectionRegistry.setPriorityOverride(sectionId, priority);
	}

	// ==================== PLACEHOLDER REGISTRATION ====================

	/**
	 * Functional interface for resolving custom placeholders.
	 */
	@FunctionalInterface
	public interface PlaceholderResolver extends Function<Component, Optional<Object>> {
	}

	/**
	 * Registers a custom placeholder resolver.
	 * <p>
	 * Placeholders are used in tooltip templates with the syntax {@code {name}}.
	 *
	 * @param name     The placeholder name (e.g., "rarity")
	 * @param resolver Function to resolve the value
	 */
	public static void registerPlaceholder(String name, PlaceholderResolver resolver) {
		PlaceholderRegistry.register(name, resolver::apply);
	}

	/**
	 * Unregisters a custom placeholder.
	 *
	 * @param name The placeholder name
	 * @return true if the placeholder was registered
	 */
	public static boolean unregisterPlaceholder(String name) {
		return PlaceholderRegistry.unregister(name);
	}

	// ==================== COMPARISON CONFIGURATION ====================

	/**
	 * Registers an attribute as "inverse" for comparison coloring.
	 * <p>
	 * Inverse attributes show red when increasing (e.g., weight, cooldown).
	 *
	 * @param attributeId The attribute identifier
	 */
	public static void registerInverseAttribute(com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier attributeId) {
		DifferenceFormatter.registerInverseAttribute(attributeId);
	}

	// ==================== BUILDER ====================

	/**
	 * Fluent builder for constructing tooltips.
	 */
	public static final class Builder {
		private final Component component;
		private ComparisonContext comparisonContext = ComparisonContext.none();
		private TooltipRenderConfig renderConfig = TooltipRenderConfig.defaults();
		private DifferenceFormatter differenceFormatter = new DifferenceFormatter();

		private Builder(Component component) {
			this.component = component;
		}

		/**
		 * Sets the comparison context for showing attribute differences.
		 *
		 * @param context The comparison context
		 * @return This builder
		 */
		public Builder withComparison(ComparisonContext context) {
			this.comparisonContext = context;
			return this;
		}

		/**
		 * Sets the render configuration.
		 *
		 * @param config The render configuration
		 * @return This builder
		 */
		public Builder withRenderConfig(TooltipRenderConfig config) {
			this.renderConfig = config;
			return this;
		}

		/**
		 * Sets a custom difference formatter.
		 *
		 * @param formatter The formatter to use
		 * @return This builder
		 */
		public Builder withDifferenceFormatter(DifferenceFormatter formatter) {
			this.differenceFormatter = formatter;
			return this;
		}

		/**
		 * Builds the complete tooltip.
		 *
		 * @param tooltipContext The Minecraft tooltip context
		 * @return List of text lines for the tooltip
		 */
		public List<Text> build(TooltipContext tooltipContext) {
			return com.sigmundgranaas.forgero.common.tooltip.impl.TooltipBuilder.build(
					component,
					comparisonContext,
					renderConfig,
					differenceFormatter,
					tooltipContext
			);
		}

		/**
		 * Builds the tooltip and appends it to an existing list.
		 *
		 * @param tooltip        The list to append to
		 * @param tooltipContext The Minecraft tooltip context
		 */
		public void appendTo(List<Text> tooltip, TooltipContext tooltipContext) {
			tooltip.addAll(build(tooltipContext));
		}
	}
}
