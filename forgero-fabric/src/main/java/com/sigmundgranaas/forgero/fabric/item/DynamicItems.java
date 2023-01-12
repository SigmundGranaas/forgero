package com.sigmundgranaas.forgero.fabric.item;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.settings.ForgeroSettings;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.List;

import static com.sigmundgranaas.forgero.fabric.item.Items.EMPTY_REPAIR_KIT;

public class DynamicItems {

    public static void registerDynamicItems() {
        if (ForgeroSettings.SETTINGS.getEnableRepairKits()) {
            registerRepairKits();
        }

    }

    public static List<Item> registerRepairKits() {
        var items = ForgeroStateRegistry.TREE.find(Type.TOOL_MATERIAL)
                .map(node -> node.getResources(State.class))
                .orElse(ImmutableList.<State>builder().build())
                .stream()
                .map(material -> new Identifier(Forgero.NAMESPACE, material.name() + "_repair_kit"))
                .map(identifier -> Registry.register(Registry.ITEM, identifier, new Item(new FabricItemSettings().group(ItemGroup.COMBAT).recipeRemainder(EMPTY_REPAIR_KIT))))
                .toList();
        return items;
    }
}
