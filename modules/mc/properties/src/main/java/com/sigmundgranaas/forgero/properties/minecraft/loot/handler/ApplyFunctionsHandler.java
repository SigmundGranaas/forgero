package com.sigmundgranaas.forgero.properties.minecraft.loot.handler;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.properties.minecraft.loot.function.ItemFunction;

import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A handler that iterates through a list of loot, applying a chain of ItemFunctions to each item.
 */
public record ApplyFunctionsHandler(List<ItemFunction> functions) implements LootHandler {
	public static final String TYPE = "forgero:apply_functions";
	public static final Codec<ApplyFunctionsHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.list(ItemFunction.CODEC).fieldOf("functions").forGetter(ApplyFunctionsHandler::functions)
	).apply(instance, ApplyFunctionsHandler::new));

	@Override
	public List<ItemStack> handle(List<ItemStack> loot, LootContext context) {
		return loot.stream()
				.map(stack -> {
					ItemStack currentStack = stack;
					for (ItemFunction function : functions) {
						currentStack = function.apply(currentStack, context);
						if (currentStack.isEmpty()) {
							break;
						}
					}
					return currentStack;
				})
				.filter(stack -> !stack.isEmpty())
				.collect(Collectors.toList());
	}

	@Override
	public String type() {
		return TYPE;
	}
}
