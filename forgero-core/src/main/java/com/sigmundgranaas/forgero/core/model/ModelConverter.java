package com.sigmundgranaas.forgero.core.model;

import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.ModelData;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.ModelEntryData;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.PaletteData;
import com.sigmundgranaas.forgero.core.texture.utils.Offset;
import com.sigmundgranaas.forgero.core.type.TypeTree;

public class ModelConverter {
	private final TypeTree tree;
	private final HashMap<String, ModelMatcher> models;

	private final HashMap<String, ArrayList<ModelData>> delayedModels;

	private final HashMap<String, ModelData> generationModels;
	private final Map<String, PaletteTemplateModel> textures;

	public ModelConverter(TypeTree tree, HashMap<String, ModelMatcher> models, Map<String, PaletteTemplateModel> textures, HashMap<String, ArrayList<ModelData>> delayedModels, HashMap<String, ModelData> generationModels) {
		this.tree = tree;
		this.delayedModels = delayedModels;
		this.generationModels = generationModels;
		this.models = models;
		this.textures = textures;

	}

	public static boolean notEmpty(String test) {
		return !test.equals(EMPTY_IDENTIFIER);
	}

	public void register(DataResource resource) {
		resource.models().forEach(data -> register(data, resource.type()));
	}

	public void register(ModelData data, String type) {
		if (data.getTemplate().contains(".png")) {
			//textures.add(data.getTemplate());
		}
		if (data.getName().equals(EMPTY_IDENTIFIER)) {
			ModelMatcher model;
			var match = new ModelMatch(data.getTarget(), "");
			if (data.getModelType().equals("BASED_COMPOSITE")) {
				model = new ModelMatchPairing(match, new TemplatedModelEntry(data.getTemplate()));
			} else if (data.getModelType().equals("COMPOSITE")) {
				model = new ModelMatchPairing(match, new CompositeModelEntry());
			} else if (data.getModelType().equals("UPGRADE")) {
				model = new ModelMatchPairing(new ModelMatch(data.getTarget(), "UPGRADE"), new TemplatedModelEntry(data.getTemplate()));
			} else {
				model = ModelMatcher.EMPTY;
			}
			tree.find(type).ifPresent(node -> node.addResource(model, ModelMatcher.class));
		} else if (notEmpty(data.getName())) {
			if (data.getTemplate().equals(EMPTY_IDENTIFIER)) {
				if (this.generationModels.containsKey(data.getName())) {
					var modeldata = this.generationModels.get(data.getName()).toBuilder().variants(data.getVariants()).build();
					var pairings = pairings(modeldata);
					var model = new MatchedModelEntry(pairings, data.getName());
					if (this.models.containsKey(data.getName())) {
						var existingMatcher = this.models.get(data.getName());
						if (existingMatcher instanceof MatchedModelEntry entry) {
							entry.add(pairings);
						}
					} else {
						this.models.put(data.getName(), model);
					}
				} else {
					if (this.delayedModels.containsKey(data.getName())) {
						this.delayedModels.get(data.getName()).add(data);
					} else {
						this.delayedModels.put(data.getName(), new ArrayList<>(List.of(data)));
					}
				}
			} else {
				this.generationModels.put(data.getName(), data);
				var variants = ImmutableList.<ModelEntryData>builder().addAll(data.getVariants());
				if (this.delayedModels.containsKey(data.getName())) {
					variants.addAll(this.delayedModels.get(data.getName()).stream().map(ModelData::getVariants).flatMap(List::stream).toList());
					delayedModels.remove(data.getName());
				}
				var modelData = data.toBuilder().variants(variants.build()).build();
				var pairings = pairings(modelData);
				var model = new MatchedModelEntry(pairings, data.getName());
				if (this.models.containsKey(data.getName())) {
					var existingMatcher = this.models.get(data.getName());
					if (existingMatcher instanceof MatchedModelEntry entry) {
						entry.add(pairings);
					}
				} else {
					this.models.put(data.getName(), model);
				}
			}
		}
	}

	private List<ModelMatchPairing> pairings(ModelData data) {
		if (!data.getModelType().equals("GENERATE")) {
			return List.of(generate(PaletteData.builder().name(data.getPalette()).build(), data.getTemplate(), data.order(), new ArrayList<>(), Offset.of(data.getOffset()), data.getResolution(), data.displayOverrides().orElse(null), data.getSecondaryTextures()));
		} else {
			List<PaletteData> palettes = tree.find(data.getPalette()).map(node -> node.getResources(PaletteData.class)).orElse(ImmutableList.<PaletteData>builder().build());
			var variants = data.getVariants().stream()
					.map(variant -> data.toBuilder()
							.template(variant.getTemplate().equals(EMPTY_IDENTIFIER) ? data.getTemplate() : variant.getTemplate())
							.target(variant.getTarget())
							.offset(variant.getOffset())
							.resolution(variant.getResolution())
							.order(data.order())
							.build())
					.collect(Collectors.toList());
			variants.add(data);
			return palettes.stream()
					.map(palette -> variants.stream()
							.map(entry -> generate(palette, entry.getTemplate(), entry.order(), new ArrayList<>(entry.getTarget()), Offset.of(entry.getOffset()), entry.getResolution(), data.displayOverrides().orElse(null), data.getSecondaryTextures()))
							.toList())
					.flatMap(List::stream)
					.toList();
		}
	}

	private ModelMatchPairing generate(PaletteData palette, String template, int order, List<String> criteria, Offset offset, Integer resolution, @Nullable JsonObject displayOverrides, List<ModelEntryData> secondaryTextures) {
		var model = new PaletteTemplateModel(palette.getTarget(), template, order, offset, resolution, displayOverrides, secondaryTextures);
		textures.put(model.identifier(), model);
		criteria.add(model.palette());
		return new ModelMatchPairing(new ModelMatch(criteria, ""), model);
	}
}
