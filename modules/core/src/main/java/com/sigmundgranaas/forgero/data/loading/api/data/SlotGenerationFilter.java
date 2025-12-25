package com.sigmundgranaas.forgero.data.loading.api.data;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Filter configuration for controlling which components are used during template generation.
 *
 * <p>This filter is applied to slots in a template to subset the components that will be
 * used for combinatorial generation. Runtime slots accept any component matching their
 * type tag, but generation filters narrow down which specific components trigger item
 * generation.</p>
 *
 * <p><strong>Filter Logic:</strong></p>
 * <ul>
 *   <li><strong>requireAllTags:</strong> Component must have ALL of these tags</li>
 *   <li><strong>requireAnyTags:</strong> Component must have AT LEAST ONE of these tags</li>
 *   <li><strong>excludeAnyTags:</strong> Component must NOT have ANY of these tags</li>
 *   <li><strong>excludeAllTags:</strong> Component must NOT have ALL of these tags (can have some)</li>
 *   <li><strong>explicitList:</strong> Only these specific component IDs are allowed (overrides tag filters)</li>
 * </ul>
 *
 * <p><strong>Example:</strong></p>
 * <pre>
 * {
 *   "require_all_tags": ["forgero:pickaxe_head_shape", "forgero:base_shape"],
 *   "exclude_any_tags": ["forgero:schematic", "forgero:cast"]
 * }
 * </pre>
 * <p>This filter would match: pickaxe_head (has both required tags, has neither excluded tag)</p>
 * <p>This filter would reject: pickaxe_head_schematic (has excluded tag "schematic")</p>
 *
 * @param requireAllTags Component must have ALL of these tags to pass filter
 * @param requireAnyTags Component must have AT LEAST ONE of these tags to pass filter
 * @param excludeAnyTags Component with ANY of these tags will be rejected
 * @param excludeAllTags Component with ALL of these tags will be rejected
 * @param explicitList   Only these specific component IDs pass (overrides all tag filters)
 */
public record SlotGenerationFilter(
		@Nullable
		List<OpenIdentifier> requireAllTags,
		@Nullable
		List<OpenIdentifier> requireAnyTags,
		@Nullable
		List<OpenIdentifier> excludeAnyTags,
		@Nullable
		List<OpenIdentifier> excludeAllTags,
		@Nullable
		List<OpenIdentifier> explicitList
) {
	/**
	 * Returns true if this filter has no constraints (all fields are null or empty).
	 */
	public boolean isEmpty() {
		return (requireAllTags == null || requireAllTags.isEmpty()) &&
				(requireAnyTags == null || requireAnyTags.isEmpty()) &&
				(excludeAnyTags == null || excludeAnyTags.isEmpty()) &&
				(excludeAllTags == null || excludeAllTags.isEmpty()) &&
				(explicitList == null || explicitList.isEmpty());
	}

	/**
	 * Creates a new builder for constructing a SlotGenerationFilter.
	 *
	 * @return A new Builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder for constructing SlotGenerationFilter instances.
	 *
	 * <p>Example usage:</p>
	 * <pre>
	 * SlotGenerationFilter filter = SlotGenerationFilter.builder()
	 *     .requireAllTags(List.of(id("forgero:pickaxe_head_shape"), id("forgero:base_shape")))
	 *     .excludeAnyTags(List.of(id("forgero:schematic"), id("forgero:cast")))
	 *     .build();
	 * </pre>
	 */
	public static class Builder {
		private List<OpenIdentifier> requireAllTags;
		private List<OpenIdentifier> requireAnyTags;
		private List<OpenIdentifier> excludeAnyTags;
		private List<OpenIdentifier> excludeAllTags;
		private List<OpenIdentifier> explicitList;

		private Builder() {
		}

		/**
		 * Sets the tags that components must have ALL of.
		 *
		 * @param tags List of tags that must all be present
		 * @return This builder
		 */
		public Builder requireAllTags(List<OpenIdentifier> tags) {
			this.requireAllTags = tags;
			return this;
		}

		/**
		 * Sets the tags that components must have AT LEAST ONE of.
		 *
		 * @param tags List of tags where at least one must be present
		 * @return This builder
		 */
		public Builder requireAnyTags(List<OpenIdentifier> tags) {
			this.requireAnyTags = tags;
			return this;
		}

		/**
		 * Sets the tags that components must NOT have ANY of.
		 *
		 * @param tags List of tags that must not be present
		 * @return This builder
		 */
		public Builder excludeAnyTags(List<OpenIdentifier> tags) {
			this.excludeAnyTags = tags;
			return this;
		}

		/**
		 * Sets the tags that, if a component has ALL of them, the component is excluded.
		 *
		 * @param tags List of tags that together result in exclusion
		 * @return This builder
		 */
		public Builder excludeAllTags(List<OpenIdentifier> tags) {
			this.excludeAllTags = tags;
			return this;
		}

		/**
		 * Sets an explicit list of component IDs that are allowed.
		 * This overrides all tag-based filters.
		 *
		 * @param ids List of specific component IDs to allow
		 * @return This builder
		 */
		public Builder explicitList(List<OpenIdentifier> ids) {
			this.explicitList = ids;
			return this;
		}

		/**
		 * Adds a single tag to the requireAllTags list.
		 *
		 * @param tag Tag to require
		 * @return This builder
		 */
		public Builder addRequireAllTag(OpenIdentifier tag) {
			if (this.requireAllTags == null) {
				this.requireAllTags = new java.util.ArrayList<>();
			}
			this.requireAllTags.add(tag);
			return this;
		}

		/**
		 * Adds a single tag to the requireAnyTags list.
		 *
		 * @param tag Tag to require (at least one)
		 * @return This builder
		 */
		public Builder addRequireAnyTag(OpenIdentifier tag) {
			if (this.requireAnyTags == null) {
				this.requireAnyTags = new java.util.ArrayList<>();
			}
			this.requireAnyTags.add(tag);
			return this;
		}

		/**
		 * Adds a single tag to the excludeAnyTags list.
		 *
		 * @param tag Tag to exclude
		 * @return This builder
		 */
		public Builder addExcludeAnyTag(OpenIdentifier tag) {
			if (this.excludeAnyTags == null) {
				this.excludeAnyTags = new java.util.ArrayList<>();
			}
			this.excludeAnyTags.add(tag);
			return this;
		}

		/**
		 * Adds a single tag to the excludeAllTags list.
		 *
		 * @param tag Tag to exclude (if all present)
		 * @return This builder
		 */
		public Builder addExcludeAllTag(OpenIdentifier tag) {
			if (this.excludeAllTags == null) {
				this.excludeAllTags = new java.util.ArrayList<>();
			}
			this.excludeAllTags.add(tag);
			return this;
		}

		/**
		 * Adds a single ID to the explicit list.
		 *
		 * @param id Component ID to allow
		 * @return This builder
		 */
		public Builder addExplicitId(OpenIdentifier id) {
			if (this.explicitList == null) {
				this.explicitList = new java.util.ArrayList<>();
			}
			this.explicitList.add(id);
			return this;
		}

		/**
		 * Builds the SlotGenerationFilter.
		 *
		 * @return A new SlotGenerationFilter instance
		 */
		public SlotGenerationFilter build() {
			return new SlotGenerationFilter(
					requireAllTags,
					requireAnyTags,
					excludeAnyTags,
					excludeAllTags,
					explicitList
			);
		}
	}
}
