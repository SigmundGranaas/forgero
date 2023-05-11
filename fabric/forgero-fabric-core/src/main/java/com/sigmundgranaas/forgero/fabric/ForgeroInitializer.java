package com.sigmundgranaas.forgero.fabric;

import static com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationBlock.ASSEMBLY_STATION_ITEM;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import com.sigmundgranaas.forgero.core.resource.PipelineBuilder;
import com.sigmundgranaas.forgero.fabric.api.entrypoint.ForgeroInitializedEntryPoint;
import com.sigmundgranaas.forgero.fabric.api.entrypoint.ForgeroPreInitializationEntryPoint;
import com.sigmundgranaas.forgero.fabric.registry.RegistryHandler;
import com.sigmundgranaas.forgero.fabric.resources.FabricPackFinder;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.item.ItemGroups;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;


public class ForgeroInitializer implements ModInitializer {
	public static final String MOD_NAMESPACE = "forgero";
	private static final List<ForgeroInitializedEntryPoint> INITIALIZED_ENTRY_POINTS =
			FabricLoader.getInstance().getEntrypointContainers("forgeroInitialized", ForgeroInitializedEntryPoint.class)
					.stream()
					.map(EntrypointContainer::getEntrypoint)
					.toList();
	private static final List<ForgeroPreInitializationEntryPoint> PRE_INITIALIZED_ENTRY_POINTS =
			FabricLoader.getInstance().getEntrypointContainers("forgeroPreInitialization", ForgeroPreInitializationEntryPoint.class)
					.stream()
					.map(EntrypointContainer::getEntrypoint)
					.toList();

	static {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(ASSEMBLY_STATION_ITEM));
	}

	@Override
	public void onInitialize() {
		handlePreInitializedEntryPoints();

		Set<String> availableDependencies = FabricLoader.getInstance().getAllMods().stream().map(ModContainer::getMetadata).map(ModMetadata::getId).collect(Collectors.toSet());

		var configuration = ForgeroConfigurationLoader.load();

		PipelineBuilder
				.builder()
				.register(() -> configuration)
				.register(FabricPackFinder.supplier())
				.state(ForgeroStateRegistry.stateListener())
				.state(ForgeroStateRegistry.compositeListener())
				.createStates(ForgeroStateRegistry.createStateListener())
				.inflated(ForgeroStateRegistry.constructListener())
				.inflated(ForgeroStateRegistry.containerListener())
				.recipes(ForgeroStateRegistry.recipeListener())
				.register(availableDependencies)
				.build()
				.execute();

		StateService service = RegistryHandler.getHandler().initialize();
		handleInitializedEntryPoints(service);
	}

	private void handleInitializedEntryPoints(StateService service) {
		INITIALIZED_ENTRY_POINTS.forEach(entryPoint -> entryPoint.onInitialized(service));
	}

	private void handlePreInitializedEntryPoints() {
		PRE_INITIALIZED_ENTRY_POINTS.forEach(ForgeroPreInitializationEntryPoint::onPreInitialization);
	}
}
