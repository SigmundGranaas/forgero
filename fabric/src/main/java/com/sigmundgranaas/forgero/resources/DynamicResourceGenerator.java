package com.sigmundgranaas.forgero.resources;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.ForgeroRegistry;
import com.sigmundgranaas.forgero.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.material.material.MaterialType;
import com.sigmundgranaas.forgero.resources.external.Patchouli;
import com.sigmundgranaas.forgero.schematic.HeadSchematic;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.texture.CachedToolPartTextureService;
import com.sigmundgranaas.forgero.tool.ForgeroTool;
import com.sigmundgranaas.forgero.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.type.MutableTypeNode;
import net.devtech.arrp.api.RRPCallback;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.tags.JTag;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Arrays;

import static com.sigmundgranaas.forgero.identifier.Common.ELEMENT_SEPARATOR;


public class DynamicResourceGenerator {
    public static final RuntimeResourcePack RESOURCE_PACK = RuntimeResourcePack.create("forgero:dynamic");

    public void generateResources() {
        generateTags();
        generateTagsFromStateTree();
        RRPCallback.BEFORE_VANILLA.register(a -> a.add(RESOURCE_PACK));

        if (FabricLoader.getInstance().isModLoaded("patchouli")) {
            new Patchouli().registerResources(RESOURCE_PACK);
        }

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public void reload(ResourceManager manager) {
                ForgeroInitializer.LOGGER.info("Clearing Forgero texture caches");
                CachedToolPartTextureService.getInstance(null).clearCache();
            }

            @Override
            public Identifier getFabricId() {
                return new Identifier("forgero", "resources");
            }
        });
    }

    public void generateTags() {
        //RESOURCE_PACK.addTag(new Identifier(ForgeroInitializer.MOD_NAMESPACE, "blocks/vein_mining_sand"), new JTag().add(new Identifier("minecraft:sand")));
        addToolPartHeadTags(ForgeroToolTypes.PICKAXE, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/pickaxehead_schematics"), new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/pickaxeheads"));
        addToolPartHeadTags(ForgeroToolTypes.AXE, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/axehead_schematics"), new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/axeheads"));
        addToolPartHeadTags(ForgeroToolTypes.SHOVEL, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/shovelhead_schematics"), new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/shovelheads"));
        addToolPartHeadTags(ForgeroToolTypes.HOE, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/hoehead_schematics"), new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/hoeheads"));
        addToolPartHeadTags(ForgeroToolTypes.SWORD, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/swordhead_schematics"), new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/swordheads"));

        createCommonToolVariantTags(ForgeroRegistry.TOOL.list());

        for (ForgeroToolTypes type : ForgeroToolTypes.values()) {
            JTag toolTag = new JTag();
            ForgeroRegistry.TOOL.list()
                    .stream()
                    .filter(tool -> tool.getToolType() == type)
                    .map(tool -> new Identifier(ForgeroInitializer.MOD_NAMESPACE, tool.getToolIdentifierString()))
                    .forEach(toolTag::add);

            RESOURCE_PACK.addTag(new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/" + type.getToolName().toLowerCase() + "s"), toolTag);
        }


        addToolPartTags(ForgeroToolPartTypes.HANDLE, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/handle_schematics"), new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/handles"));
        addToolPartTags(ForgeroToolPartTypes.HEAD, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/head_schematics"), new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/heads"));
        addToolPartTags(ForgeroToolPartTypes.BINDING, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/binding_schematics"), new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/bindings"));
        addGemTags();
    }

    public void generateTagsFromStateTree() {
        ForgeroStateRegistry.TREE.nodes().forEach(this::createTagFromType);
    }

    private void createTagFromType(MutableTypeNode node) {
        JTag typeTag = new JTag();
        var states = node.getResources(State.class);
        if (states.size() > 0) {
            states.stream()
                    .filter(state -> ForgeroStateRegistry.STATE_TO_CONTAINER.containsKey(state.identifier()))
                    .map(state -> new Identifier(ForgeroStateRegistry.STATE_TO_CONTAINER.get(state.identifier())))
                    .forEach(typeTag::add);
            RESOURCE_PACK.addTag(new Identifier("forgero", "items/" + node.name().toLowerCase()), typeTag);
        }
    }


    private void createCommonToolVariantTags(ImmutableList<ForgeroTool> list) {
        Arrays.stream(ForgeroToolTypes.values()).forEach(type -> {
            JTag woodTag = new JTag();
            ForgeroRegistry.MATERIAL.getPrimaryMaterials().forEach(material -> {
                JTag toolTag = new JTag();
                if (material.getType() == MaterialType.WOOD) {
                    woodTag.add(new Identifier(ForgeroInitializer.MOD_NAMESPACE, material.getResourceName() + ELEMENT_SEPARATOR + type.getToolName()));
                } else {
                    toolTag.add(new Identifier(ForgeroInitializer.MOD_NAMESPACE, material.getResourceName() + ELEMENT_SEPARATOR + type.getToolName()));
                    if (Registry.ITEM.containsId(new Identifier("minecraft", material.getResourceName() + "_" + type.getToolName()))) {
                        toolTag.add(new Identifier("minecraft", material.getResourceName() + "_" + type.getToolName()));
                    }
                    RESOURCE_PACK.addTag(new Identifier("c", "items/" + material.getResourceName() + "_" + type.getToolName()), toolTag);
                }
            });
            woodTag.add(new Identifier("minecraft", "wooden" + "_" + type.getToolName()));
            RESOURCE_PACK.addTag(new Identifier("c", "items/" + "wooden" + "_" + type.getToolName()), woodTag);
        });
    }


    private void addToolPartHeadTags(ForgeroToolTypes type, Identifier patternsTagId, Identifier headTagId) {
        JTag headTags = new JTag();
        ForgeroRegistry.TOOL_PART
                .getHeads()
                .stream()
                .filter(part -> part.getToolType() == type)
                .map(head -> new Identifier(ForgeroInitializer.MOD_NAMESPACE, head.getToolPartIdentifier()))
                .forEach(headTags::add);
        RESOURCE_PACK.addTag(headTagId, headTags);

        JTag headSchematicTag = new JTag();
        ForgeroRegistry.SCHEMATIC.list()
                .stream()
                .filter(pattern -> pattern.getType() == ForgeroToolPartTypes.HEAD)
                .map(HeadSchematic.class::cast)
                .filter(pattern -> pattern.getToolType() == type)
                .map(pattern -> new Identifier(ForgeroInitializer.MOD_NAMESPACE, pattern.getSchematicIdentifier()))
                .forEach(headSchematicTag::add);
        RESOURCE_PACK.addTag(patternsTagId, headSchematicTag);
    }

    private void addGemTags() {
        Identifier gemTagId = new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/gems");
        JTag gems = new JTag();
        ForgeroRegistry.GEM.list()
                .stream()
                .map(gem -> new Identifier(ForgeroInitializer.MOD_NAMESPACE, gem.getStringIdentifier()))
                .forEach(gems::add);
        RESOURCE_PACK.addTag(gemTagId, gems);
    }

    private void addToolPartTags(ForgeroToolPartTypes type, Identifier patternsTagId, Identifier toolPartTagID) {
        JTag toolparts = new JTag();

        ForgeroRegistry.TOOL_PART
                .list()
                .stream()
                .filter(toolpart -> toolpart.getToolPartType() == type)
                .map(toolpart -> new Identifier(ForgeroInitializer.MOD_NAMESPACE, toolpart.getToolPartIdentifier()))
                .forEach(toolparts::add);
        RESOURCE_PACK.addTag(toolPartTagID, toolparts);

        JTag toolPartSchematics = new JTag();
        ForgeroRegistry.SCHEMATIC.list().stream()
                .filter(pattern -> pattern.getType() == type)
                .map(pattern -> new Identifier(ForgeroInitializer.MOD_NAMESPACE, pattern.getSchematicIdentifier()))
                .forEach(toolPartSchematics::add);
        RESOURCE_PACK.addTag(patternsTagId, toolPartSchematics);
    }

}
