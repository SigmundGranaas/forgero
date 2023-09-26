package com.sigmundgranaas.forgero.core.model;

import static com.sigmundgranaas.forgero.core.model.ModelResult.MODEL_RESULT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.resource.ResourceListener;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.ModelData;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.PaletteData;
import com.sigmundgranaas.forgero.core.state.Identifiable;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.TypeTree;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;

public class ModelRegistry {
	private final HashMap<String, ModelMatcher> modelMap;
	private final Map<String, PaletteTemplateModel> textures;

	private final Map<String, String> paletteRemapper;
	private final Map<String, PaletteData> palettes;

	private final HashMap<String, ArrayList<ModelData>> delayedModels;

	private final HashMap<String, ModelData> generationModels;
	private TypeTree tree;

	public ModelRegistry(TypeTree tree) {
		this.tree = tree;
		this.palettes = new HashMap<>();
		this.modelMap = new HashMap<>();
		this.paletteRemapper = new HashMap<>();
		this.textures = new HashMap<>();
		this.delayedModels = new HashMap<>();
		this.generationModels = new HashMap<>();
	}

	public ModelRegistry() {
		this.tree = new TypeTree();
		this.palettes = new HashMap<>();
		this.modelMap = new HashMap<>();
		this.textures = new HashMap<>();
		this.delayedModels = new HashMap<>();
		this.paletteRemapper = new HashMap<>();
		this.generationModels = new HashMap<>();
	}

	public ResourceListener<List<DataResource>> modelListener() {
		return (resources, tree, idMapper) -> {
			this.tree = tree;
			resources.stream().filter(resource -> !resource.models().isEmpty()).forEach(this::register);
		};
	}

	public ResourceListener<List<DataResource>> paletteListener() {
		return (resources, tree, idMapper) -> resources.stream().filter(res -> res.palette().isPresent()).forEach(res -> paletteHandler(res, tree));
	}

	private void paletteHandler(DataResource resource, TypeTree tree) {
		var paletteData = resource.palette();
		if (paletteData.isPresent()) {
			PaletteData palette = paletteData.get().toBuilder().target(resource.name()).build();
			tree.find(resource.type()).ifPresent(node -> node.addResource(palette, PaletteData.class));
			palettes.put(palette.getName(), palette);
			if (!paletteData.get().getName().equals(resource.name())) {
				paletteRemapper.put(resource.name() + ".png", paletteData.get().getName() + ".png");
			}
		}
	}

	public void setTree(TypeTree tree) {
		this.tree = tree;
	}

	public ModelRegistry register(DataResource data) {
		var converter = new ModelConverter(tree, palettes, modelMap, textures, delayedModels, generationModels);
		converter.register(data);
		return this;
	}

	public Optional<ModelResult> find(State state, MatchContext context) {
		if (modelMap.containsKey(state.identifier())) {
			return modelMap.get(state.identifier()).get(state, this::provider, MatchContext.of()).map(model -> new ModelResult().setTemplate(model));
		} else {
			var result = new ModelResult();
			context.put(MODEL_RESULT, result);
			var modelEntries = tree.find(state.type().typeName())
					.map(node -> node.getResources(ModelMatcher.class))
					.orElse(ImmutableList.<ModelMatcher>builder().build());

			return modelEntries.stream()
					.sorted(ModelMatcher::comparator)
					.filter(entry -> entry.match(state, context))
					.map(modelMatcher -> modelMatcher.get(state, this::provider, context))
					.flatMap(Optional::stream)
					.findFirst()
					.map(result::setTemplate);
		}
	}

	public Optional<ModelResult> find(State state) {
		return find(state, MatchContext.of());
	}


	public Optional<ModelMatcher> provider(Identifiable id) {
		if (modelMap.containsKey(id.identifier())) {
			return Optional.ofNullable(modelMap.get(id.identifier()));
		} else if (modelMap.containsKey(id.name())) {
			return Optional.ofNullable(modelMap.get(id.name()));
		} else if (id instanceof State state) {
			return Optional.of(MultipleModelMatcher.of(tree.find(state.type().typeName()).map(node -> node.getResources(ModelMatcher.class)).orElse(ImmutableList.<ModelMatcher>builder().build())));
		}
		return Optional.empty();
	}

	public Map<String, PaletteTemplateModel> getTextures() {
		return textures;
	}

	public Map<String, String> getPaletteRemapper() {
		return paletteRemapper;
	}
}
