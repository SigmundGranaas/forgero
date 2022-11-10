package com.sigmundgranaas.forgero.model;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.resource.data.v2.data.ModelData;
import com.sigmundgranaas.forgero.resource.data.v2.data.PaletteData;
import com.sigmundgranaas.forgero.texture.utils.Offset;
import com.sigmundgranaas.forgero.type.TypeTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.sigmundgranaas.forgero.util.Identifiers.EMPTY_IDENTIFIER;

public class ModelConverter {
    private final TypeTree tree;
    private final HashMap<String, ModelMatcher> models;
    private final Map<String, PaletteTemplateModel> textures;

    public ModelConverter(TypeTree tree, HashMap<String, ModelMatcher> models, Map<String, PaletteTemplateModel> textures) {
        this.tree = tree;
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
        } else if (notEmpty(data.getName()) && data.getModelType().equals("GENERATE")) {
            List<PaletteData> palettes = tree.find(data.getPalette()).map(node -> node.getResources(PaletteData.class)).orElse(ImmutableList.<PaletteData>builder().build());
            var variants = data.getVariants().stream().map(variant -> data.toBuilder().template(variant.getTemplate().equals(EMPTY_IDENTIFIER) ? data.getTemplate() :variant.getTemplate() ).target(variant.getTarget()).offset(variant.getOffset()).order(data.order()).build()).collect(Collectors.toList());
            variants.add(data);
            var models = palettes.stream().map(palette -> variants.stream().map(entry -> generate(palette, entry.getTemplate(), entry.order(), new ArrayList<>(entry.getTarget()), Offset.of(entry.getOffset()))).toList()).flatMap(List::stream).toList();
            var model = new MatchedModelEntry(models, data.getName());
            this.models.put(data.getName(), model);
        }
    }

    private ModelMatchPairing generate(PaletteData palette, String template, int order, List<String> criteria, Offset offset) {
        var model = new PaletteTemplateModel(palette.getName(), template, order, offset);
        textures.put(model.identifier(), model);
        criteria.add(model.palette());
        return new ModelMatchPairing(new ModelMatch(criteria, ""), model);
    }

}
