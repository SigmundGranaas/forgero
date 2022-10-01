package com.sigmundgranaas.forgero.model;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.resource.data.v1.pojo.PalettePojo;
import com.sigmundgranaas.forgero.resource.data.v2.data.ModelData;
import com.sigmundgranaas.forgero.type.TypeTree;

import java.util.HashMap;
import java.util.List;

import static com.sigmundgranaas.forgero.util.Identifiers.EMPTY_IDENTIFIER;

public class ModelConverter {
    private TypeTree tree;
    private HashMap<String, String> textures;
    private HashMap<String, ModelAble> models;

    public void register(ModelData data, String type) {

        if (notEmpty(data.getName()) && data.getModelType().equals("GENERATE")) {
            List<PalettePojo> palettes = tree.find(data.getPalette()).map(node -> node.getResources(PalettePojo.class)).orElse(ImmutableList.<PalettePojo>builder().build());
            var originalPalettes = palettes.stream().map(palette -> generate(palette, data.getName())).toList();

            var variantPalettes = data.getVariants().stream()
                    .map(variant -> palettes.stream()
                            .map(palette -> generate(palette, data.getName()))
                            .toList())
                    .flatMap(List::stream)
                    .toList();
        }
    }

    private TextureBasedModel generate(PalettePojo palette, String template) {
        return new TextureBasedModel(String.format("%s-%s", palette.name, template), 1);
    }


    public static boolean notEmpty(String test) {
        return test.equals(EMPTY_IDENTIFIER);
    }
}
