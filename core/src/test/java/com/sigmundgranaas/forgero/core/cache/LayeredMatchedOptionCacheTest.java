package com.sigmundgranaas.forgero.core.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Duration;

import org.junit.jupiter.api.Test;

class LayeredMatchedOptionCacheTest {
	@Test
	void cacheReturnsStoredValue() {
		LayeredMatchedOptionCache<String, Integer> cache = new LayeredMatchedOptionCache<>(Duration.ofMinutes(1), 10);
		cache.get("test", () -> 42, value -> value == 42);

		Integer result = cache.get("test", () -> null, value -> true);

		assertEquals(42, result);
	}

	@Test
	void cacheReturnsNewValueFromCallback() {
		LayeredMatchedOptionCache<String, Integer> cache = new LayeredMatchedOptionCache<>(Duration.ofMinutes(1), 10);

		Integer result = cache.get("test", () -> 42, value -> false);

		assertEquals(42, result);
	}

	@Test
	void cacheReturnsCallbackValueIfNoMatchFound() {
		LayeredMatchedOptionCache<String, Integer> cache = new LayeredMatchedOptionCache<>(Duration.ofMinutes(1), 10);

		Integer result = cache.get("test", () -> null, value -> false);

		assertNull(result);
	}

	@Test
	void cacheEvictsExpiredEntries() throws InterruptedException {
		LayeredMatchedOptionCache<String, Integer> cache = new LayeredMatchedOptionCache<>(Duration.ofMillis(100), 10);
		cache.get("test", () -> 42, value -> true);
		Thread.sleep(200);

		Integer result = cache.get("test", () -> null, value -> true);

		assertNull(result);
	}

	@Test
	void cacheEvictsLeastRecentlyUsedEntryWhenFull() {
		LayeredMatchedOptionCache<String, Integer> cache = new LayeredMatchedOptionCache<>(Duration.ofMinutes(1), 2);
		cache.get("first", () -> 1, value -> value == 1);
		cache.get("first", () -> 2, value -> value == 2);
		cache.get("first", () -> 3, value -> value == 3);

		// This should no longer match any entries in the cache, so the fallback value is used.
		Integer result = cache.get("first", () -> 4, value -> value == 1);

		assertEquals(4, result);
	}
}
