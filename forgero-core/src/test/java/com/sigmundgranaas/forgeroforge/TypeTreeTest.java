package com.sigmundgranaas.forgeroforge;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.resource.data.v2.ResourceLocator;
import com.sigmundgranaas.forgero.resource.data.v2.ResourcePool;
import com.sigmundgranaas.forgero.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.resource.data.v2.data.TypeData;
import com.sigmundgranaas.forgero.resource.data.v2.factory.TypeFactory;
import com.sigmundgranaas.forgero.resource.data.v2.loading.DefaultMapper;
import com.sigmundgranaas.forgero.resource.data.v2.loading.FileResourceLoader;
import com.sigmundgranaas.forgero.resource.data.v2.loading.JsonContentFilter;
import com.sigmundgranaas.forgero.resource.data.v2.loading.PathWalker;
import com.sigmundgranaas.forgero.type.TypeTree;
import com.sigmundgranaas.forgero.util.loader.PathFinder;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.sigmundgranaas.forgero.resource.data.Constant.CORE_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypeTreeTest {

    public static ResourceLocator WALKER = PathWalker.builder()
            .pathFinder(PathFinder::ClassLoaderFinder)
            .contentFilter(new JsonContentFilter())
            .depth(20)
            .build();

    public static List<TypeData> createDataList() {
        var schematic = new TypeData("SCHEMATIC", Optional.empty(), Collections.emptyList());
        var pickaxeSchematic = new TypeData("PICKAXE_SCHEMATIC", Optional.of(schematic.name()), Collections.emptyList());
        var shovelSchematic = new TypeData("SHOVEL_SCHEMATIC", Optional.of(schematic.name()), Collections.emptyList());
        var swordSchematic = new TypeData("SWORD_SCHEMATIC", Optional.of(schematic.name()), Collections.emptyList());
        var saberSchematic = new TypeData("SABER_SCHEMATIC", Optional.of(swordSchematic.name()), Collections.emptyList());
        var part = new TypeData("PART", Optional.empty(), Collections.emptyList());
        var toolpart = new TypeData("TOOL_PART", Optional.of(part.name()), Collections.emptyList());
        var handle = new TypeData("HANDLE", Optional.of(toolpart.name()), Collections.emptyList());
        var material = new TypeData("MATERIAL", Optional.empty(), Collections.emptyList());
        var wood = new TypeData("WOOD", Optional.of(material.name()), Collections.emptyList());
        var oak = new TypeData("OAK", Optional.of("WOOD"), Collections.emptyList());
        return List.of(wood, pickaxeSchematic, shovelSchematic, swordSchematic, saberSchematic, material, schematic, oak, part, toolpart, handle);
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
        FileResourceLoader loader = new FileResourceLoader(CORE_PATH, WALKER, new DefaultMapper());
        var tree = new TypeTree();
        new TypeFactory().convertJsonToData(loader.load()).forEach(tree::addNode);
        tree.resolve();

        var testNodeSchematic = tree.find("LONG_BOW");
        assertTrue(testNodeSchematic.isPresent());
        assertEquals("BOW", testNodeSchematic.get().parent().orElseThrow().name());
    }

    @Test
    void loadedTypeTree() throws URISyntaxException {
        FileResourceLoader loader = new FileResourceLoader(CORE_PATH, WALKER, new DefaultMapper());
        ResourcePool pool = new ResourcePool(ImmutableList.<DataResource>builder().addAll(loader.load()).build());
        var tree = pool.createLoadedTypeTree();
        var pick = tree.find("PICKAXE").orElseThrow();
        var tool = tree.find("TOOL").orElseThrow();
        var holdAble = tree.find("HOLDABLE").orElseThrow();
        //Assertions.assertTrue(pick.resources().stream().anyMatch(element -> element.equals("pickaxe")));
        //Assertions.assertTrue(tool.resources().stream().anyMatch(element -> element.equals("pickaxe")));
        //Assertions.assertTrue(holdAble.resources().stream().anyMatch(element -> element.equals("pickaxe")));
    }
}