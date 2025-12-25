package com.sigmundgranaas.forgero.data.loading.api.data;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Configuration for controlling template generation behavior.
 *
 * <p>This configuration is added to part templates to control which specific components
 * are used during the combinatorial generation process. Each slot can have its own
 * filter to subset the eligible components.</p>
 *
 * <p><strong>Important Distinction:</strong></p>
 * <ul>
 *   <li><strong>Runtime Slots:</strong> Accept any component matching the slot's type tag</li>
 *   <li><strong>Generation Filters:</strong> Narrow down which components trigger item generation</li>
 * </ul>
 *
 * <p><strong>Example:</strong></p>
 * <pre>
 * {
 *   "generation": {
 *     "slots": {
 *       "shape": {
 *         "require_all_tags": ["forgero:pickaxe_head_shape", "forgero:base_shape"]
 *       },
 *       "material": {
 *         "require_all_tags": ["forgero:tool_material"]
 *       }
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>This configuration ensures that only base shapes (not schematics or casts) are used
 * during generation, even though schematics and casts can fill the shape slot at runtime.</p>
 *
 * @param slots Map of slot names to their generation filters
 */
public record GenerationConfigData(
		@Nullable
		Map<String, SlotGenerationFilter> slots
) {
	/**
	 * Returns true if this configuration has no constraints.
	 */
	public boolean isEmpty() {
		return slots == null || slots.isEmpty();
	}

	/**
	 * Gets the filter for a specific slot name, or null if no filter is defined.
	 */
	@Nullable
	public SlotGenerationFilter getFilterForSlot(String slotName) {
		if (slots == null) {
			return null;
		}
		return slots.get(slotName);
	}

	/**
	 * Creates a new builder for constructing a GenerationConfigData.
	 *
	 * @return A new Builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder for constructing GenerationConfigData instances.
	 *
	 * <p>Example usage:</p>
	 * <pre>
	 * GenerationConfigData config = GenerationConfigData.builder()
	 *     .addSlotFilter("shape", SlotGenerationFilter.builder()
	 *         .requireAllTags(List.of(id("forgero:pickaxe_head_shape"), id("forgero:base_shape")))
	 *         .build())
	 *     .addSlotFilter("material", SlotGenerationFilter.builder()
	 *         .requireAnyTags(List.of(id("forgero:metal")))
	 *         .build())
	 *     .build();
	 * </pre>
	 */
	public static class Builder {
		private Map<String, SlotGenerationFilter> slots;

		private Builder() {
		}

		/**
		 * Sets the complete map of slot filters.
		 *
		 * @param slots Map of slot names to their filters
		 * @return This builder
		 */
		public Builder slots(Map<String, SlotGenerationFilter> slots) {
			this.slots = slots;
			return this;
		}

		/**
		 * Adds a filter for a specific slot.
		 *
		 * @param slotName The name of the slot
		 * @param filter   The filter to apply to this slot
		 * @return This builder
		 */
		public Builder addSlotFilter(String slotName, SlotGenerationFilter filter) {
			if (this.slots == null) {
				this.slots = new java.util.HashMap<>();
			}
			this.slots.put(slotName, filter);
			return this;
		}

		/**
		 * Removes a filter for a specific slot.
		 *
		 * @param slotName The name of the slot to remove
		 * @return This builder
		 */
		public Builder removeSlotFilter(String slotName) {
			if (this.slots != null) {
				this.slots.remove(slotName);
			}
			return this;
		}

		/**
		 * Builds the GenerationConfigData.
		 *
		 * @return A new GenerationConfigData instance
		 */
		public GenerationConfigData build() {
			return new GenerationConfigData(slots);
		}
	}
}
