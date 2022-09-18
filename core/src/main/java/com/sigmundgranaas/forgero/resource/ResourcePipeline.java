package com.sigmundgranaas.forgero.resource;

import com.sigmundgranaas.forgero.resource.data.DataBuilder;
import com.sigmundgranaas.forgero.resource.data.StateConverter;
import com.sigmundgranaas.forgero.resource.data.v2.DataPackage;
import com.sigmundgranaas.forgero.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.resource.data.v2.factory.TypeFactory;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.type.TypeTree;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ResourcePipeline {
    private final List<DataPackage> packages;
    private final List<ResourceListener<List<DataResource>>> dataListeners;
    private final List<ResourceListener<List<DataResource>>> inflatedDataListener;
    private final List<ResourceListener<Map<String, State>>> stateListener;
    private Map<String, String> idMapper;
    private TypeTree tree;


    public ResourcePipeline(List<DataPackage> packages, List<ResourceListener<List<DataResource>>> dataListeners, List<ResourceListener<Map<String, State>>> stateListener, List<ResourceListener<List<DataResource>>> inflatedDataListener) {
        this.packages = packages;
        this.dataListeners = dataListeners;
        this.inflatedDataListener = inflatedDataListener;
        this.stateListener = stateListener;
        this.tree = new TypeTree();
        this.idMapper = new HashMap<>();
    }

    public void execute() {
        List<DataPackage> validatedPackages = validatePackages(packages);

        List<DataResource> validatedResources = validateResources(validatedPackages);

        tree = assembleTypeTree(validatedResources);
        
        List<DataResource> resources = DataBuilder.of(validatedResources, tree).buildResources();

        Map<String, State> states = mapStates(resources);


        dataListeners.forEach(listener -> listener.listen(validatedResources, tree, idMapper));
        inflatedDataListener.forEach(listener -> listener.listen(resources, tree, idMapper));
        stateListener.forEach(listener -> listener.listen(states, tree, idMapper));
    }

    private Map<String, State> mapStates(List<DataResource> validatedResources) {
        StateConverter converter = new StateConverter(tree);
        validatedResources.forEach(converter::convert);
        idMapper = converter.nameMapper();
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
        var packs = new HashSet<String>();
        packs.add("forgero");
        packs.add("minecraft");

        packages.forEach(pack -> packs.add(pack.name()));
        return packages.stream().filter(pack -> packs.containsAll(pack.dependencies())).toList();
    }

    private List<DataResource> validateResources(List<DataPackage> resources) {
        return resources.stream().map(DataPackage::data).flatMap(List::stream).toList();
    }
}
