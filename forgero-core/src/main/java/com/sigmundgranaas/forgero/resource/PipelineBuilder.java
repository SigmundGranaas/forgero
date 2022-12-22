package com.sigmundgranaas.forgero.resource;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.resource.data.v2.DataPackage;
import com.sigmundgranaas.forgero.resource.data.v2.PackageSupplier;
import com.sigmundgranaas.forgero.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.resource.data.v2.data.RecipeData;
import com.sigmundgranaas.forgero.settings.ForgeroSettings;
import com.sigmundgranaas.forgero.state.State;

import java.util.*;

public class PipelineBuilder {

    private final List<DataPackage> packages = new ArrayList<>();

    private final Set<String> dependencies = new HashSet<>();
    private final List<ResourceListener<List<DataResource>>> dataListeners = new ArrayList<>();
    private final List<ResourceListener<List<DataResource>>> inflatedDataListener = new ArrayList<>();
    private final List<ResourceListener<Map<String, State>>> stateListener = new ArrayList<>();
    private final List<ResourceListener<List<RecipeData>>> recipeListener = new ArrayList<>();

    private final List<ResourceListener<List<String>>> createStateListener = new ArrayList<>();

    private ForgeroSettings settings = ForgeroSettings.builder().build();

    public static PipelineBuilder builder() {
        return new PipelineBuilder();
    }

    public PipelineBuilder register(DataPackage dataPackage) {
        if (settings.getResourceLogging()) {
            Forgero.LOGGER.info("Registered {}", dataPackage.name());
        }
        packages.add(dataPackage);
        return this;
    }

    public PipelineBuilder data(ResourceListener<List<DataResource>> listener) {
        dataListeners.add(listener);
        return this;
    }

    public PipelineBuilder inflated(ResourceListener<List<DataResource>> listener) {
        inflatedDataListener.add(listener);
        return this;
    }

    public PipelineBuilder state(ResourceListener<Map<String, State>> listener) {
        stateListener.add(listener);
        return this;
    }

    public PipelineBuilder recipes(ResourceListener<List<RecipeData>> listener) {
        recipeListener.add(listener);
        return this;
    }

    public PipelineBuilder createStates(ResourceListener<List<String>> listener) {
        createStateListener.add(listener);
        return this;
    }

    public PipelineBuilder register(PackageSupplier supplier) {
        var packs = supplier.supply();
        if (settings.getResourceLogging()) {
            packs.forEach(pack -> Forgero.LOGGER.info("Registered {}", pack.name()));
        }
        packages.addAll(packs);
        return this;
    }

    public PipelineBuilder register(ForgeroSettings settings) {
        this.settings = settings;
        return this;
    }

    public PipelineBuilder register(Set<String> dependencies) {
        this.dependencies.addAll(dependencies);
        return this;
    }

    public ResourcePipeline build() {
        return new ResourcePipeline(packages, dataListeners, stateListener, inflatedDataListener, recipeListener, settings, dependencies, createStateListener);
    }
}
