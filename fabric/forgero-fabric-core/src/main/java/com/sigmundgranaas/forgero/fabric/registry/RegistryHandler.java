package com.sigmundgranaas.forgero.fabric.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.registry.StateCollection;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import com.sigmundgranaas.forgero.minecraft.common.service.UninitializedStateService;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RegistryHandler {
	private static final RegistryHandler HANDLER = new RegistryHandler();
	private final List<Runnable> entries;
	private final List<Consumer<StateService>> syncedEntries;

	public RegistryHandler() {
		this.entries = new ArrayList<>();
		this.syncedEntries = new ArrayList<>();
	}

	public static StateService getService() {
		if (!StateService..isInitialized()) {
			Forgero.LOGGER.warn("Forgero is not initialized yet. Please wait for the mod to finish loading.");
			return SERVICE;
		}
		return SERVICE;
	}

	public static RegistryHandler getHandler() {
		return HANDLER;
	}

	public void runJobs() {
		run();
	}

	public void initialize() {
		StateCollection collection = ForgeroStateRegistry.STATES;
		List<String> tags = ForgeroStateRegistry.TAGS;
		if (collection == null || tags == null) {
			Forgero.LOGGER.warn("Forgero is not initialized yet. Please wait for the mod to finish loading.");
			return;
		}
		List<Identifier> convertedTags = tags.stream().map(Identifier::new).toList();
		SERVICE = new ForgeroInstanceRegistry(convertedTags, collection, Registry.ITEM);
		StateService.INSTANCE = () -> SERVICE;
		runSynced(SERVICE);
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
