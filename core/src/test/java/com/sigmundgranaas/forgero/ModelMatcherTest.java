package com.sigmundgranaas.forgero;

import com.sigmundgranaas.forgero.model.ModelRegistry;
import com.sigmundgranaas.forgero.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.resource.data.v2.data.ModelData;
import com.sigmundgranaas.forgero.resource.data.v2.data.PaletteData;
import com.sigmundgranaas.forgero.type.TypeTree;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.sigmundgranaas.forgero.TypeTreeTest.createDataList;
import static com.sigmundgranaas.forgero.testutil.ToolParts.HANDLE;
import static com.sigmundgranaas.forgero.testutil.Tools.IRON_PICKAXE;

public class ModelMatcherTest {
    public static TypeTree loadedTypeTree() {
        var tree = new TypeTree();
        createDataList().forEach(tree::addNode);
        tree.resolve();
        return tree;
    }

    @Test
    void getProperModel() {
        var tree = loadedTypeTree();
        var pickaxe = IRON_PICKAXE;
        var pickaxeModel = ModelData.builder().target(List.of("PICKAXE")).modelType("COMPOSITE").build();
        var registry = new ModelRegistry(tree);

        var model = registry.find(pickaxe);
        Assertions.assertTrue(model.isPresent());
    }

    @Test
    void getHandleModel() {
        var tree = loadedTypeTree();
        var oakPalette = PaletteData.builder().name("oak").build();
        var ironPalette = PaletteData.builder().name("iron").build();
        tree.find("MATERIAL").ifPresent(node -> node.addResource(oakPalette, PaletteData.class));
        tree.find("MATERIAL").ifPresent(node -> node.addResource(ironPalette, PaletteData.class));
        var handle = HANDLE;
        var handleModelData = ModelData.builder().target(List.of("type:MATERIAL", "type:HANDLE_SCHEMATIC")).modelType("BASED_COMPOSITE").template("default_handle").build();
        var handleVariantData = ModelData.builder().name("default_handle").modelType("GENERATE").palette("MATERIAL").template("handle.png").build();
        var registry = new ModelRegistry(tree);
        registry.register(DataResource.builder().type("HANDLE").models(List.of(handleModelData, handleVariantData)).build());
        var model = registry.find(handle);
        Assertions.assertTrue(model.isPresent());
    }
}
