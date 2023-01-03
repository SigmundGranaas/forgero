package com.sigmundgranaas.forgero.minecraft.common.item;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.settings.ForgeroSettings;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.type.Type;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.List;

import static com.sigmundgranaas.forgero.minecraft.common.item.Items.EMPTY_REPAIR_KIT;

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
                .map(identifier -> Registry.register(Registries.ITEM, identifier, new Item(new FabricItemSettings().recipeRemainder(EMPTY_REPAIR_KIT))))
                .toList();
        items.forEach(item -> ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(item)));

        return items;
    }
}
