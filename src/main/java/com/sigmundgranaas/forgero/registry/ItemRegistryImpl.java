package com.sigmundgranaas.forgero.registry;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.ItemCollection;
import com.sigmundgranaas.forgero.item.ToolPartItem;
import com.sigmundgranaas.forgero.item.items.tool.ForgeroPickaxeItem;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public class ItemRegistryImpl implements ItemRegistry {
    public static final TagKey<Item> HANDLE_PATTERNS = TagKey.of(Registry.ITEM_KEY, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "handle_patterns"));
    public static final TagKey<Item> BINDING_PATTERNS = TagKey.of(Registry.ITEM_KEY, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "binding_patterns"));
    public static final TagKey<Item> PICKAXEHEAD_PATTERNS = TagKey.of(Registry.ITEM_KEY, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "pickaxehead_patterns"));
    public static final TagKey<Item> SHOVELHEAD_PATTERNS = TagKey.of(Registry.ITEM_KEY, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "shovelhead_patterns"));
    public static final TagKey<Item> AXEHEAD_PATTERNS = TagKey.of(Registry.ITEM_KEY, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "axehead_patterns"));
    public static final TagKey<Item> HANDLES = TagKey.of(Registry.ITEM_KEY, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "handles"));

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
    public void registerPatterns() {
        collection.INSTANCE.INSTANCE.getPatterns().forEach(pattern -> {
            Registry.register(Registry.ITEM, new Identifier(ForgeroInitializer.MOD_NAMESPACE, pattern.getPattern().getPatternIdentifier()), pattern);
        });
    }
}
