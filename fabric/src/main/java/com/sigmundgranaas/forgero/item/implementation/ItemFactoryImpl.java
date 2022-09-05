package com.sigmundgranaas.forgero.item.implementation;

import com.sigmundgranaas.forgerocore.gem.Gem;
import com.sigmundgranaas.forgerocore.property.Property;
import com.sigmundgranaas.forgerocore.property.passive.StaticPassiveType;
import com.sigmundgranaas.forgerocore.schematic.Schematic;
import com.sigmundgranaas.forgerocore.tool.ForgeroTool;
import com.sigmundgranaas.forgerocore.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgerocore.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.ItemFactory;
import com.sigmundgranaas.forgero.item.ItemGroups;
import com.sigmundgranaas.forgero.item.adapter.DescriptionWriter;
import com.sigmundgranaas.forgero.item.adapter.SimpleToolMaterialAdapter;
import com.sigmundgranaas.forgero.item.items.GemItem;
import com.sigmundgranaas.forgero.item.items.SchematicItem;
import com.sigmundgranaas.forgero.item.items.ToolPartItemImpl;
import com.sigmundgranaas.forgero.item.items.tool.*;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
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
    public ForgeroToolItem createTool(ForgeroTool tool) {
        Item.Settings settings = new Item.Settings().group(ItemGroup.TOOLS);
        if (tool.getPropertyStream().getStaticPassiveProperties().anyMatch(property -> property.getStaticType() == StaticPassiveType.FIREPROOF)) {
            settings.fireproof();
        }
        if (tool.getToolType() == ForgeroToolTypes.SWORD) {
            settings.group(ItemGroup.COMBAT);
        }

        return switch (tool.getToolType()) {
            case PICKAXE -> new ForgeroPickaxeItem(new SimpleToolMaterialAdapter(tool.getMaterial()), settings, tool);
            case SHOVEL -> new ForgeroShovelItem(new SimpleToolMaterialAdapter(tool.getMaterial()), settings, tool);
            case AXE -> new ForgeroAxeItem(new SimpleToolMaterialAdapter(tool.getMaterial()), settings, tool);
            case SWORD -> new ForgeroSwordItem(new SimpleToolMaterialAdapter(tool.getMaterial()), settings, tool);
            case HOE -> new ForgeroHoeItem(new SimpleToolMaterialAdapter(tool.getMaterial()), settings, tool);
        };
    }

    @Override
    public Item createToolPart(ForgeroToolPart toolPart) {
        Item.Settings settings = new Item.Settings().group(ItemGroups.FORGERO_TOOL_PARTS);
        if (Property.stream(toolPart.getPrimaryMaterial().getPrimaryProperties()).getStaticPassiveProperties().anyMatch(property -> property.getStaticType() == StaticPassiveType.FIREPROOF)) {
            settings.fireproof();
        }
        return new ToolPartItemImpl(settings, toolPart.getPrimaryMaterial(), toolPart.getToolPartType(), toolPart);
    }

    @Override
    public SchematicItem createSchematic(Schematic pattern) {
        return new SchematicItem(new Item.Settings().group(ItemGroups.FORGERO_TOOL_PARTS).rarity(DescriptionWriter.getRarityFromInt(pattern.getRarity())), pattern);
    }

    @Override
    public GemItem createGem(Gem gem) {
        return new GemItem(new FabricItemSettings().group(ItemGroup.MISC), gem);
    }
}
