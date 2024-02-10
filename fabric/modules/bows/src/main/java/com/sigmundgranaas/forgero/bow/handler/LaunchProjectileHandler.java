package com.sigmundgranaas.forgero.bow.handler;

import static com.sigmundgranaas.forgero.minecraft.common.feature.FeatureUtils.compute;
import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.FORGERO_IDENTIFIER;
import static net.minecraft.item.BowItem.TICKS_PER_SECOND;

import java.util.Optional;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.attribute.BaseAttribute;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
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
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class LaunchProjectileHandler implements StopHandler {
	private final StateService service;
	public static final String TYPE = "forgero:launch_projectile";
	public static final JsonBuilder<LaunchProjectileHandler> BUILDER = HandlerBuilder.fromObject(LaunchProjectileHandler.class, LaunchProjectileHandler::fromJson);

	public static final String DRAW_POWER_ATTRIBUTE_TYPE = "forgero:draw_power";
	public static final String DRAW_SPEED_ATTRIBUTE_TYPE = "forgero:draw_speed";

	private final Attribute power;
	private final float accuracy;
	private final boolean canBeCritical = true;
	private final Attribute drawTime;

	public LaunchProjectileHandler(Attribute power, float accuracy, Attribute drawTime) {
		this.power = power;
		this.accuracy = accuracy;
		this.drawTime = drawTime;
		this.service = StateService.INSTANCE;
	}

	public static LaunchProjectileHandler fromJson(JsonObject json) {
		float power = json.get("draw_power").getAsFloat();
		float accuracy = json.get("accuracy").getAsFloat();

		return new LaunchProjectileHandler(BaseAttribute.of(power, DRAW_POWER_ATTRIBUTE_TYPE), accuracy, BaseAttribute.of(0f, DRAW_SPEED_ATTRIBUTE_TYPE));
	}

	@Override
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		if (!(user instanceof PlayerEntity playerEntity)) {
			return;
		}

		ItemStack arrowStack = obtainArrowStack(playerEntity);
		if (arrowStack.isEmpty()) {
			return;
		}

		ArrowProperties arrowProps = ArrowProperties.fromItemStack(arrowStack, service);

		int useTime = stack.getMaxUseTime() - remainingUseTicks;

		if (useTime > 1) {
			removeItemFromState(stack, playerEntity, playerEntity.getActiveHand());
			fireArrow(world, playerEntity, arrowStack, useTime, arrowProps);
		}
	}

	private void removeItemFromState(ItemStack bow, PlayerEntity player, Hand hand) {
		Optional<State> arrow = StateService.INSTANCE.convert(obtainArrowStack(player));
		Optional<State> bowState = StateService.INSTANCE.convert(bow);
		if (arrow.isPresent() && bowState.isPresent() && bowState.get() instanceof Composite composite) {
			State converted = composite.removeUpgrade(arrow.get().identifier());
			ItemStack newBow = bow.copy();
			newBow.getOrCreateNbt().put(FORGERO_IDENTIFIER, StateEncoder.ENCODER.encode(converted));
			player.setStackInHand(hand, newBow);
		}
	}

	private void fireArrow(World world, PlayerEntity shooter, ItemStack arrowStack, int useTime, ArrowProperties arrowProps) {
		if (world.isClient) {
			return;
		}

		if (isCreativeMode(shooter)) {
			decrementArrowStack(shooter, arrowStack);
		}

		ArrowItem arrowItem = (arrowStack.getItem() instanceof ArrowItem) ? (ArrowItem) arrowStack.getItem() : (ArrowItem) Items.ARROW;
		PersistentProjectileEntity projectile = createProjectile(arrowItem, arrowStack, shooter, world);

		float pullProgress = getPullProgress(useTime, getDrawTime(shooter));

		if (pullProgress == 1.0F && canBeCritical) {
			projectile.setCritical(true);
		}

		LaunchParams params = createParams(pullProgress, arrowProps);
		applyVelocity(projectile, shooter, params);

		world.spawnEntity(projectile);
	}

	private boolean isCreativeMode(PlayerEntity player) {
		return player.getAbilities().creativeMode;
	}

	private void decrementArrowStack(PlayerEntity shooter, ItemStack arrowStack) {
		arrowStack.decrement(1);
		if (arrowStack.isEmpty()) {
			shooter.getInventory().removeOne(arrowStack);
		}
	}

	private float getDrawTime(PlayerEntity shooter) {
		float drawTime = compute(this.drawTime, shooter).asFloat();
		return Math.max(drawTime, 0.1f);
	}


	private PersistentProjectileEntity createProjectile(ArrowItem arrowItem, ItemStack arrowStack, PlayerEntity shooter, World world) {
		return arrowItem.createArrow(world, arrowStack, shooter);
	}

	private void applyVelocity(PersistentProjectileEntity projectile, PlayerEntity shooter, LaunchParams params) {
		projectile.setVelocity(shooter, shooter.getPitch(), shooter.getYaw(), params.roll(), params.power(), params.accuracy());
	}

	private LaunchParams createParams(float pullProgress, ArrowProperties arrowProps) {
		float power = pullProgress * this.power.getValue();
		float accuracy = 1.0F - arrowProps.getStability() * this.accuracy;
		float roll = 0.0F;
		return new LaunchParams(power, accuracy, roll);
	}

	private ItemStack obtainArrowStack(PlayerEntity playerEntity) {
		ItemStack arrowStack = playerEntity.getProjectileType(playerEntity.getMainHandStack());
		if (arrowStack.isEmpty() && !playerEntity.getAbilities().creativeMode) {
			arrowStack = new ItemStack(Items.ARROW);
		}
		return arrowStack;
	}

	/**
	 * Get the pull progress based on the draw time.
	 * Will return a float between 0 and 1. Where 1 is fully drawn.
	 *
	 * @param useTicks How many ticks have been used.
	 * @param drawTime The draw time, measured in seconds.
	 * @return The pull progress.
	 */
	public static float getPullProgress(int useTicks, float drawTime) {
		float f = (float) useTicks / (drawTime * TICKS_PER_SECOND);
		f = (f * f + f * 2.0F) / 3.0F;
		if (f > 1.0F) {
			f = 1.0F;
		}
		return f;
	}

	private record LaunchParams(float power, float accuracy, float roll) {
	}
}
