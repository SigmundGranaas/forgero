package com.sigmundgranaas.forgero.item;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgerocore.ForgeroRegistry;
import com.sigmundgranaas.forgerocore.tool.ForgeroToolWithBinding;
import com.sigmundgranaas.forgerocore.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgerocore.toolpart.binding.ToolPartBinding;
import com.sigmundgranaas.forgerocore.toolpart.factory.ToolPartBindingBuilder;
import com.sigmundgranaas.forgerocore.toolpart.factory.ToolPartHandleBuilder;
import com.sigmundgranaas.forgerocore.toolpart.factory.ToolPartHeadBuilder;
import com.sigmundgranaas.forgerocore.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgerocore.toolpart.head.ToolPartHead;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static com.sigmundgranaas.forgerocore.identifier.Common.ELEMENT_SEPARATOR;

public class ItemGroups {
    public static final String FORGERO_GROUP = "forgero";

    public static final ItemGroup FORGERO_TOOL_PARTS = FabricItemGroupBuilder.create(
                    new Identifier(ForgeroInitializer.MOD_NAMESPACE, FORGERO_GROUP))
            .icon(ItemGroups::createForgeroToolIcon)
            .build();

    private static ItemStack createForgeroToolIcon() {
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
        return stack;
    }
}