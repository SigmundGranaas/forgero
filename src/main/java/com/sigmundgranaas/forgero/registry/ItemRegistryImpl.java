package com.sigmundgranaas.forgero.registry;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.ItemCollection;
import com.sigmundgranaas.forgero.item.ToolPartItem;
import com.sigmundgranaas.forgero.item.items.tool.ForgeroPickaxeItem;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public class ItemRegistryImpl implements ItemRegistry {
    public static final Tag<Item> HANDLE_PATTERNS = TagFactory.ITEM.create(new Identifier(ForgeroInitializer.MOD_NAMESPACE, "handle_patterns"));
    public static final Tag<Item> BINDING_PATTERNS = TagFactory.ITEM.create(new Identifier(ForgeroInitializer.MOD_NAMESPACE, "binding_patterns"));
    public static final Tag<Item> PICKAXEHEAD_PATTERNS = TagFactory.ITEM.create(new Identifier(ForgeroInitializer.MOD_NAMESPACE, "pickaxehead_patterns"));
    public static final Tag<Item> SHOVELHEAD_PATTERNS = TagFactory.ITEM.create(new Identifier(ForgeroInitializer.MOD_NAMESPACE, "shovelhead_patterns"));
    public static final Tag<Item> AXEHEAD_PATTERNS = TagFactory.ITEM.create(new Identifier(ForgeroInitializer.MOD_NAMESPACE, "axehead_patterns"));
    public static final Tag<Item> HANDLES = TagFactory.ITEM.create(new Identifier(ForgeroInitializer.MOD_NAMESPACE, "handles"));

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
        collection.INSTANCE.getToolParts().forEach(this::registerToolPart);
    }

    @Override
    public void registerPatterns() {
        Tag<Item> handles = HANDLE_PATTERNS;
        collection.INSTANCE.INSTANCE.getPatterns().forEach(pattern -> {
            Registry.register(Registry.ITEM, new Identifier(ForgeroInitializer.MOD_NAMESPACE, pattern.getPattern().getPatternIdentifier()), pattern);
        });
    }
}
