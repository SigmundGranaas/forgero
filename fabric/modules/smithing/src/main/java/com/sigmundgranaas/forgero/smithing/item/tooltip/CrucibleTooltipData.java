package com.sigmundgranaas.forgero.smithing.item.tooltip;

import net.minecraft.client.item.TooltipData;
import net.minecraft.item.ItemStack;

public record CrucibleTooltipData(ItemStack storedItem, int count) implements TooltipData {
}
