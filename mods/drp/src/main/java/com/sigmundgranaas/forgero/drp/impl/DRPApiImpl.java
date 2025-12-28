package com.sigmundgranaas.forgero.drp.impl;

import com.sigmundgranaas.forgero.drp.api.DRPApi;
import com.sigmundgranaas.forgero.drp.api.DynamicResourcePack;
import com.sigmundgranaas.forgero.drp.api.debug.DebugExporter;
import com.sigmundgranaas.forgero.drp.api.lifecycle.HotReloadListener;
import com.sigmundgranaas.forgero.drp.api.lifecycle.ResourcePackPhase;
import com.sigmundgranaas.forgero.drp.impl.debug.DebugExporterImpl;
import com.sigmundgranaas.forgero.drp.impl.pack.DynamicResourcePackImpl;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of DRPApi.
 */
public class DRPApiImpl implements DRPApi {

	private static final DRPApiImpl INSTANCE = new DRPApiImpl();

	private final List<DynamicResourcePack> registeredPacks = Collections.synchronizedList(new ArrayList<>());
	private final Map<DynamicResourcePack, ResourcePackPhase> packPhases = new ConcurrentHashMap<>();
	private final Map<DynamicResourcePack, HotReloadListener> hotReloadListeners = new ConcurrentHashMap<>();

	private DRPApiImpl() {
	}

	public static DRPApiImpl getInstance() {
		return INSTANCE;
	}

	@Override
	public PackBuilder createPack(String id) {
		return createPack(Identifier.tryParse(id));
	}

	@Override
	public PackBuilder createPack(Identifier id) {
		return new PackBuilderImpl(id);
	}

	@Override
	public void register(DynamicResourcePack pack, ResourcePackPhase phase) {
		registeredPacks.add(pack);
		packPhases.put(pack, phase);
	}

	@Override
	public void enableHotReload(DynamicResourcePack pack, HotReloadListener listener) {
		hotReloadListeners.put(pack, listener);
	}

	@Override
	public void triggerHotReload() {
		for (Map.Entry<DynamicResourcePack, HotReloadListener> entry : hotReloadListeners.entrySet()) {
			DynamicResourcePack pack = entry.getKey();
			HotReloadListener listener = entry.getValue();

			// Clear and regenerate
			if (!pack.isSealed()) {
				pack.clear();
				listener.onReload(pack);
			}
		}
	}

	@Override
	public DebugExporter exportToPath(Path outputPath) {
		return new DebugExporterImpl(outputPath, List.copyOf(registeredPacks));
	}

	@Override
	public List<DynamicResourcePack> getRegisteredPacks() {
		return List.copyOf(registeredPacks);
	}

	/**
	 * Gets packs for a specific phase.
	 */
	public List<DynamicResourcePack> getPacksForPhase(ResourcePackPhase phase) {
		return registeredPacks.stream()
				.filter(pack -> packPhases.get(pack) == phase)
				.toList();
	}

	/**
	 * PackBuilder implementation.
	 */
	private static class PackBuilderImpl implements PackBuilder {

		private final Identifier id;
		private String description = "Dynamic Resource Pack";
		private int priority = 0;
		private boolean validateResources;

		PackBuilderImpl(Identifier id) {
			this.id = id;
			// Default to validating in development environment
			this.validateResources = FabricLoader.getInstance().isDevelopmentEnvironment();
		}

		@Override
		public PackBuilder description(String description) {
			this.description = description;
			return this;
		}

		@Override
		public PackBuilder priority(int priority) {
			this.priority = priority;
			return this;
		}

		@Override
		public PackBuilder validateResources(boolean validate) {
			this.validateResources = validate;
			return this;
		}

		@Override
		public DynamicResourcePack build() {
			return new DynamicResourcePackImpl(id, description, priority, validateResources);
		}
	}
}
