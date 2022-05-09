package com.sigmundgranaas.forgero.registry;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.ItemCollection;
import com.sigmundgranaas.forgero.item.ToolPartItem;
import com.sigmundgranaas.forgero.item.items.tool.ForgeroPickaxeItem;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public class ItemRegistryImpl implements ItemRegistry {
    public static final TagKey<Item> HANDLE_SCHEMATICS = TagKey.of(Registry.ITEM_KEY, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "handle_schematics"));
    public static final TagKey<Item> BINDING_SCHEMATICS = TagKey.of(Registry.ITEM_KEY, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "binding_schematics"));
    public static final TagKey<Item> HEAD_SCHEMATICS = TagKey.of(Registry.ITEM_KEY, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "head_schematics"));
    public static final TagKey<Item> PICKAXEHEAD_SCHEMATICS = TagKey.of(Registry.ITEM_KEY, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "pickaxehead_schematics"));
    public static final TagKey<Item> SHOVELHEAD_SCHEMATICS = TagKey.of(Registry.ITEM_KEY, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "shovelhead_schematics"));
    public static final TagKey<Item> AXEHEAD_SCHEMATICS = TagKey.of(Registry.ITEM_KEY, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "axehead_schematics"));
    public static final TagKey<Item> HANDLES = TagKey.of(Registry.ITEM_KEY, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "handles"));
    public static final TagKey<Item> BINDINGS = TagKey.of(Registry.ITEM_KEY, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "bindings"));
    public static final TagKey<Item> PICKAXES = TagKey.of(Registry.ITEM_KEY, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "pickaxes"));
    public static final TagKey<Item> SHOVELS = TagKey.of(Registry.ITEM_KEY, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "shovels"));
    public static final TagKey<Item> AXES = TagKey.of(Registry.ITEM_KEY, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "axes"));
    public static final TagKey<Item> HEADS = TagKey.of(Registry.ITEM_KEY, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "heads"));
    public static final TagKey<Item> GEMS = TagKey.of(Registry.ITEM_KEY, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "gems"));

    public static final TagKey<Block> VEIN_MINING_ORES = TagKey.of(Registry.BLOCK_KEY, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "vein_mining_ores"));
    public static final TagKey<Block> VEIN_MINING_SAND = TagKey.of(Registry.BLOCK_KEY, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "vein_mining_sand"));
    public static final TagKey<Block> PLANTS = TagKey.of(Registry.BLOCK_KEY, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "plants"));


    private static ItemRegistry INSTANCE;
    private final ItemCollection collection;

    public ItemRegistryImpl(ItemCollection collection) {
        this.collection = collection;
    }

    public static ItemRegistry getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ItemRegistryImpl(ItemCollection.INSTANCE);
        }
        return INSTANCE;
    }

    @Override
    public void registerToolPart(Item part) {
        Registry.register(Registry.ITEM, ((ToolPartItem) part).getIdentifier(), part);
    }

    @Override
    public void registerTool(Item tool) {
        Registry.register(Registry.ITEM, ((ForgeroToolItem) tool).getIdentifier(), tool);
    }


    @Override
    public void registerTools() {
        collection.INSTANCE.getTools().forEach(this::registerTool);
        List<Item> pickaxes = collection.INSTANCE.getTools().stream().filter(item -> item instanceof ForgeroPickaxeItem).collect(Collectors.toList());
        //ToolManagerImpl.tag(FabricToolTags.PICKAXES).register(new ModdedToolsVanillaBlocksToolHandler(pickaxes));

    }

    @Override
    public void registerToolParts() {
        collection.INSTANCE.getToolParts().stream().sorted(Comparator.comparingInt(toolpart -> ((ToolPartItem) toolpart).getPart().getPrimaryMaterial().getRarity())).forEach(this::registerToolPart);
    }

    @Override
    public void registerSchematics() {
        collection.INSTANCE.getSchematics().forEach(schematicItem -> {
            Registry.register(Registry.ITEM, new Identifier(ForgeroInitializer.MOD_NAMESPACE, schematicItem.getSchematic().getSchematicIdentifier()), schematicItem);
        });
    }

    @Override
    public void registerGems() {
        collection.INSTANCE.getGems().forEach(gemItem -> {
            Registry.register(Registry.ITEM, new Identifier(ForgeroInitializer.MOD_NAMESPACE, gemItem.getGem().getIdentifier()), gemItem);
        });
    }
}
