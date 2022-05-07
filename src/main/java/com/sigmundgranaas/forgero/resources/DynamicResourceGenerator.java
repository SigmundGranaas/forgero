package com.sigmundgranaas.forgero.resources;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.schematic.HeadSchematic;
import com.sigmundgranaas.forgero.core.texture.CachedToolPartTextureService;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import net.devtech.arrp.api.RRPCallback;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.tags.JTag;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;


public class DynamicResourceGenerator {
    public static final RuntimeResourcePack RESOURCE_PACK = RuntimeResourcePack.create("forgero:builtin");

    public void generateResources() {
        generateTags();
        RRPCallback.BEFORE_VANILLA.register(a -> a.add(RESOURCE_PACK));


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
        addToolPartHeadTags(ForgeroToolTypes.SHOVEL, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/shovel_schematics"), new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/shovels"));


        addToolPartTags(ForgeroToolPartTypes.HANDLE, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/handle_schematics"), new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/handles"));
        addToolPartTags(ForgeroToolPartTypes.BINDING, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/binding_schematics"), new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/bindings"));

    }


    private void addToolPartHeadTags(ForgeroToolTypes type, Identifier patternsTagId, Identifier headTagId) {
        JTag pickaxeheads = new JTag();
        ForgeroRegistry
                .getInstance()
                .toolPartCollection()
                .getHeads()
                .stream()
                .filter(part -> part.getToolType() == ForgeroToolTypes.PICKAXE)
                .map(head -> new Identifier(ForgeroInitializer.MOD_NAMESPACE, head.getToolPartIdentifier()))
                .forEach(pickaxeheads::add);
        RESOURCE_PACK.addTag(headTagId, pickaxeheads);

        JTag pickaxeheadsSchematics = new JTag();
        ForgeroRegistry
                .getInstance()
                .schematicCollection()
                .getSchematics()
                .stream()
                .filter(pattern -> pattern.getType() == ForgeroToolPartTypes.HEAD)
                .map(HeadSchematic.class::cast)
                .filter(pattern -> pattern.getToolType() == type)
                .map(pattern -> new Identifier(ForgeroInitializer.MOD_NAMESPACE, pattern.getSchematicIdentifier()))
                .forEach(pickaxeheadsSchematics::add);
        RESOURCE_PACK.addTag(patternsTagId, pickaxeheadsSchematics);
    }

    private void addToolPartTags(ForgeroToolPartTypes type, Identifier patternsTagId, Identifier toolPartTagID) {
        JTag toolparts = new JTag();

        ForgeroRegistry
                .getInstance()
                .toolPartCollection()
                .getToolParts()
                .stream()
                .filter(toolpart -> toolpart.getToolPartType() == type)
                .map(toolpart -> new Identifier(ForgeroInitializer.MOD_NAMESPACE, toolpart.getToolPartIdentifier()))
                .forEach(toolparts::add);
        RESOURCE_PACK.addTag(toolPartTagID, toolparts);

        JTag toolPartSchematics = new JTag();
        ForgeroRegistry
                .getInstance()
                .schematicCollection()
                .getSchematics()
                .stream()
                .filter(pattern -> pattern.getType() == type)
                .map(pattern -> new Identifier(ForgeroInitializer.MOD_NAMESPACE, pattern.getSchematicIdentifier()))
                .forEach(toolPartSchematics::add);
        RESOURCE_PACK.addTag(patternsTagId, toolPartSchematics);
    }

}
