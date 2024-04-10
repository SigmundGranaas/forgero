package com.sigmundgranaas.forgero.fabric.client.model;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.client.model.BakedModelResult;

public class CachedSingleStateStrategy implements ModelStrategy {
	private final ModelStrategy strategy;
	private final Map<Integer, Deque<CacheEntry>> modelCache;
	private final long cacheEntryTTL; // TTL for each cache entry

	private static class CacheEntry {
		final BakedModelResult result;
		long expiryTime; // When this entry should be considered expired

		CacheEntry(BakedModelResult result, long currentTime) {
			this.result = result;
			this.expiryTime = currentTime;
		}
	}

	public CachedSingleStateStrategy(ModelStrategy strategy, long cacheEntryTTL) {
		this.strategy = strategy;
		this.cacheEntryTTL = cacheEntryTTL;
		this.modelCache = new ConcurrentHashMap<>();
	}

	@Override
	public BakedModelResult getModel(State state, MatchContext context) {
		int stateHash = state.hashCode();
		long now = System.currentTimeMillis();

		if (!modelCache.containsKey(stateHash)) {
			modelCache.put(stateHash, new LinkedList<>());
		}

		Deque<CacheEntry> entries = modelCache.get(stateHash);
		// Attempt to find a valid and non-expired entry
		for (Iterator<CacheEntry> it = entries.iterator(); it.hasNext(); ) {
			CacheEntry entry = it.next();
			if (now > entry.expiryTime) {
				// Remove expired entries
				it.remove();
			} else if (entry.result.result().isValid(state, context)) {
				// Entry is valid and not expired, reset its TTL and move it to the front
				entry.expiryTime = now + cacheEntryTTL;
				// Move to front
				it.remove();
				entries.addFirst(entry);
				return entry.result;
			}
		}

		// If no valid entry is found, or all are expired, fetch a new model and cache it
		BakedModelResult newResult = strategy.getModel(state, context);
		CacheEntry newEntry = new CacheEntry(newResult, now + cacheEntryTTL);
		entries.addFirst(newEntry); // Add new entry to the front

		return newResult;
	}
}
