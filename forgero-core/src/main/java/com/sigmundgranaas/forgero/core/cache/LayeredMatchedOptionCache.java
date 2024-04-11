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

	public V get(K key, Supplier<V> callback, Predicate<V> matcher) {
		Deque<CacheEntry<V>> entries = prepareMap(key);

		cleanUpEntries(entries, key);

		return findValidEntry(entries, matcher)
				.map(CacheEntry::value)
				.orElseGet(() -> addNewEntry(entries, callback.get()));
	}

	private Deque<CacheEntry<V>> prepareMap(K key) {
		this.now = System.currentTimeMillis();
		if (!cache.containsKey(key)) {
			cache.put(key, new LinkedList<>());
		}

		return cache.get(key);
	}

	private void cleanUpEntries(Deque<CacheEntry<V>> entries, K key) {
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
