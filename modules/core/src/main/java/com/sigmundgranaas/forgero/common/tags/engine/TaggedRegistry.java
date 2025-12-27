package com.sigmundgranaas.forgero.common.tags.engine;

import com.sigmundgranaas.forgero.common.identifier.api.Identifiable;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.Taggable;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An immutable, generic registry for storing and querying resources that are both Identifiable and Taggable.
 *
 * @param <T> The type of the resource being stored. Must extend Identifiable and Taggable.
 */
public class TaggedRegistry<T extends Identifiable & Taggable> {

	private final Map<OpenIdentifier, T> resources;
	private final Map<OpenIdentifier, Set<OpenIdentifier>> tagIndex;
	private final TagResolver tagResolver;

	private TaggedRegistry(Map<OpenIdentifier, T> resources, Map<OpenIdentifier, Set<OpenIdentifier>> tagIndex, TagResolver tagResolver) {
		this.resources = resources;
		this.tagIndex = tagIndex;
		this.tagResolver = tagResolver;
	}

	/**
	 * Finds a resource by its unique identifier.
	 *
	 * @param id The ID of the resource.
	 * @return An Optional containing the resource, or empty if not found.
	 */
	public Optional<T> find(OpenIdentifier id) {
		return Optional.ofNullable(resources.get(id));
	}

	/**
	 * @return An unmodifiable collection of all resources in the registry.
	 */
	public Collection<T> all() {
		return Collections.unmodifiableCollection(resources.values());
	}

	/**
	 * Retrieves all resources that are DIRECTLY tagged with the given tag.
	 * This method does not consider tag inheritance.
	 *
	 * @param tag The exact tag to look for.
	 * @return A list of resources with the direct tag.
	 */
	public List<T> getDirectlyTagged(OpenIdentifier tag) {
		return tagIndex.getOrDefault(tag, Collections.emptySet())
				.stream()
				.map(resources::get)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	/**
	 * Finds all resources that match a tag, including through inheritance.
	 * For example, querying for "forgero:metal" will also find items tagged with "forgero:iron" if iron is a child of metal.
	 *
	 * @param tag The tag to query by.
	 * @return A list of all matching resources.
	 */
	public List<T> findByTag(OpenIdentifier tag) {
		return tagResolver.getDescendants(tag).stream()
				.flatMap(descendantTag -> getDirectlyTagged(descendantTag).stream())
				.distinct()
				.collect(Collectors.toList());
	}

	public static class Builder<T extends Identifiable & Taggable> {
		private final TagResolver tagResolver;
		private final Map<OpenIdentifier, T> resources = new HashMap<>();
		private final Map<OpenIdentifier, Set<OpenIdentifier>> tagIndex = new HashMap<>();
		private final Set<OpenIdentifier> knownTags;

		public Builder(TagResolver tagResolver) {
			this.tagResolver = tagResolver;
			this.knownTags = tagResolver.getAllTags();
		}

		/**
		 * Adds a resource to the registry.
		 * Validates that all of the resource's direct tags exist in the provided TagResolver.
		 *
		 * @param resource The resource to add.
		 * @return This builder instance for chaining.
		 * @throws IllegalArgumentException if the resource has a tag that does not exist in the TagResolver
		 * or if a resource with the same ID has already been added.
		 */
		public Builder<T> add(T resource) {
			if (resources.containsKey(resource.id())) {
				throw new IllegalArgumentException("Cannot add resource. A resource with ID " + resource.id() + " already exists.");
			}

			for (OpenIdentifier tag : resource.getTags()) {
				if (!knownTags.contains(tag)) {
					throw new IllegalArgumentException("Resource " + resource.id() + " contains tag " + tag + " which does not exist in the TagResolver. \n Available tags: " + knownTags);
				}
				tagIndex.computeIfAbsent(tag, k -> new HashSet<>()).add(resource.id());
			}

			resources.put(resource.id(), resource);
			return this;
		}

		/**
		 * Constructs the final, immutable TaggedRegistry.
		 *
		 * @return An immutable registry instance.
		 */
		public TaggedRegistry<T> build() {
			return new TaggedRegistry<>(
					Map.copyOf(resources),
					Map.copyOf(tagIndex),
					tagResolver
			);
		}
	}
}
