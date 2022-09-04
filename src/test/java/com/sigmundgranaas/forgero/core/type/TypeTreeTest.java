package com.sigmundgranaas.forgero.core.type;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.data.v2.JsonPoolLoader;
import com.sigmundgranaas.forgero.core.data.v2.ResourcePool;
import com.sigmundgranaas.forgero.core.data.v2.data.TypeData;
import com.sigmundgranaas.forgero.core.data.v2.factory.TypeFactory;
import com.sigmundgranaas.forgero.core.data.v2.json.JsonResource;
import com.sigmundgranaas.forgero.core.resourceloader.JsonPathLoader;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypeTreeTest {

    public static List<TypeData> createDataList() {
        var schematic = new TypeData("SCHEMATIC", Optional.empty());
        var pickaxeSchematic = new TypeData("PICKAXE_SCHEMATIC", Optional.of(schematic.name()));
        var shovelSchematic = new TypeData("SHOVEL_SCHEMATIC", Optional.of(schematic.name()));
        var swordSchematic = new TypeData("SWORD_SCHEMATIC", Optional.of(schematic.name()));
        var saberSchematic = new TypeData("SABER_SCHEMATIC", Optional.of(swordSchematic.name()));
        var material = new TypeData("MATERIAL", Optional.empty());
        var wood = new TypeData("WOOD", Optional.of(material.name()));
        var oak = new TypeData("OAK", Optional.of("WOOD"));
        return List.of(wood, pickaxeSchematic, shovelSchematic, swordSchematic, saberSchematic, material, schematic, oak);
    }

    @Test
    void createValidTypeTree() {
        var tree = new TypeTree();
        createDataList().forEach(tree::addNode);
        tree.resolve();
        var testNodeSchematic = tree.find("SABER_SCHEMATIC");
        var testNodeOak = tree.find("OAK");
        assertTrue(testNodeSchematic.isPresent());
        assertTrue(testNodeSchematic.get().parent().isPresent());

        assertTrue(testNodeOak.isPresent());
        assertTrue(testNodeOak.get().parent().isPresent());
    }

    @Test
    void loadTypeTreeFromJson() throws URISyntaxException {
        JsonPoolLoader loader = new JsonPoolLoader(JsonPathLoader.getResourcesInFolder());
        var tree = new TypeTree();
        new TypeFactory().convertJsonToData(loader.loadPojos()).forEach(tree::addNode);
        tree.resolve();

        var testNodeSchematic = tree.find("LONG_BOW");
        assertTrue(testNodeSchematic.isPresent());
        assertEquals("BOW", testNodeSchematic.get().parent().orElseThrow().name());
    }

    @Test
    void loadedTypeTree() throws URISyntaxException {
        JsonPoolLoader loader = new JsonPoolLoader(JsonPathLoader.getResourcesInFolder());
        ResourcePool pool = new ResourcePool(ImmutableList.<JsonResource>builder().addAll(loader.loadPojos()).build());
        var tree = pool.createLoadedTypeTree();
        var pick = tree.find("PICKAXE").orElseThrow();
        var tool = tree.find("TOOL").orElseThrow();
        var holdAble = tree.find("HOLDABLE").orElseThrow();
        //Assertions.assertTrue(pick.resources().stream().anyMatch(element -> element.equals("pickaxe")));
        //Assertions.assertTrue(tool.resources().stream().anyMatch(element -> element.equals("pickaxe")));
        //Assertions.assertTrue(holdAble.resources().stream().anyMatch(element -> element.equals("pickaxe")));
    }
}