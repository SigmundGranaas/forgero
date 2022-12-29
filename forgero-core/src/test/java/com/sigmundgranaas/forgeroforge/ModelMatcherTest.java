package com.sigmundgranaas.forgeroforge;

import com.sigmundgranaas.forgero.model.CompositeModelTemplate;
import com.sigmundgranaas.forgero.model.ModelRegistry;
import com.sigmundgranaas.forgero.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.resource.data.v2.data.ModelData;
import com.sigmundgranaas.forgero.resource.data.v2.data.PaletteData;
import com.sigmundgranaas.forgero.type.TypeTree;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.sigmundgranaas.forgeroforge.TypeTreeTest.createDataList;
import static com.sigmundgranaas.forgeroforge.testutil.ToolParts.HANDLE;
import static com.sigmundgranaas.forgeroforge.testutil.Tools.IRON_PICKAXE;
import static com.sigmundgranaas.forgeroforge.testutil.Upgrades.BINDING;
import static com.sigmundgranaas.forgeroforge.testutil.Upgrades.LEATHER;

public class ModelMatcherTest {
    public static TypeTree loadedTypeTree() {
        var tree = new TypeTree();
        createDataList().forEach(tree::addNode);
        tree.resolve();
        return tree;
    }

    @Test
    void getProperModel() {
        var registry = new ModelRegistry();
        PipeLineTest.defaultResourcePipeLineTest()
                .data(registry.paletteListener())
                .data(registry.modelListener())
                .build().execute();
        var pickaxe = IRON_PICKAXE;
        var pickaxeModel = registry.find(pickaxe);
        Assertions.assertTrue(pickaxeModel.isPresent());
        Assertions.assertEquals(2, ((CompositeModelTemplate) pickaxeModel.get()).getModels().size());
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

    @Test
    void getModelFromPipeLine() {
        var registry = new ModelRegistry();
        PipeLineTest.defaultResourcePipeLineTest().data(registry.paletteListener()).data(registry.modelListener()).build().execute();
        var handle = HANDLE;
        var pickaxe = IRON_PICKAXE;
        var model = registry.find(handle);
        Assertions.assertTrue(model.isPresent());
        var pickaxeModel = registry.find(pickaxe);
        Assertions.assertTrue(pickaxeModel.isPresent());
    }

    @Test
    void getModelWithUpgrade() {
        var registry = new ModelRegistry();
        PipeLineTest.defaultResourcePipeLineTest().data(registry.paletteListener()).data(registry.modelListener()).build().execute();
        var pickaxe = IRON_PICKAXE.upgrade(BINDING);
        var pickaxeModel = registry.find(pickaxe);
        Assertions.assertTrue(pickaxeModel.isPresent());
        Assertions.assertEquals(3, ((CompositeModelTemplate) pickaxeModel.get()).getModels().size());
    }

    @Test
    void getHandleModelWithUpgrade() {
        var registry = new ModelRegistry();
        PipeLineTest.defaultResourcePipeLineTest().data(registry.paletteListener()).data(registry.modelListener()).build().execute();
        var handle = HANDLE.upgrade(LEATHER);
        var handleModel = registry.find(handle);
        Assertions.assertTrue(handleModel.isPresent());
        //Assertions.assertEquals(2, ((CompositeModelTemplate) handleModel.get()).getModels().size());
    }

}
