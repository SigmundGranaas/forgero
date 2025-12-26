package com.sigmundgranaas.forgero.common.tags.engine;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.Taggable;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TagGraph implements TagResolver {
	private static final TagGraph EMPTY = new TagGraph(Collections.emptyMap());
	private final Map<OpenIdentifier, Set<OpenIdentifier>> parentRelationships;
	private final Map<OpenIdentifier, Set<OpenIdentifier>> childRelationships;

	public TagGraph(Map<OpenIdentifier, Set<OpenIdentifier>> parentRelationships) {
		var immutableRelationShips = parentRelationships.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> Set.copyOf(e.getValue())));
		this.parentRelationships = Map.copyOf(immutableRelationShips);

		// Pre-calculate the reverse mapping for fast descendant lookups
		this.childRelationships = buildChildRelationships(this.parentRelationships);
	}

	/**
	 * @return A shared, immutable instance of an empty TagGraph.
	 */
	public static TagGraph empty() {
		return EMPTY;
	}

	/**
	 * Merges this TagGraph with another TagResolver, returning a new TagGraph containing the combined relationships.
	 * Supports universal merge - any TagResolver implementation can be merged.
	 *
	 * @param other The other TagResolver to merge with.
	 * @return A new, combined TagResolver.
	 */
	@Override
	public TagResolver merge(TagResolver other) {
		if (this == EMPTY) {
			return other;
		}
		if (other == TagResolver.empty()) {
			return this;
		}

		Map<OpenIdentifier, Set<OpenIdentifier>> mergedRelationships = new HashMap<>();

		// Deep copy current relationships to the new map
		this.parentRelationships.forEach((key, value) -> mergedRelationships.put(key, new HashSet<>(value)));

		// Merge relationships from the other resolver (universal merge)
		other.getRelationships().forEach((childId, parentsToAdd) -> {
			mergedRelationships.computeIfAbsent(childId, k -> new HashSet<>()).addAll(parentsToAdd);
		});

		return new TagGraph(mergedRelationships);
	}

	private Map<OpenIdentifier, Set<OpenIdentifier>> buildChildRelationships(Map<OpenIdentifier, Set<OpenIdentifier>> parents) {
		Map<OpenIdentifier, Set<OpenIdentifier>> children = new HashMap<>();
		parents.forEach((child, parentSet) -> {
			for (OpenIdentifier parent : parentSet) {
				children.computeIfAbsent(parent, k -> new HashSet<>()).add(child);
			}
		});
		return children;
	}

	@Override
	public boolean hasTag(Taggable item, OpenIdentifier tag) {
		return isTagged(item, tag);
	}

	/**
	 * Checks if an item has a specific tag, either directly or through inheritance.
	 * This is done using a Breadth-First Search (BFS) up the parent hierarchy.
	 *
	 * @param item     The item to check.
	 * @param targetId The identifier of the tag to look for.
	 * @return true if the tag is present directly or in the item's tag hierarchy.
	 */
	public boolean isTagged(Taggable item, OpenIdentifier targetId) {
		Queue<OpenIdentifier> toVisit = new LinkedList<>(item.getTags());
		Set<OpenIdentifier> visited = new HashSet<>(item.getTags());

		while (!toVisit.isEmpty()) {
			OpenIdentifier current = toVisit.poll();
			if (current.equals(targetId)) {
				return true;
			}

			Set<OpenIdentifier> parents = parentRelationships.getOrDefault(current, Collections.emptySet());
			for (OpenIdentifier parent : parents) {
				if (visited.add(parent)) {
					toVisit.add(parent);
				}
			}
		}
		return false;
	}

	/**
	 * Filters a collection of items, returning a list of those that have the specified tag,
	 * either directly or through inheritance.
	 *
	 * @param targetId The identifier of the tag to search for.
	 * @param items    The collection of items to filter.
	 * @param <T>      The type of the Taggable item.
	 * @return A new list containing only the matching items.
	 */
	@Override
	public <T extends Taggable> List<T> findTagged(OpenIdentifier targetId, Collection<T> items) {
		return items.stream()
				.filter(item -> this.isTagged(item, targetId))
				.collect(Collectors.toList());
	}


	/**
	 * Filters a collection of items, returning a list of those that have the specified tag
	 * assigned *directly* to them, ignoring inheritance.
	 *
	 * @param targetId The identifier of the tag to search for.
	 * @param items      The collection of items to filter.
	 * @param <T>        The type of the Taggable item.
	 * @return A new list containing only the matching items.
	 */
	@Override
	public <T extends Taggable> List<T> findDirectlyTagged(OpenIdentifier targetId, Collection<T> items) {
		return items.stream()
				.filter(item -> item.getTags().contains(targetId))
				.collect(Collectors.toList());
	}

	@Override
	public Set<OpenIdentifier> getAllTags() {
		return getAllIdentifiers();
	}

	/**
	 * @return An unmodifiable set containing every tag identifier present in the graph.
	 */
	public Set<OpenIdentifier> getAllIdentifiers() {
		return Stream.concat(
						parentRelationships.keySet().stream(),
						parentRelationships.values().stream().flatMap(Set::stream))
				.collect(Collectors.toUnmodifiableSet());
	}


	/**
	 * Finds all tags that inherit from the given tag, including the tag itself.
	 *
	 * @param id The identifier of the root tag to start the search from.
	 * @return A set of all descendant tags plus the starting tag.
	 */
	@Override
	public Set<OpenIdentifier> getDescendants(OpenIdentifier id) {
		Set<OpenIdentifier> descendants = new HashSet<>();
		Queue<OpenIdentifier> toVisit = new LinkedList<>();

		toVisit.add(id);
		descendants.add(id);

		while (!toVisit.isEmpty()) {
			OpenIdentifier current = toVisit.poll();
			for (OpenIdentifier child : childRelationships.getOrDefault(current, Collections.emptySet())) {
				if (descendants.add(child)) {
					toVisit.add(child);
				}
			}
		}
		return descendants;
	}

	/**
	 * Retrieves the direct parents of a given tag.
	 *
	 * @param id The identifier of the child tag.
	 * @return An unmodifiable set of the parent tag identifiers. Returns an empty set if the tag has no parents or is not in the graph.
	 */
	@Override
	public Set<OpenIdentifier> getParents(OpenIdentifier id) {
		return Set.copyOf(parentRelationships.getOrDefault(id, Collections.emptySet()));
	}

	@Override
	public Map<OpenIdentifier, Set<OpenIdentifier>> getRelationships() {
		return Map.copyOf(parentRelationships);
	}
}
