package com.sigmundgranaas.forgero.model;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.resource.data.v2.data.ModelData;
import com.sigmundgranaas.forgero.resource.data.v2.data.PaletteData;
import com.sigmundgranaas.forgero.type.TypeTree;

import java.util.HashMap;
import java.util.List;

import static com.sigmundgranaas.forgero.util.Identifiers.EMPTY_IDENTIFIER;

public class ModelConverter {
    private final TypeTree tree;
    private final HashMap<String, ModelMatcher> models;

    public ModelConverter(TypeTree tree, HashMap<String, ModelMatcher> models) {
        this.tree = tree;
        this.models = models;
    }

    public static boolean notEmpty(String test) {
        return !test.equals(EMPTY_IDENTIFIER);
    }

    public void register(DataResource resource) {
        resource.models().forEach(data -> register(data, resource.type()));
    }

    public void register(ModelData data, String type) {
        if (data.getName().equals(EMPTY_IDENTIFIER)) {
            ModelMatcher model;
            var match = new ModelMatch(data.getTarget(), "");
            model = new ModelMatchPairing(match, new TemplatedModelEntry(data.getTemplate()));
            tree.find(type).ifPresent(node -> node.addResource(model, ModelMatcher.class));
        } else if (notEmpty(data.getName()) && data.getModelType().equals("GENERATE")) {
            List<PaletteData> palettes = tree.find(data.getPalette()).map(node -> node.getResources(PaletteData.class)).orElse(ImmutableList.<PaletteData>builder().build());
            var models = palettes.stream().map(palette -> generate(palette, data.getTemplate())).toList();
            var model = new MatchedModelEntry(models, data.getName());
            this.models.put(data.getName(), model);
        }
    }

    private ModelMatchPairing generate(PaletteData palette, String template) {
        var model = new PaletteTemplateModel(palette.getName(), template);
        return new ModelMatchPairing(new ModelMatch(List.of(palette.getName()), ""), model);
    }

}
