package com.sigmundgranaas.forgero.smithing.item.custom;

import java.util.List;

import com.sigmundgranaas.forgero.smithing.minigame.MinigameLogic;
import com.sigmundgranaas.forgero.smithing.minigame.SmithingRewardData;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureRules;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureState;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SmithingTongsItem extends Item {
	public static final String STORED_STACK_KEY = "forgero_stored_stack";
	private static final int MAX_USE_TIME = 72_000;
	private static final int QUENCH_INTERVAL_TICKS = 5;
	private static final int QUENCH_DEGREES_PER_INTERVAL = 80;

	public SmithingTongsItem(Settings settings) {
		super(settings);
	}

	public static boolean canStore(ItemStack stack) {
		return !stack.isEmpty()
				&& !(stack.getItem() instanceof SmithingTongsItem)
				&& (stack.getItem() instanceof MorphedItem || TemperatureRules.canTrackTemperature(stack));
	}

	public static boolean hasStoredStack(ItemStack tongsStack) {
		return !getStoredStack(tongsStack).isEmpty();
	}

	public static ItemStack getStoredStack(ItemStack tongsStack) {
		if (tongsStack.isEmpty() || !tongsStack.hasNbt()) {
			return ItemStack.EMPTY;
		}

		NbtCompound nbt = tongsStack.getNbt();

		if (nbt == null || !nbt.contains(STORED_STACK_KEY, NbtCompound.COMPOUND_TYPE)) {
			return ItemStack.EMPTY;
		}

		ItemStack stored = ItemStack.fromNbt(nbt.getCompound(STORED_STACK_KEY));

		if (!canStore(stored)) {
			return ItemStack.EMPTY;
		}

		stored.setCount(1);
		return stored;
	}

	public static boolean tryStoreOne(ItemStack tongsStack, ItemStack sourceStack) {
		if (hasStoredStack(tongsStack) || !canStore(sourceStack)) {
			return false;
		}

		ItemStack stored = sourceStack.copy();
		stored.setCount(1);

		setStoredStack(tongsStack, stored);
		sourceStack.decrement(1);

		return true;
	}

	public static void setStoredStack(ItemStack tongsStack, ItemStack storedStack) {
		if (tongsStack.isEmpty() || !canStore(storedStack)) {
			return;
		}

		ItemStack stored = storedStack.copy();
		stored.setCount(1);

		NbtCompound storedNbt = new NbtCompound();
		stored.writeNbt(storedNbt);

		tongsStack.getOrCreateNbt().put(STORED_STACK_KEY, storedNbt);
	}

	public static ItemStack removeStoredStack(ItemStack tongsStack) {
		ItemStack stored = getStoredStack(tongsStack);
		clearStoredStack(tongsStack);

		if (!MorphedItem.needsQuench(stored)
				&& TemperatureState.currentTemperature(stored) <= TemperatureState.DEFAULT_TEMPERATURE) {
			TemperatureRules.removeTemperatureData(stored);
		}

		return stored;
	}

	public static void clearStoredStack(ItemStack tongsStack) {
		if (tongsStack.isEmpty() || !tongsStack.hasNbt()) {
			return;
		}

		NbtCompound nbt = tongsStack.getNbt();

		if (nbt == null) {
			return;
		}

		nbt.remove(STORED_STACK_KEY);

		if (nbt.isEmpty()) {
			tongsStack.setNbt(null);
		}
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		PlayerEntity player = context.getPlayer();

		if (player == null
				|| !TemperatureRules.isWaterCauldron(context.getWorld().getBlockState(context.getBlockPos()))
				|| !canQuench(context.getStack())) {
			return ActionResult.PASS;
		}

		player.setCurrentHand(context.getHand());
		return ActionResult.success(context.getWorld().isClient);
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.NONE;
	}

	@Override
	public int getMaxUseTime(ItemStack stack) {
		return MAX_USE_TIME;
	}

	@Override
	public void usageTick(World world, LivingEntity user, ItemStack tongsStack, int remainingUseTicks) {
		BlockPos cauldronPos = getTargetedWaterCauldron(world, user);

		if (cauldronPos == null || !canQuench(tongsStack)) {
			user.stopUsingItem();
			return;
		}

		if (world.isClient || (MAX_USE_TIME - remainingUseTicks) % QUENCH_INTERVAL_TICKS != 0) {
			return;
		}

		ItemStack stored = getStoredStack(tongsStack);
		int temperature = TemperatureState.currentTemperature(stored);

		if (temperature <= TemperatureState.DEFAULT_TEMPERATURE) {
			if (MorphedItem.needsQuench(stored)) {
				SmithingRewardData.beginQuenchSessionIfNeeded(stored);
				SmithingRewardData.completeFinalQuenchIfReady(stored);
			}

			ItemStack finalized = MinigameLogic.finalizeAfterQuenchIfReady(stored, world, cauldronPos);

			if (finalized != stored) {
				setStoredStack(tongsStack, finalized);

				if (user instanceof PlayerEntity player) {
					player.getInventory().markDirty();
				}
			} else if (MorphedItem.needsQuench(stored)) {
				setStoredStack(tongsStack, stored);
			}

			return;
		}

		SmithingRewardData.beginQuenchSessionIfNeeded(stored);

		int cooledTemperature = Math.max(
				TemperatureState.DEFAULT_TEMPERATURE,
				temperature - QUENCH_DEGREES_PER_INTERVAL
		);

		TemperatureRules.quench(stored, cooledTemperature);
		SmithingRewardData.completeFinalQuenchIfReady(stored);
		setStoredStack(tongsStack, MinigameLogic.finalizeAfterQuenchIfReady(stored, world, cauldronPos));

		if (user instanceof PlayerEntity player) {
			player.getInventory().markDirty();
		}

		emitQuenchingEffects((ServerWorld) world, cauldronPos);
	}

	@Override
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		if (world.isClient) {
			return;
		}

		ItemStack stored = getStoredStack(stack);

		if (stored.isEmpty()) {
			return;
		}

		SmithingRewardData.clearActiveQuenchSession(stored);
		setStoredStack(stack, stored);

		if (user instanceof PlayerEntity player) {
			player.getInventory().markDirty();
		}
	}

	private static boolean canQuench(ItemStack tongsStack) {
		ItemStack stored = getStoredStack(tongsStack);
		return !stored.isEmpty()
				&& (TemperatureRules.canTrackTemperature(stored) || MorphedItem.needsQuench(stored));
	}

	@Nullable
	private static BlockPos getTargetedWaterCauldron(World world, LivingEntity user) {
		HitResult hitResult = user.raycast(5.0D, 1.0F, false);

		if (!(hitResult instanceof BlockHitResult blockHit)) {
			return null;
		}

		BlockPos pos = blockHit.getBlockPos();
		return TemperatureRules.isWaterCauldron(world.getBlockState(pos)) ? pos : null;
	}

	private static void emitQuenchingEffects(ServerWorld world, BlockPos pos) {
		double x = pos.getX() + 0.5D;
		double y = pos.getY() + 0.85D;
		double z = pos.getZ() + 0.5D;

		world.spawnParticles(ParticleTypes.CLOUD, x, y, z, 8, 0.25D, 0.1D, 0.25D, 0.02D);
		world.spawnParticles(ParticleTypes.SMOKE, x, y, z, 5, 0.2D, 0.08D, 0.2D, 0.01D);
		world.playSound(
				null,
				pos,
				SoundEvents.BLOCK_FIRE_EXTINGUISH,
				SoundCategory.BLOCKS,
				0.7F,
				1.2F
		);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack tongsStack = user.getStackInHand(hand);

		if (hasStoredStack(tongsStack)) {
			if (!user.isSneaking()) {
				return TypedActionResult.pass(tongsStack);
			}

			if (!world.isClient) {
				ItemStack stored = removeStoredStack(tongsStack);

				if (!stored.isEmpty()) {
					user.getInventory().offerOrDrop(stored);
					user.getInventory().markDirty();
				}
			}

			return TypedActionResult.success(tongsStack, world.isClient());
		}

		Hand otherHand = hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
		ItemStack otherStack = user.getStackInHand(otherHand);

		if (!canStore(otherStack)) {
			return TypedActionResult.pass(tongsStack);
		}

		if (!world.isClient && tryStoreOne(tongsStack, otherStack)) {
			user.getInventory().markDirty();
		}

		return TypedActionResult.success(tongsStack, world.isClient());
	}

	@Override
	public void appendTooltip(
			ItemStack stack,
			@Nullable World world,
			List<Text> tooltip,
			TooltipContext context
	) {
		ItemStack stored = getStoredStack(stack);

		if (stored.isEmpty()) {
			tooltip.add(Text.translatable("item.forgero.smithing_tongs.empty").formatted(Formatting.DARK_GRAY));
			return;
		}

		tooltip.add(Text.translatable(
				"item.forgero.smithing_tongs.stored",
				stored.getName()
		).formatted(Formatting.GRAY));
	}
}
