package com.sigmundgranaas.forgero.core.cache;

import java.time.Duration;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A generic caching utility that stores values based on key, with support for time-to-live (TTL)
 * and maximum size constraints per key. This cache is designed to be layered, meaning that
 * it can store multiple entries per key, each with individual expiration and access metrics.
 * <p>
 * The cache is intended for scenarios where entries are not only time-sensitive but also
 * need to be validated against a predicate before retrieval. This is useful in situations
 * where cache entries might become stale or invalid under certain conditions, and fresh data
 * needs to be fetched dynamically.
 *
 * @param <K> The type of keys maintained by this cache.
 * @param <V> The type of mapped values.
 */
public class LayeredMatchedOptionCache<K, V> {
	private final Map<K, Deque<CacheEntry<V>>> cache;
	private final long entryTTL; // Time to live for each cache entry
	private final int maxCacheSize; // Maximum number of entries in each of the sections of the cache
	private long now;

	public LayeredMatchedOptionCache(Duration entryTTL, Integer maxCacheSize) {
		this.entryTTL = entryTTL != null ? entryTTL.toMillis() : Long.MAX_VALUE;
		this.maxCacheSize = maxCacheSize != null ? maxCacheSize : Integer.MAX_VALUE;
		this.cache = new ConcurrentHashMap<>();
	}

	/**
	 * Retrieves a value based on the key, using a callback and a matcher predicate.
	 * If the cache contains valid entries for the key, it tries to find an entry that matches
	 * the predicate. If no valid entry is found, the callback is used to generate a new value,
	 * which is then cached and returned.
	 *
	 * @param key      The key whose associated value is to be returned.
	 * @param callback A {@link Supplier} that provides a new value when required.
	 * @param matcher  A {@link Predicate} that must be satisfied by a cached value for it to be considered valid.
	 * @return The value associated with the key or newly fetched if no valid cache entry exists.
	 */
	public V get(K key, Supplier<V> callback, Predicate<V> matcher) {
		Deque<CacheEntry<V>> entries = prepareMap(key);

		cleanUpEntries(entries, key);

		return findValidEntry(entries, matcher)
				.map(CacheEntry::value)
				.orElseGet(() -> addNewEntry(entries, callback.get()));
	}

	/**
	 * Retrieves the second-most recently accessed value for a specific key, if available.
	 * This method is typically used for fetching previous state values when the most recent
	 * value may already be engaged or locked in processing.
	 *
	 * @param key The key for which the previous value is requested.
	 * @return An {@link Optional<V>} containing the previous value if available, otherwise empty.
	 */
	public Optional<V> getPrevious(K key) {
		Deque<CacheEntry<V>> entries = prepareMap(key);
		if (entries.isEmpty()) {
			return Optional.empty();
		}
		if (entries.size() == 1) {
			return Optional.of(entries.peek().value);
		} else {
			Iterator<CacheEntry<V>> it = entries.iterator();
			it.next();
			return Optional.of(it.next().value);
		}
	}

	private Deque<CacheEntry<V>> prepareMap(K key) {
		this.now = System.currentTimeMillis();
		if (!cache.containsKey(key)) {
			cache.put(key, new LinkedList<>());
		}

		return cache.get(key);
	}

	private void cleanUpEntries(Deque<CacheEntry<V>> entries, K key) {
		if (entries.isEmpty()) {
			return;
		}
		for (Iterator<CacheEntry<V>> it = entries.iterator(); it.hasNext(); ) {
			CacheEntry<V> entry = it.next();
			if (now > entry.expiryTime || entries.size() > maxCacheSize) {
				it.remove();
			}
		}
		if (entries.isEmpty()) {
			this.cache.remove(key);
		}
	}

	private long newExpiry() {
		return now + entryTTL;
	}

	private Optional<CacheEntry<V>> findValidEntry(Deque<CacheEntry<V>> entries, Predicate<V> matcher) {
		for (Iterator<CacheEntry<V>> it = entries.iterator(); it.hasNext(); ) {
			CacheEntry<V> entry = it.next();
			if (matcher.test(entry.value)) {
				entry.hit(newExpiry());
				if (entries.peekFirst() != entry) {
					it.remove();
					entries.addFirst(entry);
				}
				return Optional.of(entry);
			}
		}
		return Optional.empty();
	}

	private V addNewEntry(Deque<CacheEntry<V>> entries, V newValue) {
		CacheEntry<V> newEntry = new CacheEntry<>(newValue, newExpiry());
		newEntry.hitCount = 1;
		entries.addFirst(newEntry);

		if (entries.size() > maxCacheSize) {
			entries.removeLast();
		}

		return newValue;
	}

	private static class CacheEntry<V> {
		final V value;
		long expiryTime;
		int hitCount;

		CacheEntry(V value, long expiryTime) {
			this.value = value;
			this.expiryTime = expiryTime;
			this.hitCount = 0;
		}

		void hit(long newExpiryTime) {
			this.expiryTime = newExpiryTime;
			this.hitCount++;
		}

		V value() {
			return this.value;
		}
	}
}
