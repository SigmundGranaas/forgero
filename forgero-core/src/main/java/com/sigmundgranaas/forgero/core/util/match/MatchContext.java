package com.sigmundgranaas.forgero.core.util.match;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A class that holds the matching context. It stores both the metadata for the context and the
 * matches to be evaluated against this context. The class implements Matchable, allowing it to be
 * tested against other Matchable instances.
 */
public class MatchContext implements Matchable {
	protected final Map<String, Object> metadata;
	protected final List<Matchable> matches;

	/**
	 * Default constructor. Initializes metadata as an empty HashMap and matches as an empty ArrayList.
	 */
	public MatchContext() {
		this.metadata = new HashMap<>();
		this.matches = new ArrayList<>();
	}

	/**
	 * Constructor that allows the initialization of metadata and matches with given values.
	 *
	 * @param metadata The metadata Map to be used.
	 * @param matches  The matches List to be used.
	 */
	public MatchContext(Map<String, Object> metadata, List<Matchable> matches) {
		this.metadata = new HashMap<>(metadata);
		this.matches = matches;
	}

	/**
	 * Constructor that initializes metadata as an empty HashMap and allows to initialize matches with given values.
	 *
	 * @param matches The matches List to be used.
	 */
	public MatchContext(List<Matchable> matches) {
		this.metadata = new HashMap<>();
		this.matches = matches;
	}

	public static MatchContext mutable(MatchContext context) {
		return new MutableMatchContext(new HashMap<>(context.metadata), new ArrayList<>(context.matches));
	}

	/**
	 * Creates a new MatchContext with empty metadata and matches.
	 *
	 * @return A new instance of MatchContext.
	 */
	public static MatchContext of() {
		return new MatchContext();
	}

	/**
	 * Creates a new MatchContext with given matches and empty metadata.
	 *
	 * @param matches The matches List to be used.
	 * @return A new instance of MatchContext with the given matches.
	 */
	public static MatchContext of(List<Matchable> matches) {
		return new MatchContext(matches);
	}

	/**
	 * Adds a key-value pair to the metadata Map and returns a new MatchContext with updated metadata.
	 *
	 * @param key   The key to be added to the metadata.
	 * @param value The value associated with the key.
	 * @return A new MatchContext instance with the updated metadata.
	 */
	public MatchContext put(String key, Object value) {
		Map<String, Object> metadata = new HashMap<>(this.metadata);
		metadata.put(key, value);
		return new MatchContext(metadata, new ArrayList<>(matches));
	}

	/**
	 * Adds a matchable to the matches List if it is not already present, and returns a new MatchContext with updated matches.
	 *
	 * @param matchable The Matchable instance to be added to the matches.
	 * @return A new MatchContext instance with the updated matches.
	 */
	public MatchContext add(Matchable matchable) {
		List<Matchable> matches = new ArrayList<>(this.matches);
		if (!matches.contains(matchable)) {
			matches.add(matchable);
		}
		return new MatchContext(new HashMap<>(metadata), matches);
	}

	/**
	 * Tests whether a given Matchable instance matches any of the Matchable instances in this context.
	 *
	 * @param match   The Matchable instance to test.
	 * @param context The MatchContext for the test.
	 * @return True if the given Matchable matches any Matchable in this context, otherwise false.
	 */
	@Override
	public boolean test(Matchable match, MatchContext context) {
		return matches.stream().anyMatch(matchable -> matchable.test(match, context));
	}

	/**
	 * Retrieves a value from the metadata Map and attempts to cast it to a specified class.
	 *
	 * @param <T>  The class type to cast to.
	 * @param key  The key for the value to be retrieved from the metadata Map.
	 * @param type The class to cast the retrieved value to.
	 * @return An Optional of the value if it is present and can be cast to the specified class. Otherwise, an empty Optional.
	 */
	public <T> Optional<T> get(String key, Class<T> type) {
		Object value = metadata.get(key);
		if (type.isInstance(value)) {
			return Optional.of(type.cast(value));
		}
		return Optional.empty();
	}

	/**
	 * Retrieves a value from the metadata Map using a ContextKey and attempts to cast it to the class defined by the ContextKey.
	 *
	 * @param <T> The class type to cast to.
	 * @param key The ContextKey for the value to be retrieved from the metadata Map.
	 * @return An Optional of the value if it is present and can be cast to the specified class. Otherwise, an empty Optional.
	 */
	public <T> Optional<T> get(ContextKey<T> key) {
		return get(key.getKey(), key.getClazz());
	}

	/**
	 * Adds a key-value pair to the metadata Map using a ContextKey and returns a new MatchContext with updated metadata.
	 *
	 * @param key   The ContextKey to be added to the metadata.
	 * @param value The value associated with the ContextKey.
	 * @return A new MatchContext instance with the updated metadata.
	 */
	public MatchContext put(ContextKey<?> key, Object value) {
		return put(key.getKey(), value);
	}
}
