package com.sigmundgranaas.forgero.fabric.initialization.datareloader;

import java.util.Set;
import java.util.stream.Collectors;

import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import com.sigmundgranaas.forgero.core.resource.PipelineBuilder;
import com.sigmundgranaas.forgero.fabric.ForgeroInitializer;
import com.sigmundgranaas.forgero.fabric.resources.FabricPackFinder;

import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;

public class DataPipeLineReloader implements SimpleSynchronousResourceReloadListener {
	@Override
	public void reload(ResourceManager manager) {
		var config = ForgeroConfigurationLoader.load(FabricLoader.getInstance().getConfigDir());
		Set<String> availableDependencies = FabricLoader.getInstance().getAllMods().stream().map(ModContainer::getMetadata).map(ModMetadata::getId).collect(Collectors.toSet());
		PipelineBuilder.builder()
				.register(() -> config)
				.register(FabricPackFinder.supplier())
				.state(ForgeroStateRegistry.stateListener())
				.register(availableDependencies)
				.silent()
				.build()
				.execute();
	}

	@Override
	public Identifier getFabricId() {
		return new Identifier(ForgeroInitializer.MOD_NAMESPACE, "data");
	}
}
