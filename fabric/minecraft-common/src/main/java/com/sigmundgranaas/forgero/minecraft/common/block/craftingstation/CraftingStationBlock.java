package com.sigmundgranaas.forgero.minecraft.common.block.craftingstation;

import com.sigmundgranaas.forgero.core.Forgero;

import com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationBlock;

import com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationScreenHandler;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import static net.minecraft.block.Blocks.CRAFTING_TABLE;

public class CraftingStationBlock extends Block{

	public CraftingStationBlock(Settings settings) {
		super(settings);
	}

	public static final Identifier CRAFTING_STATION = new Identifier(Forgero.NAMESPACE, "crafting_station");

	public static final Block CRAFTING_STATION_BLOCK = new CraftingStationBlock(AbstractBlock.Settings.copy(CRAFTING_TABLE));

	public static final BlockItem CRAFTING_STATION_ITEM = new BlockItem(CRAFTING_STATION_BLOCK, new Item.Settings());

	private static final VoxelShape SHAPE;

	static {
		VoxelShape shape = VoxelShapes.empty();
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.0625, 0.125, 0.0625, 0.25, 0.75, 0.25));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.75, 0.125, 0.0625, 0.9375, 0.75, 0.25));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.75, 0.125, 0.75, 0.9375, 0.75, 0.9375));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.0625, 0.125, 0.75, 0.25, 0.75, 0.9375));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0.75, 0, 1, 1, 1));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.25, 0.25, 0.125, 0.75, 0.375, 0.1875));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.25, 0.25, 0.8125, 0.75, 0.375, 0.875));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.125, 0.25, 0.25, 0.1875, 0.375, 0.75));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.8125, 0.25, 0.25, 0.875, 0.375, 0.75));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0, 0, 0.3125, 0.125, 0.3125));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.6875, 0, 0, 1, 0.125, 0.3125));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.6875, 0, 0.6875, 1, 0.125, 1));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0, 0.6875, 0.3125, 0.125, 1));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.25, 0.1875, 0.125, 0.75, 0.25, 0.875));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.75, 0.1875, 0.25, 0.875, 0.25, 0.75));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.125, 0.1875, 0.25, 0.25, 0.25, 0.75));

		SHAPE = shape.simplify();
	}

	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}




}
