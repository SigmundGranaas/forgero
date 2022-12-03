package com.sigmundgranaas.forgero.item;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ItemGroups {
    public static final String FORGERO_GROUP = "forgero";

    public static final ItemGroup FORGERO_TOOL_PARTS = FabricItemGroupBuilder.create(
                    new Identifier(ForgeroInitializer.MOD_NAMESPACE, "parts"))
            .icon(ItemGroups::createPartIcon)
            .build();

    public static final ItemGroup FORGERO_SCHEMATICS = FabricItemGroupBuilder.create(
                    new Identifier(ForgeroInitializer.MOD_NAMESPACE, "schematics"))
            .icon(ItemGroups::createSchematicIcon)
            .build();

    public static final ItemGroup FORGERO_TOOLS = FabricItemGroupBuilder.create(
                    new Identifier(ForgeroInitializer.MOD_NAMESPACE, "tools"))
            .icon(ItemGroups::createForgeroToolIcon)
            .build();

    public static final ItemGroup FORGERO_WEAPONS = FabricItemGroupBuilder.create(
                    new Identifier(ForgeroInitializer.MOD_NAMESPACE, "weapons"))
            .icon(ItemGroups::createWeaponIcon)
            .build();

    public static final ItemGroup FORGERO_GEMS = FabricItemGroupBuilder.create(
                    new Identifier(ForgeroInitializer.MOD_NAMESPACE, "trinkets"))
            .icon(ItemGroups::createTrinketIcon)
            .build();

    private static ItemStack createForgeroToolIcon() {
        /**
         ForgeroToolPart head = new ToolPartHeadBuilder(ForgeroRegistry.TOOL_PART.getHead("iron-pickaxehead").get())
         .setSecondary(ForgeroRegistry.MATERIAL.getSecondaryMaterial("netherite").get())
         .setGem(ForgeroRegistry.GEM.getResource("lapis-gem").get())
         .createToolPart();

         ForgeroToolPart handle = new ToolPartHandleBuilder(ForgeroRegistry.TOOL_PART.getHandle("spruce-handle").get())
         .setSecondary(ForgeroRegistry.MATERIAL.getSecondaryMaterial("rabbit_hide").get())
         .setGem(ForgeroRegistry.GEM.getResource("diamond-gem").get())
         .createToolPart();

         ForgeroToolPart binding = new ToolPartBindingBuilder(ForgeroRegistry.TOOL_PART.getBinding("netherite-binding").get())
         .setSecondary(ForgeroRegistry.MATERIAL.getSecondaryMaterial("leather").get())
         .createToolPart();

         var tool = new ForgeroToolWithBinding((ToolPartHead) head, (ToolPartHandle) handle, (ToolPartBinding) binding);
         var stack = new ItemStack(Registry.ITEM.get(new Identifier(ForgeroInitializer.MOD_NAMESPACE, "iron" + ELEMENT_SEPARATOR + "pickaxe")));
         stack.getOrCreateNbt().put(NBTFactory.FORGERO_TOOL_NBT_IDENTIFIER, NBTFactory.INSTANCE.createNBTFromTool(tool));

         */
        return new ItemStack(Items.DIAMOND_AXE);
    }

    private static ItemStack createWeaponIcon() {
        return new ItemStack(Items.DIAMOND_SWORD);
    }

    private static ItemStack createSchematicIcon() {
        return new ItemStack(Registry.ITEM.get(new Identifier("forgero:pickaxe_head-schematic")));
    }

    private static ItemStack createPartIcon() {
        return new ItemStack(Registry.ITEM.get(new Identifier("forgero:iron-pickaxe_head")));
    }

    private static ItemStack createTrinketIcon() {
        return new ItemStack(Registry.ITEM.get(new Identifier("forgero:redstone-gem")));
    }
}