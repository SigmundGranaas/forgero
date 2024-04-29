package com.sigmundgranaas.forgero.fabric.gametest;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Utils {
	public static boolean canHarvestBlockWithTool(World world, BlockPos blockPos, Item tool) {
		BlockState blockState = world.getBlockState(blockPos);
		ItemStack toolStack = new ItemStack(tool);
		return toolStack.isSuitableFor(blockState);
	}


	public static Item itemFromString(String identifier) {
		Item item = Registries.ITEM.get(new Identifier(identifier));
		if (item == Items.AIR) {
			return Registries.ITEM.get(new Identifier("forgero:" + identifier));
		}
		return item;
	}

	public static <T> T handlerFromString(String handler, JsonBuilder<T> builder) {
		JsonElement element = JsonParser.parseString(handler);
		return builder.build(element).orElseThrow(() -> new IllegalStateException(String.format("Encountered invalid handler when trying to build from string. It's either invalid or the API has changed. Here is the handler in question: %s", handler)));
	}

	public static <T> T handlerFromString(String handler, Codec<T> builder) {
		JsonElement element = JsonParser.parseString(handler);
		return builder.decode(JsonOps.INSTANCE, element)
				.result()
				.orElseThrow(() -> new IllegalStateException(String.format("Encountered invalid handler when trying to build from string. It's either invalid or the API has changed. Here is the handler in question: %s", handler)))
				.getFirst();
	}

}
