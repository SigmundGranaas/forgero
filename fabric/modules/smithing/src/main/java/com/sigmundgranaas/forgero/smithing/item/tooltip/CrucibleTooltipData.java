package com.sigmundgranaas.forgero.smithing.item.tooltip;

import net.minecraft.client.item.TooltipData;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public record CrucibleTooltipData(ItemStack storedItem, int count, Identifier liquidType) implements TooltipData {
}
