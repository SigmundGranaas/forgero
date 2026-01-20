package com.sigmundgranaas.forgero.minecraft.common.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;

public record ItemData(Item item, Identifier id, Item.Settings settings, ItemGroup group) {
}
