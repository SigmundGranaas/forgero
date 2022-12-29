package com.sigmundgranaas.forgero.minecraft.common.item;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.settings.ForgeroSettings;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.type.Type;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.List;

import static com.sigmundgranaas.forgero.minecraft.common.item.Items.EMPTY_REPAIR_KIT;

public class DynamicItems {

    public static void registerDynamicItems() {
        if (ForgeroSettings.SETTINGS.getEnableRepairKits()) {
            registerRepairKits();
        }

    }

    public static List<Item> registerRepairKits() {
        return ForgeroStateRegistry.TREE.find(Type.TOOL_MATERIAL)
                .map(node -> node.getResources(State.class))
                .orElse(ImmutableList.<State>builder().build())
                .stream()
                .map(material -> new Identifier(Forgero.NAMESPACE, material.name() + "_repair_kit"))
                .map(identifier -> Registry.register(Registry.ITEM, identifier, new Item(new Item.Settings().group(ItemGroup.MISC).recipeRemainder(EMPTY_REPAIR_KIT))))
                .toList();

    }
}
