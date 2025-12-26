package com.sigmundgranaas.forgero.common.tags.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.engine.EmptyTagResolver;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraph;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A resolver for querying tag hierarchies and determining tag relationships.
 * This interface provides a clean abstraction over the underlying tag graph structure,
 * hiding implementation details while exposing the key operations needed for tag-based queries.
 *
 * <p>All implementations must be:
 * <ul>
 *   <li>Immutable after construction</li>
 *   <li>Thread-safe for concurrent access</li>
 *   <li>Null-safe (use {@link #empty()} instead of null)</li>
 * </ul>
 */
public interface TagResolver {

	/**
	 * Checks if an item has a specific tag, either directly or through inheritance.
	 *
	 * @param item The item to check.
	 * @param tag  The tag identifier to look for.
	 * @return true if the item has the tag directly or through inheritance.
	 */
	boolean hasTag(Taggable item, OpenIdentifier tag);

	/**
	 * Gets all tags that inherit from the given tag (descendants in the hierarchy).
	 * The result includes the tag itself.
	 *
	 * @param tag The root tag to find descendants for.
	 * @return A set of all descendant tags, including the tag itself.
	 */
	Set<OpenIdentifier> getDescendants(OpenIdentifier tag);

	/**
	 * Gets the direct parents of a tag.
	 *
	 * @param tag The tag to find parents for.
	 * @return A set of direct parent tags, or an empty set if the tag has no parents.
	 */
	Set<OpenIdentifier> getParents(OpenIdentifier tag);

	/**
	 * Gets all known tag identifiers in this resolver.
	 *
	 * @return An unmodifiable set of all tag identifiers.
	 */
	Set<OpenIdentifier> getAllTags();

	/**
	 * Filters a collection of items, returning those that have the specified tag
	 * either directly or through inheritance.
	 *
	 * @param tag   The tag to filter by.
	 * @param items The collection of items to filter.
	 * @param <T>   The type of taggable item.
	 * @return A list of items that have the tag (directly or inherited).
	 */
	<T extends Taggable> List<T> findTagged(OpenIdentifier tag, Collection<T> items);

	/**
	 * Filters a collection of items, returning those that have the specified tag
	 * assigned directly to them, ignoring inheritance.
	 *
	 * @param tag   The tag to filter by.
	 * @param items The collection of items to filter.
	 * @param <T>   The type of taggable item.
	 * @return A list of items that have the tag directly assigned.
	 */
	<T extends Taggable> List<T> findDirectlyTagged(OpenIdentifier tag, Collection<T> items);

	/**
	 * Merges this resolver with another, returning a combined resolver containing
	 * all relationships from both. Supports universal merge - any TagResolver
	 * implementation can merge with any other.
	 *
	 * @param other The other resolver to merge with.
	 * @return A new resolver containing the combined relationships.
	 */
	TagResolver merge(TagResolver other);

	/**
	 * Exports the parent relationships for merging purposes.
	 * Returns a map where each key is a child tag and the value is the set of its parent tags.
	 *
	 * @return An unmodifiable map of parent relationships.
	 */
	Map<OpenIdentifier, Set<OpenIdentifier>> getRelationships();

	/**
	 * Returns an empty resolver that contains no tags or relationships.
	 * This is the null object pattern - use this instead of null.
	 *
	 * @return A shared, immutable empty resolver instance.
	 */
	static TagResolver empty() {
		return EmptyTagResolver.INSTANCE;
	}

	/**
	 * Creates a resolver from raw parent relationships.
	 *
	 * @param relationships A map where each key is a child tag and the value is the set of its parent tags.
	 * @return A new resolver containing the specified relationships.
	 */
	static TagResolver fromRelationships(Map<OpenIdentifier, Set<OpenIdentifier>> relationships) {
		return new TagGraph(relationships);
	}
}
