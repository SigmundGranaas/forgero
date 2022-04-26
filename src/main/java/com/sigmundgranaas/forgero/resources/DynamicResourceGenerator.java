package com.sigmundgranaas.forgero.resources;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.pattern.HeadPattern;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import net.devtech.arrp.api.RRPCallback;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.tags.JTag;
import net.minecraft.util.Identifier;


public class DynamicResourceGenerator {
    public static final RuntimeResourcePack RESOURCE_PACK = RuntimeResourcePack.create("forgero:builtin");

    public void generateResources() {
        generateTags();
        RRPCallback.BEFORE_VANILLA.register(a -> a.add(RESOURCE_PACK));
    }

    public void generateTags() {
        //RESOURCE_PACK.addTag(new Identifier(ForgeroInitializer.MOD_NAMESPACE, "blocks/vein_mining_sand"), new JTag().add(new Identifier("minecraft:sand")));
        addToolPartHeadTags(ForgeroToolTypes.PICKAXE, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/pickaxehead_patterns"), new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/pickaxeheads"));
        addToolPartHeadTags(ForgeroToolTypes.AXE, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/axehead_patterns"), new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/axeheads"));
        addToolPartHeadTags(ForgeroToolTypes.SHOVEL, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/shovel_patterns"), new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/shovels"));


        addToolPartTags(ForgeroToolPartTypes.HANDLE, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/handle_patterns"), new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/handles"));
        addToolPartTags(ForgeroToolPartTypes.BINDING, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/binding_patterns"), new Identifier(ForgeroInitializer.MOD_NAMESPACE, "items/bindings"));

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

        JTag pickaxeheadsPatterns = new JTag();
        ForgeroRegistry
                .getInstance()
                .patternCollection()
                .getPatterns()
                .stream()
                .filter(pattern -> pattern.getType() == ForgeroToolPartTypes.HEAD)
                .map(HeadPattern.class::cast)
                .filter(pattern -> pattern.getToolType() == type)
                .map(pattern -> new Identifier(ForgeroInitializer.MOD_NAMESPACE, pattern.getPatternIdentifier()))
                .forEach(pickaxeheadsPatterns::add);
        RESOURCE_PACK.addTag(patternsTagId, pickaxeheadsPatterns);
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

        JTag toolPartPatterns = new JTag();
        ForgeroRegistry
                .getInstance()
                .patternCollection()
                .getPatterns()
                .stream()
                .filter(pattern -> pattern.getType() == type)
                .map(pattern -> new Identifier(ForgeroInitializer.MOD_NAMESPACE, pattern.getPatternIdentifier()))
                .forEach(toolPartPatterns::add);
        RESOURCE_PACK.addTag(patternsTagId, toolPartPatterns);
    }

}
