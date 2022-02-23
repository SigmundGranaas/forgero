package com.sigmundgranaas.forgero.item;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class ItemGroups {
    public static final String FORGERO_TOOL_PART_GROUP = "toolparts";

    public static final ItemGroup FORGERO_TOOL_PARTS = FabricItemGroupBuilder.create(
                    new Identifier(ForgeroInitializer.MOD_NAMESPACE, FORGERO_TOOL_PART_GROUP))
            .icon(() -> new ItemStack(Items.BOWL))
            .build();
}
