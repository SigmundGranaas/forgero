package com.sigmundgranaas.forgero.item;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;

public class ItemGroups {
    public static final String FORGERO_GROUP = "forgero";

    public static final ItemGroup FORGERO_TOOL_PARTS = FabricItemGroupBuilder.create(
                    new Identifier(ForgeroInitializer.MOD_NAMESPACE, FORGERO_GROUP))
            .icon(() -> new ItemStack(Registry.ITEM.get(new Identifier(ForgeroInitializer.MOD_NAMESPACE, "oak" + ELEMENT_SEPARATOR + "handle"))))
            .build();
}
