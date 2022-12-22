package com.sigmundgranaas.forgero.resource;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.resource.data.DataBuilder;
import com.sigmundgranaas.forgero.resource.data.StateConverter;
import com.sigmundgranaas.forgero.resource.data.v2.DataPackage;
import com.sigmundgranaas.forgero.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.resource.data.v2.data.RecipeData;
import com.sigmundgranaas.forgero.resource.data.v2.factory.TypeFactory;
import com.sigmundgranaas.forgero.settings.ForgeroSettings;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.type.TypeTree;

import java.util.*;

public class ResourcePipeline {
    private final List<DataPackage> packages;
    private final List<ResourceListener<List<DataResource>>> dataListeners;
    private final List<ResourceListener<List<DataResource>>> inflatedDataListener;
    private final List<ResourceListener<Map<String, State>>> stateListener;
    private final List<ResourceListener<List<RecipeData>>> recipeListener;

    private final List<ResourceListener<List<String>>> createStateListener;
    private final ForgeroSettings settings;
    private final Set<String> dependencies;
    private List<String> createsStates;
    private Map<String, String> idMapper;
    private TypeTree tree;
    private List<RecipeData> recipes;


    public ResourcePipeline(List<DataPackage> packages, List<ResourceListener<List<DataResource>>> dataListeners, List<ResourceListener<Map<String, State>>> stateListener, List<ResourceListener<List<DataResource>>> inflatedDataListener, List<ResourceListener<List<RecipeData>>> recipeListener, ForgeroSettings settings, Set<String> dependencies, List<ResourceListener<List<String>>> createStateListener) {
        this.packages = packages;
        this.dataListeners = dataListeners;
        this.inflatedDataListener = inflatedDataListener;
        this.stateListener = stateListener;
        this.recipeListener = recipeListener;
        this.createStateListener = createStateListener;
        this.tree = new TypeTree();
        this.idMapper = new HashMap<>();
        this.recipes = new ArrayList<>();
        this.settings = settings;
        this.dependencies = dependencies;
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


        return packages.stream().filter(this::filterPackages).toList();
    }

    private boolean filterPackages(DataPackage dataPackage) {
        if (!settings.filterPacks(dataPackage)) {
            return false;
        }
        if (!dependencies.containsAll(dataPackage.dependencies())) {
            if (settings.getResourceLogging()) {
                var missingDependencies = dataPackage.dependencies().stream().filter(depend -> !dependencies.contains(depend)).toList();
                Forgero.LOGGER.info("{} was disabled due to lacking dependencies: {}", dataPackage.identifier(), missingDependencies);
            }
            return false;
        }

        return true;
    }

    private List<DataResource> validateResources(List<DataPackage> resources) {
        return resources.stream().map(DataPackage::data).flatMap(List::stream).filter(settings::filterResources).toList();
    }
}
