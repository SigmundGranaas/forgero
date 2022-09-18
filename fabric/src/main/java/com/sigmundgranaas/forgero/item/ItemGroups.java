package com.sigmundgranaas.forgero.item;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.ForgeroRegistry;
import com.sigmundgranaas.forgero.tool.ForgeroToolWithBinding;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.toolpart.binding.ToolPartBinding;
import com.sigmundgranaas.forgero.toolpart.factory.ToolPartBindingBuilder;
import com.sigmundgranaas.forgero.toolpart.factory.ToolPartHandleBuilder;
import com.sigmundgranaas.forgero.toolpart.factory.ToolPartHeadBuilder;
import com.sigmundgranaas.forgero.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.toolpart.head.ToolPartHead;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static com.sigmundgranaas.forgero.identifier.Common.ELEMENT_SEPARATOR;

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