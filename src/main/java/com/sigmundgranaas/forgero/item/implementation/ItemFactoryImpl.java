package com.sigmundgranaas.forgero.item.implementation;

import com.sigmundgranaas.forgero.core.pattern.HeadPattern;
import com.sigmundgranaas.forgero.core.pattern.Pattern;
import com.sigmundgranaas.forgero.core.property.attribute.Target;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.item.ItemFactory;
import com.sigmundgranaas.forgero.item.ItemGroups;
import com.sigmundgranaas.forgero.item.adapter.DescriptionWriter;
import com.sigmundgranaas.forgero.item.adapter.SimpleToolMaterialAdapter;
import com.sigmundgranaas.forgero.item.items.PatternItem;
import com.sigmundgranaas.forgero.item.items.ToolPartItemImpl;
import com.sigmundgranaas.forgero.item.items.tool.ForgeroAxeItem;
import com.sigmundgranaas.forgero.item.items.tool.ForgeroPickaxeItem;
import com.sigmundgranaas.forgero.item.items.tool.ShovelItem;
import com.sigmundgranaas.forgero.recipe.customrecipe.RecipeTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;

public class ItemFactoryImpl implements ItemFactory {
    public static ItemFactory INSTANCE;

    public static ItemFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ItemFactoryImpl();
        }
        return INSTANCE;
    }

    @Override
    public Item createTool(ForgeroTool tool) {
        Target target = Target.createEmptyTarget();
        return switch (tool.getToolType()) {
            case PICKAXE -> new ForgeroPickaxeItem(new SimpleToolMaterialAdapter(tool.getMaterial()), new Item.Settings().group(ItemGroup.TOOLS), tool);
            case SHOVEL -> new ShovelItem(new SimpleToolMaterialAdapter(tool.getMaterial()), new Item.Settings().group(ItemGroup.TOOLS), tool);
            case AXE -> new ForgeroAxeItem(new SimpleToolMaterialAdapter(tool.getMaterial()), new Item.Settings().group(ItemGroup.TOOLS), tool);
            case SWORD -> null;
        };
    }

    @Override
    public Item createToolPart(ForgeroToolPart toolPart) {
        return new ToolPartItemImpl(new Item.Settings().group(ItemGroups.FORGERO_TOOL_PARTS), toolPart.getPrimaryMaterial(), toolPart.getToolPartType(), toolPart);
    }

    @Override
    public PatternItem createPattern(Pattern pattern) {
        RecipeTypes recipeTypes = switch (pattern.getType()) {
            case BINDING -> RecipeTypes.BINDING_RECIPE;
            case HANDLE -> RecipeTypes.HANDLE_RECIPE;
            case HEAD -> switch (((HeadPattern) pattern).getToolType()) {
                case PICKAXE -> RecipeTypes.PICKAXEHEAD_RECIPE;
                case SHOVEL -> RecipeTypes.SHOVELHEAD_RECIPE;
                case AXE -> RecipeTypes.AXEHEAD_RECIPE;
                case SWORD -> RecipeTypes.AXEHEAD_RECIPE;
            };
        };
        return new PatternItem(new Item.Settings().group(ItemGroups.FORGERO_TOOL_PARTS).rarity(DescriptionWriter.getRarityFromInt(pattern.getRarity())), recipeTypes, pattern);
    }
}
