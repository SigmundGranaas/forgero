package com.sigmundgranaas.forgero.smithing.block.custom;

import java.util.Set;

import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities;
import com.sigmundgranaas.forgero.smithing.block.entity.custom.HearthBlockEntity;
import com.sigmundgranaas.forgero.smithing.item.custom.MorphedItem;
import com.sigmundgranaas.forgero.smithing.item.custom.SmithingTongsItem;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureRules;

import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class HearthBlock extends CampfireBlock implements Waterloggable {
	// Add material id/name fragments here when more materials should require the Soul Hearth.
	private static final Set<String> SOUL_HEARTH_REQUIRED_MATERIALS = Set.of(
			"netherite"
	);
	private static final int MORPHED_STACK_CHECK_DEPTH = 4;

	private final boolean heatsSoulHearthMaterials;

	public HearthBlock(boolean emitsParticles, int fireDamage, boolean heatsSoulHearthMaterials, Settings settings) {
		super(emitsParticles, fireDamage, settings);
		this.heatsSoulHearthMaterials = heatsSoulHearthMaterials;
		this.setDefaultState(this.getStateManager().getDefaultState()
				.with(FACING, Direction.NORTH)
				.with(LIT, true)
				.with(WATERLOGGED, false));
	}

	public boolean canHeat(ItemStack stack) {
		return TemperatureRules.canTrackTemperature(stack) && canWarm(stack);
	}

	private boolean canPlaceHeldItem(ItemStack stack) {
		return TemperatureRules.canTrackTemperature(stack) && canWarm(stack);
	}

	private boolean canPlaceTongsItem(ItemStack stack) {
		return SmithingTongsItem.canStore(stack) && canWarm(stack);
	}

	private boolean canWarm(ItemStack stack) {
		return heatsSoulHearthMaterials || !requiresSoulHearth(stack);
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

		if (held.getItem() instanceof SmithingTongsItem) {
			return useTongsOnHearth(world, player, held, hearth, slot);
		}

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
			if (!canPlaceHeldItem(held)) {
				sendRejectedStackMessage(world, player, held);
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

	private ActionResult useTongsOnHearth(
			World world,
			PlayerEntity player,
			ItemStack tongsStack,
			HearthBlockEntity hearth,
			ItemStack slot
	) {
		if (SmithingTongsItem.hasStoredStack(tongsStack)) {
			if (!slot.isEmpty()) {
				return ActionResult.PASS;
			}

			ItemStack stored = SmithingTongsItem.getStoredStack(tongsStack);
			if (!canPlaceTongsItem(stored)) {
				sendRejectedStackMessage(world, player, stored);
				return ActionResult.PASS;
			}

			if (!world.isClient) {
				stored = SmithingTongsItem.removeStoredStack(tongsStack);

				if (!stored.isEmpty()) {
					hearth.setStack(0, stored);
					hearth.markDirtyAndSync();
					player.getInventory().markDirty();
				}
			}

			return ActionResult.SUCCESS;
		}

		if (slot.isEmpty() || !SmithingTongsItem.canStore(slot)) {
			return ActionResult.PASS;
		}

		if (!world.isClient) {
			ItemStack stored = slot.copy();
			stored.setCount(1);

			SmithingTongsItem.setStoredStack(tongsStack, stored);

			if (slot.getCount() <= 1) {
				hearth.setStack(0, ItemStack.EMPTY);
			} else {
				slot.decrement(1);
			}

			hearth.markDirtyAndSync();
			player.getInventory().markDirty();
		}

		return ActionResult.SUCCESS;
	}

	private void sendRejectedStackMessage(World world, PlayerEntity player, ItemStack stack) {
		if (!world.isClient) {
			return;
		}

		if (requiresSoulHearth(stack) && !heatsSoulHearthMaterials) {
			player.sendMessage(Text.literal("This material can only be warmed on the Soul Hearth!"), true);
			return;
		}

		if (!TemperatureRules.canTrackTemperature(stack)) {
			player.sendMessage(Text.literal("Only temperature items can be placed on the hearth!"), true);
		}
	}

	private static boolean requiresSoulHearth(ItemStack stack) {
		return requiresSoulHearth(stack, 0);
	}

	private static boolean requiresSoulHearth(ItemStack stack, int depth) {
		if (stack.isEmpty() || depth > MORPHED_STACK_CHECK_DEPTH) {
			return false;
		}

		if (matchesSoulHearthMaterial(Registries.ITEM.getId(stack.getItem()).toString())) {
			return true;
		}

		if (StateService.INSTANCE.convert(stack).map(HearthBlock::requiresSoulHearth).orElse(false)) {
			return true;
		}

		if (stack.getItem() instanceof MorphedItem) {
			return requiresSoulHearth(MorphedItem.getStartStack(stack), depth + 1)
					|| requiresSoulHearth(MorphedItem.getResultStack(stack), depth + 1);
		}

		return false;
	}

	private static boolean requiresSoulHearth(State state) {
		if (matchesSoulHearthMaterial(state.identifier())) {
			return true;
		}

		if (state instanceof Composite composite) {
			return SOUL_HEARTH_REQUIRED_MATERIALS.stream().anyMatch(material -> composite.has(material).isPresent());
		}

		return false;
	}

	private static boolean matchesSoulHearthMaterial(String id) {
		return SOUL_HEARTH_REQUIRED_MATERIALS.stream().anyMatch(id::contains);
	}
}
