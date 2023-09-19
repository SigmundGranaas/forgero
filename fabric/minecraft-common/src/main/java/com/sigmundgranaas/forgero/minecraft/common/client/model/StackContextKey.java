package com.sigmundgranaas.forgero.minecraft.common.client.model;

import com.sigmundgranaas.forgero.core.util.match.MatchContext;

import net.minecraft.item.ItemStack;

public record StackContextKey(ItemStack stack, MatchContext context) {

	@Override
	public int hashCode() {
		return stack.hashCode() + context.hashCode();
	}
}
