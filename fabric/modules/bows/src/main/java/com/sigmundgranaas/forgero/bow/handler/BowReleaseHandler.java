package com.sigmundgranaas.forgero.bow.handler;

import com.sigmundgranaas.forgero.bow.item.BowProperties;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.StopHandler;

import com.sigmundgranaas.forgero.minecraft.common.item.ArrowProperties;

import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.StateEncoder;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.Optional;

import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.FORGERO_IDENTIFIER;
import static net.minecraft.item.BowItem.TICKS_PER_SECOND;

public class BowReleaseHandler implements StopHandler {
	private StateService service;

	@Override
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		if (!(user instanceof PlayerEntity playerEntity)) {
			return;
		}

		Optional<BowProperties> optionalBowProps = BowProperties.fromItemStack(stack, service);
		if (optionalBowProps.isEmpty()) {
			return;
		}
		BowProperties bowProps = optionalBowProps.get();

		ItemStack arrowStack = obtainArrowStack(playerEntity);
		if (arrowStack.isEmpty()) {
			return;
		}

		ArrowProperties arrowProps = ArrowProperties.fromItemStack(arrowStack, service);

		int useTime = stack.getMaxUseTime() - remainingUseTicks;
		float pullProgress = getPullProgress(useTime, bowProps.getFlexibility());

		removeItemFromState(stack, playerEntity, playerEntity.getActiveHand());
		if ((double) pullProgress >= 0.1) {
			fireArrow(stack, world, playerEntity, arrowStack, pullProgress, bowProps, arrowProps);
		}
	}

	private void fireArrow(ItemStack bowStack, World world, PlayerEntity shooter, ItemStack arrowStack, float pullProgress, BowProperties bowProps, ArrowProperties arrowProps) {
		if (world.isClient) {
			return;
		}

		ArrowItem arrowItem = (arrowStack.getItem() instanceof ArrowItem) ? (ArrowItem) arrowStack.getItem() : (ArrowItem) Items.ARROW;
		PersistentProjectileEntity projectile = createProjectile(arrowItem, arrowStack, shooter, world, bowProps, arrowProps, pullProgress);

		if (pullProgress == 1.0F) {
			projectile.setCritical(true);
		}

		bowStack.damage(1, shooter, (playerEntity) -> playerEntity.sendToolBreakStatus(shooter.getActiveHand()));

		boolean isCreativeMode = shooter.getAbilities().creativeMode;
		if (!isCreativeMode) {
			arrowStack.decrement(1);
			if (arrowStack.isEmpty()) {
				shooter.getInventory().removeOne(arrowStack);
			}
		}

		world.spawnEntity(projectile);
		world.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F / (world.getRandom().nextFloat() * 0.4F + 1.2F) + pullProgress * 0.5F);
		shooter.incrementStat(Stats.USED.getOrCreateStat(bowStack.getItem()));
	}


	private PersistentProjectileEntity createProjectile(ArrowItem arrowItem, ItemStack arrowStack, PlayerEntity shooter, World world, BowProperties bowProps, ArrowProperties arrowProps, float pullProgress) {
		PersistentProjectileEntity projectile = arrowItem.createArrow(world, arrowStack, shooter);
		float launchVelocity = pullProgress * bowProps.getForce();
		float inaccuracy = 1.0F - arrowProps.getStability();
		projectile.setVelocity(shooter, shooter.getPitch(), shooter.getYaw(), 0.0F, launchVelocity * 3.0F, inaccuracy);
		return projectile;
	}

	private void addArrowToState(ItemStack bow, PlayerEntity player, Hand hand) {
		Optional<State> arrow = StateService.INSTANCE.convert(obtainArrowStack(player));
		Optional<State> bowState = StateService.INSTANCE.convert(bow);
		if (arrow.isPresent() && bowState.isPresent() && bowState.get() instanceof Composite composite) {
			State converted = composite.upgrade(arrow.get());
			ItemStack newBow = bow.copy();
			newBow.getOrCreateNbt().put(FORGERO_IDENTIFIER, StateEncoder.ENCODER.encode(converted));
			player.setStackInHand(hand, newBow);
		}
	}

	private void removeItemFromState(ItemStack bow, PlayerEntity player, Hand hand) {
		Optional<State> arrow = StateService.INSTANCE.convert(obtainArrowStack(player));
		Optional<State> bowState = StateService.INSTANCE.convert(bow);
		if (arrow.isPresent() &&  bowState.isPresent() && bowState.get() instanceof Composite composite) {
			State converted = composite.removeUpgrade(arrow.get().identifier());
			ItemStack newBow = bow.copy();
			newBow.getOrCreateNbt().put(FORGERO_IDENTIFIER, StateEncoder.ENCODER.encode(converted));
			player.setStackInHand(hand, newBow);
		}
	}

	private ItemStack obtainArrowStack(PlayerEntity playerEntity) {
		ItemStack arrowStack = playerEntity.getProjectileType(playerEntity.getMainHandStack());
		if (arrowStack.isEmpty() && !playerEntity.getAbilities().creativeMode) {
			arrowStack = new ItemStack(Items.ARROW);
		}
		return arrowStack;
	}

	public static float getPullProgress(int useTicks, float bowFlexibility) {
		float f = (float) useTicks / (bowFlexibility * TICKS_PER_SECOND);
		f = (f * f + f * 2.0F) / 3.0F;
		if (f > 1.0F) {
			f = 1.0F;
		}
		return f;
	}
}
