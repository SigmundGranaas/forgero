package com.sigmundgranaas.forgero.registry;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.ItemCollection;
import com.sigmundgranaas.forgero.item.ItemGroups;
import com.sigmundgranaas.forgero.item.ToolPartItem;
import com.sigmundgranaas.forgero.item.items.PatternItem;
import com.sigmundgranaas.forgero.item.items.tool.ForgeroPickaxeItem;
import com.sigmundgranaas.forgero.recipe.customrecipe.RecipeTypes;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public class ItemRegistryImpl implements ItemRegistry {
    public static final Tag<Item> HANDLE_PATTERNS = TagFactory.ITEM.create(new Identifier(Forgero.MOD_NAMESPACE, "handle_patterns"));
    public static final Tag<Item> BINDING_PATTERNS = TagFactory.ITEM.create(new Identifier(Forgero.MOD_NAMESPACE, "binding_patterns"));
    public static final Tag<Item> PICKAXEHEAD_PATTERNS = TagFactory.ITEM.create(new Identifier(Forgero.MOD_NAMESPACE, "pickaxehead_patterns"));
    public static final Tag<Item> SHOVELHEAD_PATTERNS = TagFactory.ITEM.create(new Identifier(Forgero.MOD_NAMESPACE, "shovelhead_patterns"));

    private static final Item BINDING_PATTERN_DEFAULT = new PatternItem(new Item.Settings().group(ItemGroups.FORGERO_TOOL_PARTS), RecipeTypes.BINDING_RECIPE);
    private static final Item HANDLE_PATTERN_DEFAULT = new PatternItem(new Item.Settings().group(ItemGroups.FORGERO_TOOL_PARTS), RecipeTypes.HANDLE_RECIPE);
    private static final Item PICKAXEHEAD_PATTERN_DEFAULT = new PatternItem(new Item.Settings().group(ItemGroups.FORGERO_TOOL_PARTS), RecipeTypes.PICKAXEHEAD_RECIPE);
    private static final Item SHOVELHEAD_PATTERN_DEFAULT = new PatternItem(new Item.Settings().group(ItemGroups.FORGERO_TOOL_PARTS), RecipeTypes.SHOVELHEAD_RECIPE);

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
        Registry.register(Registry.ITEM, new Identifier(Forgero.MOD_NAMESPACE, "binding_pattern_default"), BINDING_PATTERN_DEFAULT);
        Registry.register(Registry.ITEM, new Identifier(Forgero.MOD_NAMESPACE, "handle_pattern_default"), HANDLE_PATTERN_DEFAULT);
        Registry.register(Registry.ITEM, new Identifier(Forgero.MOD_NAMESPACE, "pickaxehead_pattern_default"), PICKAXEHEAD_PATTERN_DEFAULT);
        Registry.register(Registry.ITEM, new Identifier(Forgero.MOD_NAMESPACE, "shovelhead_pattern_default"), SHOVELHEAD_PATTERN_DEFAULT);
    }
}
