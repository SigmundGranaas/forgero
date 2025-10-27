package com.sigmundgranaas.forgero.smithing.block.custom;

import com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities;
import com.sigmundgranaas.forgero.smithing.block.entity.custom.HearthBlockEntity;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;

import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class HearthBlock extends CampfireBlock implements Waterloggable {

	public HearthBlock(boolean emitsParticles, int fireDamage, Settings settings) {
		super(emitsParticles, fireDamage, settings);
		this.setDefaultState(this.getStateManager().getDefaultState()
				.with(FACING, Direction.NORTH)
				.with(LIT, true)
				.with(WATERLOGGED, false));
	}

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		super.randomDisplayTick(state, world, pos, random);
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new HearthBlockEntity(ModBlockEntities.HEARTH, pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		if (type == ModBlockEntities.HEARTH) {
			return (w, p, s, be) -> HearthBlockEntity.tick(w, p, s, (HearthBlockEntity) be);
		}
		return null;
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, net.minecraft.util.hit.BlockHitResult hit) {
		BlockEntity be = world.getBlockEntity(pos);
		if (!(be instanceof HearthBlockEntity hearth)) {
			return ActionResult.PASS;
		}

		ItemStack held = player.getStackInHand(hand);
		ItemStack slot = hearth.getStack(0);

		boolean wantsExtract = player.isSneaking() || held.isEmpty();

		if (wantsExtract && !slot.isEmpty()) {
			if (!world.isClient) {
				ItemStack extracted = slot.copy();
				player.giveItemStack(extracted);
				hearth.setStack(0, ItemStack.EMPTY);
				hearth.markDirtyAndSync();
			}
			return ActionResult.SUCCESS;
		}

		if (!held.isEmpty() && slot.isEmpty()) {
			if (!TemperatureUtils.hasMaxTemperature(held)) {
				if (world.isClient) {
					player.sendMessage(Text.literal("Only temperature items can be placed on the hearth!"), true);
				}
				return ActionResult.PASS;
			}
			if (!world.isClient) {
				ItemStack stackToInsert = held.copy();
				stackToInsert.setCount(1);
				hearth.setStack(0, stackToInsert);
				held.decrement(1);
				hearth.markDirtyAndSync();
			}
			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}
}
