package com.sigmundgranaas.forgero.core.model;

import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.sigmundgranaas.forgero.core.model.match.PredicateFactory;
import com.sigmundgranaas.forgero.core.model.match.PredicateMatcher;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.ModelData;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.ModelEntryData;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.PaletteData;
import com.sigmundgranaas.forgero.core.texture.utils.Offset;
import com.sigmundgranaas.forgero.core.type.TypeTree;

/**
 * This class is responsible for converting model data files to actual model matchers.
 * Model matchers are used to dynamically create textures and also determine the logic for tool and weapon modeling.
 */
public class ModelConverter {
	private final TypeTree tree;

	private final HashMap<String, ModelMatcher> models;

	private final HashMap<String, ArrayList<ModelData>> delayedModels;

	private final Map<String, PaletteData> palettes;

	private final HashMap<String, ModelData> generationModels;

	private final Map<String, PaletteTemplateModel> textures;

	/**
	 * Constructor for the ModelConverter class.
	 *
	 * @param tree             The type tree of models.
	 * @param palettes         The mapping of palette data.
	 * @param models           The mapping of model matchers.
	 * @param textures         The mapping of texture models.
	 * @param delayedModels    Models which processing has been delayed.
	 * @param generationModels Models to be generated.
	 */
	public ModelConverter(TypeTree tree, Map<String, PaletteData> palettes, HashMap<String, ModelMatcher> models, Map<String, PaletteTemplateModel> textures, HashMap<String, ArrayList<ModelData>> delayedModels, HashMap<String, ModelData> generationModels) {
		this.tree = tree;
		this.palettes = palettes;
		this.delayedModels = delayedModels;
		this.generationModels = generationModels;
		this.models = models;
		this.textures = textures;
	}

	/**
	 * Checks if a given string is not empty.
	 *
	 * @param test String to be checked.
	 * @return True if not empty, false otherwise.
	 */
	public static boolean notEmpty(String test) {
		return !test.equals(EMPTY_IDENTIFIER);
	}

	/**
	 * Registers model data for a given resource.
	 *
	 * @param resource The data resource to register.
	 */
	public void register(DataResource resource) {
		resource.models().forEach(data -> register(data, resource.type()));
	}

	/**
	 * Registers a model data with its type.
	 *
	 * @param data The model data.
	 * @param type The type of the model.
	 */
	public void register(ModelData data, String type) {
		if (data.getName().equals(EMPTY_IDENTIFIER)) {
			handleEmptyIdentifier(data, type);
		} else if (notEmpty(data.getName())) {
			handleNonEmptyIdentifier(data);
		}
	}

	/**
	 * Handles models with empty identifiers.
	 *
	 * @param data The model data.
	 * @param type The type of the model.
	 */
	private void handleEmptyIdentifier(ModelData data, String type) {
		ModelMatcher model = createModelMatcher(data);
		tree.find(type).ifPresent(node -> node.addResource(model, ModelMatcher.class));
	}

	/**
	 * Creates a ModelMatcher from given ModelData.
	 *
	 * @param data The model data.
	 * @return A ModelMatcher object.
	 */
	private ModelMatcher createModelMatcher(ModelData data) {
		var match = PredicateMatcher.of(data.getPredicates(), new PredicateFactory());
		return switch (data.getModelType()) {
			case "BASED_COMPOSITE", "UPGRADE" ->
					new ModelMatchPairing(match, new TemplatedModelEntry(data.getTemplate()));
			case "COMPOSITE" -> new ModelMatchPairing(match, new CompositeModelEntry());
			default -> ModelMatcher.EMPTY;
		};
	}

	/**
	 * Handles models with non-empty identifiers.
	 *
	 * @param data The model data.
	 */
	private void handleNonEmptyIdentifier(ModelData data) {
		if (data.getTemplate().equals(EMPTY_IDENTIFIER)) {
			if (this.generationModels.containsKey(data.getName())) {
				processGeneratedModels(data);
			} else {
				delayModelProcessing(data);
			}
		} else {
			processNonGeneratedModels(data);
		}
	}

	/**
	 * Processes models that are set to be generated.
	 *
	 * @param data The model data.
	 */
	private void processGeneratedModels(ModelData data) {
		var modelData = this.generationModels.get(data.getName()).toBuilder().variants(data.getVariants()).build();
		var pairings = pairings(modelData);
		var model = new MatchedModelEntry(pairings, data.getName());
		addOrUpdateModel(data, model, pairings);
	}

	/**
	 * Delays the processing of a model.
	 *
	 * @param data The model data.
	 */
	private void delayModelProcessing(ModelData data) {
		this.delayedModels.computeIfAbsent(data.getName(), k -> new ArrayList<>()).add(data);
	}

