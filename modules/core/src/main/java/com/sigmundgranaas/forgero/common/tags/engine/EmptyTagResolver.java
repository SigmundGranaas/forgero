package com.sigmundgranaas.forgero.common.tags.engine;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.Taggable;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A null object implementation of {@link TagResolver} that contains no tags or relationships.
 * This class is used as the singleton empty resolver returned by {@link TagResolver#empty()}.
 *
 * <p>This implementation:
 * <ul>
 *   <li>Returns false for all {@link #hasTag} checks</li>
 *   <li>Returns empty collections for all queries</li>
 *   <li>Delegates to the other resolver when merged</li>
 * </ul>
 */
public final class EmptyTagResolver implements TagResolver {

	/**
	 * The shared singleton instance.
	 */
	public static final EmptyTagResolver INSTANCE = new EmptyTagResolver();

	private EmptyTagResolver() {
	}

	@Override
	public boolean hasTag(Taggable item, OpenIdentifier tag) {
		return false;
	}

	@Override
	public Set<OpenIdentifier> getDescendants(OpenIdentifier tag) {
		return Set.of(tag);
	}

	@Override
	public Set<OpenIdentifier> getParents(OpenIdentifier tag) {
		return Set.of();
	}

	@Override
	public Set<OpenIdentifier> getAllTags() {
		return Set.of();
	}

	@Override
	public <T extends Taggable> List<T> findTagged(OpenIdentifier tag, Collection<T> items) {
		return List.of();
	}

	@Override
	public <T extends Taggable> List<T> findDirectlyTagged(OpenIdentifier tag, Collection<T> items) {
		return List.of();
	}

	@Override
	public Map<OpenIdentifier, Set<OpenIdentifier>> getRelationships() {
		return Map.of();
	}

	@Override
	public TagResolver merge(TagResolver other) {
		return other;
	}
}
