package com.sigmundgranaas.forgero.smithing.block.custom;

import com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities;
import com.sigmundgranaas.forgero.smithing.block.entity.custom.HearthBlockEntity;
import com.sigmundgranaas.forgero.smithing.item.custom.CrucibleItem;

import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
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
			.with(SIGNAL_FIRE, false)
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
        // Replace HearthBlockEntityType.INSTANCE with your actual BlockEntityType for HearthBlockEntity
        if (type == ModBlockEntities.HEARTH) {
            return (w, p, s, be) -> HearthBlockEntity.tick(w, p, s, (HearthBlockEntity) be);
        }
        return null;
    }

	// Allow placing/taking the single-slot item by interacting with the block
	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, net.minecraft.util.hit.BlockHitResult hit) {
		BlockEntity be = world.getBlockEntity(pos);
		if (!(be instanceof HearthBlockEntity hearth)) {
			return ActionResult.PASS;
		}

		ItemStack held = player.getStackInHand(hand);
		ItemStack slot = hearth.getStack(0);

		boolean wantsExtract = player.isSneaking() || held.isEmpty();

		// Block extraction while smelting
		if (wantsExtract && hearth.isSmelting()) {
			return ActionResult.SUCCESS; // consume without action
		}

		if (wantsExtract && !slot.isEmpty()) {
			if (!world.isClient) {
				ItemStack extracted = slot.copy();
				// Remove CustomModelData if it's a CrucibleItem
				if (extracted.getItem() instanceof CrucibleItem && extracted.hasNbt() && extracted.getNbt().contains("CustomModelData")) {
					NbtCompound nbt = extracted.getNbt();
					nbt.remove("CustomModelData");
					extracted.setNbt(nbt);
				}
				player.giveItemStack(extracted);
				hearth.setStack(0, ItemStack.EMPTY);
				hearth.markDirtyAndSync();
			}
			return ActionResult.SUCCESS;
		}

		// Only allow CrucibleItem to be placed in the slot
		if (!held.isEmpty() && slot.isEmpty()) {
			if (!(held.getItem() instanceof CrucibleItem)) {
				return ActionResult.PASS;
			}
			if (!world.isClient) {
				ItemStack crucibleStack = hearth.createCustomCrucibleStack(held);
				hearth.setStack(0, crucibleStack);
				held.decrement(1);
				hearth.markDirtyAndSync();
				// Optionally start smelting immediately (server will also handle next tick)
				// hearth.tryStartSmelting(); // if made public
			}
			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}


}
