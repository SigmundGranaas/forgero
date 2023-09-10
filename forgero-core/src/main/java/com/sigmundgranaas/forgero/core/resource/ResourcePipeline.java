package com.sigmundgranaas.forgero.core.resource;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.configuration.ForgeroConfiguration;
import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import com.sigmundgranaas.forgero.core.resource.data.DataBuilder;
import com.sigmundgranaas.forgero.core.resource.data.StateConverter;
import com.sigmundgranaas.forgero.core.resource.data.v2.DataPackage;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.DependencyData;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.RecipeData;
import com.sigmundgranaas.forgero.core.resource.data.v2.factory.TypeFactory;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.TypeTree;

public class ResourcePipeline {
	private final List<DataPackage> packages;
	private final List<ResourceListener<List<DataResource>>> dataListeners;
	private final List<ResourceListener<List<DataResource>>> inflatedDataListener;
	private final List<ResourceListener<Map<String, State>>> stateListener;
	private final List<ResourceListener<List<RecipeData>>> recipeListener;
	private final List<ResourceListener<List<String>>> createStateListener;
	private final Set<String> dependencies;
	private final ForgeroConfiguration configuration;
	private final boolean silent;
	private List<String> createsStates;
	private Map<String, String> idMapper;
	private TypeTree tree;
	private List<RecipeData> recipes;

	public ResourcePipeline(List<DataPackage> packages, List<ResourceListener<List<DataResource>>> dataListeners, List<ResourceListener<Map<String, State>>> stateListener, List<ResourceListener<List<DataResource>>> inflatedDataListener, List<ResourceListener<List<RecipeData>>> recipeListener, List<ResourceListener<List<String>>> createStateListener, ForgeroConfiguration configuration, Set<String> modDependencies, boolean silent) {
		this.packages = packages;
		this.dataListeners = dataListeners;
		this.inflatedDataListener = inflatedDataListener;
		this.stateListener = stateListener;
		this.recipeListener = recipeListener;
		this.createStateListener = createStateListener;
		this.tree = new TypeTree();
		this.idMapper = new HashMap<>();
		this.recipes = new ArrayList<>();
		this.dependencies = new HashSet<>(ImmutableSet.<String>builder().add("forgero", "minecraft").addAll(modDependencies).build());
		this.configuration = configuration;
		this.silent = silent;
	}

	public static boolean filterDependencies(Set<String> availableDependencies, DependencyData dependencyData, String id, boolean silent, ForgeroConfiguration configuration) {
		if (availableDependencies.isEmpty()) {
			return true;
		}
		boolean hasAllDependencies = availableDependencies.containsAll(dependencyData.getDependencies());
		boolean hasAnyOfDependencies = availableDependencies.stream().anyMatch(dependencyData.getAny_of()::contains) || dependencyData.getAny_of().isEmpty();
		boolean hasNoneOf = availableDependencies.stream().noneMatch(dependencyData.getNone_of()::contains) || dependencyData.getNone_of().isEmpty();
		if (hasAllDependencies && hasAnyOfDependencies && hasNoneOf) {
			return true;

		} else {
			if (configuration.logDisabledPackages) {
				if (!hasAllDependencies) {
					var missingDependencies = dependencyData.getDependencies().stream().filter(depend -> !availableDependencies.contains(depend)).toList();
					if (!silent) {
						Forgero.LOGGER.info("{} was disabled due to lacking dependencies: {}", id, missingDependencies);
					}
				} else if (!hasAnyOfDependencies) {
					if (!silent) {
						Forgero.LOGGER.info("{} was disabled due to missing any of these dependencies: {}", id, dependencyData.getAny_of());
					}
				} else {
					if (!silent) {
						Forgero.LOGGER.info("{} was disabled due to the presence of any of these dependencies: {}", id, dependencyData.getNone_of());
					}
				}

			}
			return false;
		}

	}

	public void execute() {
		List<DataPackage> validatedPackages = validatePackages(packages);

		List<DataResource> validatedResources = validateResources(validatedPackages);

		tree = assembleTypeTree(validatedResources);
		var dataBuilder = DataBuilder.of(validatedResources, tree);
		List<DataResource> resources = dataBuilder.buildResources();
		this.recipes = dataBuilder.recipes();

		Map<String, State> states = mapStates(resources);

		recipeListener.forEach(listener -> listener.listen(recipes, tree, idMapper));
		dataListeners.forEach(listener -> listener.listen(validatedResources, tree, idMapper));
		inflatedDataListener.forEach(listener -> listener.listen(resources, tree, idMapper));
		stateListener.forEach(listener -> listener.listen(states, tree, idMapper));
		createStateListener.forEach(listener -> listener.listen(createsStates, tree, idMapper));
	}

	private Map<String, State> mapStates(List<DataResource> validatedResources) {
		StateConverter converter = new StateConverter(tree);
		validatedResources.forEach(converter::convert);
		idMapper = converter.nameMapper();
		createsStates = converter.createStates().stream().map(idMapper::get).distinct().toList();
		return converter.states();
	}

	private TypeTree assembleTypeTree(List<DataResource> resources) {
		TypeTree tree = new TypeTree();
		TypeFactory
				.convert(resources)
				.forEach(tree::addNode);
		tree.resolve();
		return tree;
	}

	private List<DataPackage> validatePackages(List<DataPackage> packages) {
		dependencies.add("forgero");
		dependencies.add("minecraft");

		packages.forEach(pack -> dependencies.add(pack.name()));

		var validatedResources = packages.stream().filter(this::filterPackages).toList();
		if (configuration.resourceLogging && !silent) {
			Forgero.LOGGER.info("Registered and validated {} Forgero packages", validatedResources.size());
			Forgero.LOGGER.info("{}", validatedResources.stream().map(DataPackage::name).toList());
		}
		return validatedResources;
	}

	private boolean filterPackages(DataPackage dataPackage) {
		if (!filterPacks(dataPackage)) {
			return false;
		}
		return filterDependencies(dependencies, dataPackage.dependencies(), dataPackage.identifier(), silent, configuration);
	}

	private List<DataResource> validateResources(List<DataPackage> resources) {
		return resources.parallelStream()
				.map(DataPackage::loadData)
				.flatMap(List::stream)
				.filter(this::filterResources)
				.toList();
	}

	private boolean filterResources(DataResource resource) {
		boolean filter = ForgeroConfigurationLoader.configuration.disabledResources.stream().noneMatch(disabled -> resource.identifier().equals(disabled));
		if (!filter && ForgeroConfigurationLoader.configuration.resourceLogging && !silent) {
			Forgero.LOGGER.info(MessageFormat.format("{0} was disabled by the configuration, located at {1}", resource.identifier(), ForgeroConfigurationLoader.configurationFilePath));
		}

		return filterDependencies(dependencies, resource.dependencies(), resource.identifier(), silent, configuration);
	}

	private boolean filterPacks(DataPackage dataPackage) {
		boolean filter = ForgeroConfigurationLoader.configuration.disabledPacks.stream().noneMatch(disabled -> dataPackage.identifier().equals(disabled));
		if (!filter && ForgeroConfigurationLoader.configuration.resourceLogging && !silent) {
			Forgero.LOGGER.info(MessageFormat.format("{0} was disabled by the configuration, located at {1}", dataPackage.identifier(), ForgeroConfigurationLoader.configurationFilePath));
		}

		return filter;
	}
}
