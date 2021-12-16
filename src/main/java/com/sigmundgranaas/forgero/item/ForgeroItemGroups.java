package com.sigmundgranaas.forgero.item;

import com.sigmundgranaas.forgero.Forgero;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class ForgeroItemGroups {
    public static final String FORGERO_TOOL_GROUP = "tools";
    public static final String FORGERO_TOOL_PART_GROUP = "toolparts";
    public static final String FORGERO_MODIFIER_GROUP = "modifiers";

    public static final ItemGroup FORGERO_TOOLS = FabricItemGroupBuilder.create(
                    new Identifier(Forgero.MOD_NAMESPACE, FORGERO_TOOL_GROUP))
            .icon(() -> new ItemStack(Items.BOWL))
            .build();
    public static final ItemGroup FORGERO_MODIFIERS = FabricItemGroupBuilder.create(
                    new Identifier(Forgero.MOD_NAMESPACE, FORGERO_MODIFIER_GROUP))
            .icon(() -> new ItemStack(Items.BOWL))
            .build();
    public static final ItemGroup FORGERO_TOOL_PARTS = FabricItemGroupBuilder.create(
                    new Identifier(Forgero.MOD_NAMESPACE, FORGERO_TOOL_PART_GROUP))
            .icon(() -> new ItemStack(Items.BOWL))
            .build();
}
