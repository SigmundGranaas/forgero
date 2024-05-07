package com.sigmundgranaas.forgero.blocks.block.custom;

import com.sigmundgranaas.forgero.core.Forgero;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

import static com.sigmundgranaas.forgero.blocks.block.ModBlocks.CRAFTING_STATION;
import static net.minecraft.block.Blocks.CRAFTING_TABLE;

public class CraftingStation extends Block{

	public CraftingStation(Settings settings) {
		super(settings);
	}

	public static final BlockItem CRAFTING_STATION_ITEM = new BlockItem(CRAFTING_STATION, new Item.Settings());





}
