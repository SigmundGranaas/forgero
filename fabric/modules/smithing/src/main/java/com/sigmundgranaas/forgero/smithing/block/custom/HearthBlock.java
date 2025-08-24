package com.sigmundgranaas.forgero.smithing.block.custom;

import com.sigmundgranaas.forgero.minecraft.common.item.StateItem;
import com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities;
import com.sigmundgranaas.forgero.smithing.block.entity.custom.HearthBlockEntity;
import com.sigmundgranaas.forgero.smithing.item.custom.CrucibleItem;
import com.sigmundgranaas.forgero.smithing.util.TemperatureItemUtil;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

//TODO Fuel System & Heating parts and ingots & Recipe system is fucked?


public class HearthBlock extends CampfireBlock implements Waterloggable {
	public static final BooleanProperty CRUCIBLE_PRESENT = BooleanProperty.of("crucible_present");

	public HearthBlock(boolean emitsParticles, int fireDamage, Settings settings) {
		super(emitsParticles, fireDamage, settings);
		this.setDefaultState(this.getStateManager().getDefaultState()
			.with(FACING, Direction.NORTH)
			.with(LIT, true)
			.with(CRUCIBLE_PRESENT, false)
			.with(WATERLOGGED, false));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(CRUCIBLE_PRESENT);
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
			if (world.isClient) {
				player.sendMessage(Text.literal("That's not done yet!"), true);
			}
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
				// Update block state: crucible removed
				world.setBlockState(pos, state.with(CRUCIBLE_PRESENT, false), 3);
			}
			return ActionResult.SUCCESS;
		}

		// Accept only Crucible or temperature items in the slot
		if (!held.isEmpty() && slot.isEmpty()) {
			boolean isCrucible = held.getItem() instanceof CrucibleItem;
			boolean isTemperatureItem = com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils.hasMaxTemperature(held);
			if (!isCrucible && !isTemperatureItem) {
				if (world.isClient) {
					player.sendMessage(Text.literal("Only crucibles and temperature items can be placed on the hearth!"), true);
				}
				return ActionResult.PASS;
			}
			if (!world.isClient) {
				ItemStack stackToInsert;
				if (isCrucible) {
					stackToInsert = hearth.createCustomCrucibleStack(held);
				} else {
					stackToInsert = held.copy();
					stackToInsert.setCount(1);
				}
				hearth.setStack(0, stackToInsert);
				held.decrement(1);
				hearth.markDirtyAndSync();
				// Update block state: crucible placed (or temperature item, same visual)
				world.setBlockState(pos, state.with(CRUCIBLE_PRESENT, true), 3);
			}
			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}


}
