package com.sigmundgranaas.forgero.properties.minecraft.loot.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.properties.minecraft.loot.filter.ItemFilter;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmeltingRecipe;
import org.jetbrains.annotations.NotNull;

/**
 * A function that attempts to smelt a dropped item, like converting raw iron to an iron ingot.
 */
public record AutoSmeltFunction(ItemFilter filter) implements ItemFunction {
	public static final String TYPE = "forgero:auto_smelt";
	public static final Codec<AutoSmeltFunction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ItemFilter.CODEC.fieldOf("filter").forGetter(AutoSmeltFunction::filter)
	).apply(instance, AutoSmeltFunction::new));

	@Override
	@NotNull
	public ItemStack apply(ItemStack stack, LootContext context) {
		if (!filter.test(stack) || context.getWorld().isClient()) {
			return stack;
		}

		return context.getWorld().getRecipeManager()
				.getFirstMatch(RecipeType.SMELTING, new SimpleInventory(stack), context.getWorld())
				.map(recipe -> recipe.getOutput(context.getWorld().getRegistryManager()))
				.map(ItemStack::copy)
				.orElse(stack);
	}

	@Override
	public String type() {
		return TYPE;
	}
}