	/**
	 * Processes models that are not set to be generated.
	 *
	 * @param data The model data.
	 */
	private void processNonGeneratedModels(ModelData data) {
		this.generationModels.put(data.getName(), data);
		var variantsBuilder = ImmutableList.<ModelEntryData>builder().addAll(data.getVariants());

		if (this.delayedModels.containsKey(data.getName())) {
			variantsBuilder.addAll(this.delayedModels.get(data.getName()).stream().map(ModelData::getVariants).flatMap(List::stream).toList());
			delayedModels.remove(data.getName());
		}

		var modelData = data.toBuilder().variants(variantsBuilder.build()).build();
		var pairings = pairings(modelData);
		var model = new MatchedModelEntry(pairings, data.getName());
		addOrUpdateModel(data, model, pairings);
	}

	/**
	 * Adds or updates the model matchers.
	 *
	 * @param data     Model data to be processed.
	 * @param model    Model matcher to be processed.
	 * @param pairings List of model match pairings.
	 */
	private void addOrUpdateModel(ModelData data, ModelMatcher model, List<ModelMatchPairing> pairings) {
		if (this.models.containsKey(data.getName()) && this.models.get(data.getName()) instanceof MatchedModelEntry existingMatcher) {
			existingMatcher.add(pairings);
		} else {
			this.models.put(data.getName(), model);
		}
	}

	/**
	 * Pairs the given model data to generate model match pairings.
	 *
	 * @param data Model data.
	 * @return A list of model match pairings.
	 */
	private List<ModelMatchPairing> pairings(ModelData data) {
		List<PaletteData> palettes = findPalettes(data);

		if (!data.getModelType().equals("GENERATE")) {
			return createPairings(palettes, data);
		} else {
			return generatePairings(palettes, data);
		}
	}

	/**
	 * Finds palettes associated with the given model data.
	 *
	 * @param data Model data.
	 * @return A list of associated palettes.
	 */
	private List<PaletteData> findPalettes(ModelData data) {
		List<PaletteData> palettes = tree.find(data.getPalette())
				.map(node -> node.getResources(PaletteData.class))
				.orElse(ImmutableList.<PaletteData>builder().build());

		if (palettes.isEmpty()) {
			PaletteData paletteData = this.palettes.get(data.getPalette());
			if (paletteData != null) {
				palettes = List.of(paletteData);
			}
		}

		return palettes;
	}

	/**
	 * Generates model match pairings based on given palettes and data.
	 *
	 * @param palettes List of palettes.
	 * @param data     Model data.
	 * @return A list of generated model match pairings.
	 */
	private List<ModelMatchPairing> generatePairings(List<PaletteData> palettes, ModelData data) {
		return palettes.stream()
				.map(palette -> data.toBuilder().palette(palette.getTarget()).build())
				.map(this::buildVariants)
				.flatMap(List::stream)
				.map(model -> generate(model, List.of(new JsonPrimitive("name:" + model.getPalette()))))
				.toList();
	}

	/**
	 * Creates model match pairings based on given palettes and data.
	 *
	 * @param palettes List of palettes.
	 * @param data     Model data.
	 * @return A list of created model match pairings.
	 */
	private List<ModelMatchPairing> createPairings(List<PaletteData> palettes, ModelData data) {
		return palettes.stream()
				.map(palette -> data.toBuilder().palette(palette.getTarget()).build())
				.map(this::buildVariants)
				.flatMap(List::stream)
				.map(model -> generate(model, List.of(new JsonPrimitive("name:" + model.getPalette()))))
				.toList();
	}

	/**
	 * Constructs variants for the provided model data.
	 *
	 * @param data Model data.
	 * @return A list of variant model data.
	 */
	private List<ModelData> buildVariants(ModelData data) {
		return Stream.concat(
						data.getVariants().stream()
								.map(variant -> data.toBuilder()
										.template(variant.getTemplate().equals(EMPTY_IDENTIFIER) ? data.getTemplate() : variant.getTemplate())
										.palette(variant.getPalette().equals(EMPTY_IDENTIFIER) ? data.getPalette() : variant.getPalette())
										.predicate(variant.getTarget())
										.offset(variant.getOffset())
										.resolution(variant.getResolution())
										.order(data.order())
										.build()),
						Stream.of(data))
				.collect(Collectors.toList());
	}

	/**
	 * Generates a model match pairing from palette data, model data, and additional predicates.
	 *
	 * @param data                 Model data.
	 * @param additionalPredicates Additional predicates for matching.
	 * @return A model match pairing.
	 */
	private ModelMatchPairing generate(ModelData data, List<JsonElement> additionalPredicates) {
		var model = new PaletteTemplateModel(data.getPalette(), data.getTemplate(), data.order(), Offset.of(data.getOffset()), data.getResolution(), data.displayOverrides().orElse(null));
		textures.put(model.identifier(), model);
		List<JsonElement> predicates = new ArrayList<>(data.getPredicates());
		predicates.addAll(additionalPredicates);
		return new ModelMatchPairing(PredicateMatcher.of(predicates, new PredicateFactory()), model);
	}
}
