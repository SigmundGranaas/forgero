package com.sigmundgranaas.forgero.fabric.gametest.cases;

import net.minecraft.item.ItemStack;
import net.minecraft.test.GameTestException;

public class ItemStackCase {
	public static void assertDamage(ItemStack stack, int damage) {
		// Ensure the tool has taken damage
		if (stack.getDamage() == 0 && damage > 0) {
			throw new GameTestException("Expected the item to take damage, but it did not");
		}

		// Ensure tool has taken exactly the damage it should
		if (stack.getDamage() != damage) {
			throw new GameTestException(String.format("Expected the item to take %s damage, but it took %s", damage, stack.getDamage()));
		}
	}
}
