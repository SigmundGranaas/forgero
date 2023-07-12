package com.sigmundgranaas.forgero.fabric.block;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public class DummyHandler extends ScreenHandler {
	public static ScreenHandler dummyHandler = new DummyHandler();

	protected DummyHandler() {
		super(ScreenHandlerType.CRAFTING, 0);
	}

	@Override
	public ItemStack transferSlot(PlayerEntity player, int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return true;
	}
}
