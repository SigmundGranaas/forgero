package com.sigmundgranaas.forgero.core.registry;

import com.sigmundgranaas.forgero.core.soul.PropertyLevelProvider;
import com.sigmundgranaas.forgero.core.soul.SoulLevelPropertyHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SoulLevelPropertyRegistry {
	private final static Map<String, PropertyLevelProvider> MAP = new ConcurrentHashMap<>();
	public SoulLevelPropertyHandler handler;

	public static SoulLevelPropertyHandler handler() {
		return new SoulLevelPropertyHandler(MAP);
	}

	public static void register(String id, PropertyLevelProvider prop) {
		MAP.put(id, prop);
	}

	public static void refresh() {
		MAP.clear();
	}
}
