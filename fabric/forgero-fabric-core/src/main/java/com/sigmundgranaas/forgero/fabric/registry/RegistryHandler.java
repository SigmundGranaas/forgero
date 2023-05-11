package com.sigmundgranaas.forgero.fabric.registry;

import static com.sigmundgranaas.forgero.core.ForgeroStateRegistry.CONTAINER_TO_STATE;
import static com.sigmundgranaas.forgero.core.ForgeroStateRegistry.TAG_TO_STATE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.registry.StateCollection;
import com.sigmundgranaas.forgero.minecraft.common.service.StateMapper;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class RegistryHandler {
	private static final RegistryHandler HANDLER = new RegistryHandler();
	private final List<Runnable> entries;
	private final List<Consumer<StateService>> syncedEntries;

	public RegistryHandler() {
		this.entries = new ArrayList<>();
		this.syncedEntries = new ArrayList<>();
	}

	public static RegistryHandler getHandler() {
		return HANDLER;
	}

	public void runJobs() {
		run();
	}

	public StateService initialize() {
		StateCollection collection = ForgeroStateRegistry.STATES;
		Map<String, String> itemToStateMap = CONTAINER_TO_STATE;
		Map<String, String> stateToItemMap = ForgeroStateRegistry.STATE_TO_CONTAINER;
		Map<String, String> tagToStateMap = ForgeroStateRegistry.TAG_TO_STATE;
		Map<String, String> stateToTag = ForgeroStateRegistry.STATE_TO_TAG;
		if (collection == null || CONTAINER_TO_STATE == null) {
			Forgero.LOGGER.error("Forgero is not initialized yet. Please wait for the mod to finish loading.");
			throw new IllegalStateException("Forgero is not initialized yet. Please wait for the mod to finish loading.");
		}
		List<Identifier> convertedTags = TAG_TO_STATE.keySet().stream().map(Identifier::new).toList();
		StateMapper mapper = new StateMapper(itemToStateMap, stateToItemMap, stateToTag, tagToStateMap);
		StateService service = new ForgeroInstanceRegistry(convertedTags, collection, Registries.ITEM, itemToStateMap, tagToStateMap, mapper);
		StateService.initialize(service);
		runSynced(service);
		return service;
	}

	public void acceptJob(Runnable handler) {
		this.entries.add(handler);
	}

	public void acceptSyncedJob(Consumer<StateService> handler) {
		this.syncedEntries.add(handler);
	}

	private void run() {
		entries.forEach(Runnable::run);
	}

	private void runSynced(StateService service) {
		syncedEntries.forEach(callable -> callable.accept(service));
	}
}
