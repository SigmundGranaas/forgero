package com.sigmundgranaas.forgero.common.tooltip.api;

/**
 * Configuration for tooltip rendering behavior.
 * <p>
 * Use the builder to create custom configurations:
 * <pre>{@code
 * TooltipRenderConfig config = TooltipRenderConfig.builder()
 *     .showAdvancedInfo(Screen.hasShiftDown())
 *     .hideZeroValues(true)
 *     .build();
 * }</pre>
 *
 * @param baseIndent            Base indentation level for all content
 * @param hideZeroValues        Whether to hide attributes with zero/default values
 * @param showDifferenceArrows  Whether to show ↑/↓ arrows for comparisons
 * @param showDifferenceValues  Whether to show numeric difference values
 * @param showAdvancedInfo      Whether to show advanced info (e.g., when Shift held)
 * @param addPaddingAfterSections Whether to add empty line after each section
 */
public record TooltipRenderConfig(
		int baseIndent,
		boolean hideZeroValues,
		boolean showDifferenceArrows,
		boolean showDifferenceValues,
		boolean showAdvancedInfo,
		boolean addPaddingAfterSections
) {

	/**
	 * Default configuration.
	 */
	private static final TooltipRenderConfig DEFAULTS = new TooltipRenderConfig(
			0,      // baseIndent
			true,   // hideZeroValues
			true,   // showDifferenceArrows
			true,   // showDifferenceValues
			false,  // showAdvancedInfo
			true    // addPaddingAfterSections
	);

	/**
	 * Returns the default configuration.
	 */
	public static TooltipRenderConfig defaults() {
		return DEFAULTS;
	}

	/**
	 * Creates a new builder.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder for TooltipRenderConfig.
	 */
	public static final class Builder {
		private int baseIndent = 0;
		private boolean hideZeroValues = true;
		private boolean showDifferenceArrows = true;
		private boolean showDifferenceValues = true;
		private boolean showAdvancedInfo = false;
		private boolean addPaddingAfterSections = true;

		private Builder() {
		}

		public Builder baseIndent(int indent) {
			this.baseIndent = indent;
			return this;
		}

		public Builder hideZeroValues(boolean hide) {
			this.hideZeroValues = hide;
			return this;
		}

		public Builder showDifferenceArrows(boolean show) {
			this.showDifferenceArrows = show;
			return this;
		}

		public Builder showDifferenceValues(boolean show) {
			this.showDifferenceValues = show;
			return this;
		}

		public Builder showAdvancedInfo(boolean show) {
			this.showAdvancedInfo = show;
			return this;
		}

		public Builder addPaddingAfterSections(boolean add) {
			this.addPaddingAfterSections = add;
			return this;
		}

		public TooltipRenderConfig build() {
			return new TooltipRenderConfig(
					baseIndent,
					hideZeroValues,
					showDifferenceArrows,
					showDifferenceValues,
					showAdvancedInfo,
					addPaddingAfterSections
			);
		}
	}
}
