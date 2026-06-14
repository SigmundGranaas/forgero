package com.sigmundgranaas.forgero.smithing.networking;

import java.util.List;

import com.sigmundgranaas.forgero.smithing.block.entity.custom.SmithingAnvilBlockEntity;
import com.sigmundgranaas.forgero.smithing.item.custom.MorphedItem;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;
import com.sigmundgranaas.forgero.smithing.util.SchematicMaterialCost;
import com.sigmundgranaas.forgero.smithing.util.SchematicResultUtil;

import net.minecraft.block.AnvilBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class SmithingPacketValidator {
	private static final double MAX_ANVIL_USE_DISTANCE_SQUARED = 64.0D;

	private SmithingPacketValidator() {
	}

	public static SmithingAnvilBlockEntity getValidSmithingAnvil(
			ServerPlayerEntity player,
			BlockPos pos
	) {
		if (player == null || pos == null) {
			return null;
		}

		if (player.isRemoved() || player.isSpectator()) {
			return null;
		}

		ServerWorld world = player.getServerWorld();

		if (player.getPos().squaredDistanceTo(Vec3d.ofCenter(pos)) > MAX_ANVIL_USE_DISTANCE_SQUARED) {
			return null;
		}

		if (!world.isChunkLoaded(pos)) {
			return null;
		}

		if (!(world.getBlockState(pos).getBlock() instanceof AnvilBlock)) {
			return null;
		}

		BlockEntity blockEntity = world.getBlockEntity(pos);

		if (!(blockEntity instanceof SmithingAnvilBlockEntity smithingAnvil)) {
			return null;
		}

		return smithingAnvil;
	}

	public static boolean isValidHand(Hand hand) {
		return hand == Hand.MAIN_HAND || hand == Hand.OFF_HAND;
	}

	public static boolean canPickupFromAnvil(ItemStack stackInHand, ItemStack anvilStack) {
		return stackInHand.isEmpty() && !anvilStack.isEmpty();
	}

	public static boolean canPlaceOnAnvil(ItemStack stackInHand, ItemStack anvilStack) {
		if (stackInHand.isEmpty()) {
			return false;
		}

		/*
		 * Restoring unfinished morphed items is valid only when the anvil is empty.
		 */
		if (stackInHand.getItem() instanceof MorphedItem) {
			return anvilStack.isEmpty();
		}

		/*
		 * Material ingots are allowed even when the anvil already has an ingot stack.
		 * SmithingAnvilBlockEntity.tryPlaceItem(...) does the exact validation:
		 * same item, not morphed, max stack cap, planned product null, etc.
		 */
		return TemperatureUtils.hasMaxTemperature(stackInHand);
	}

	public static boolean canSelectSchematicProduct(
			ServerPlayerEntity player,
			SmithingAnvilBlockEntity anvil,
			Identifier selectedProduct
	) {
		if (player == null || anvil == null || selectedProduct == null) {
			return false;
		}

		ItemStack anvilStack = anvil.getInventory().getStack(0);

		if (anvilStack.isEmpty()) {
			return false;
		}

		if (anvilStack.getItem() instanceof MorphedItem) {
			return false;
		}

		if (!TemperatureUtils.hasMaxTemperature(anvilStack)) {
			return false;
		}

		int requiredCost = SchematicMaterialCost.getCost(selectedProduct);

		if (anvilStack.getCount() < requiredCost) {
			return false;
		}

		List<Identifier> availableProducts =
				SchematicResultUtil.findAvailableSchematicProductsForPlayer(player);

		if (!availableProducts.contains(selectedProduct)) {
			return false;
		}

		return !anvil.createProductFromPlanned(selectedProduct).isEmpty();
	}
}
