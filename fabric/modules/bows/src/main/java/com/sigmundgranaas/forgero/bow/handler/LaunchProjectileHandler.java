package com.sigmundgranaas.forgero.bow.handler;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.attribute.BaseAttribute;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttributeBuilder;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Accuracy;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.feature.FeatureUtils;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.StopHandler;
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
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.FORGERO_IDENTIFIER;
import static com.sigmundgranaas.forgero.minecraft.common.utils.PropertyUtils.container;
import static net.minecraft.item.BowItem.TICKS_PER_SECOND;

public class LaunchProjectileHandler implements StopHandler {
	private final StateService service;
	public static final String TYPE = "forgero:launch_projectile";
	public static final JsonBuilder<LaunchProjectileHandler> BUILDER = HandlerBuilder.fromObject(LaunchProjectileHandler.class, LaunchProjectileHandler::fromJson);

	public static final String DRAW_POWER_ATTRIBUTE_TYPE = "forgero:draw_power";
	public static final String DRAW_SPEED_ATTRIBUTE_TYPE = "forgero:draw_speed";

	public static final PropertyContainer DEFAULT_ARROW = PropertyContainer.of(BaseAttribute.of(50f, Accuracy.KEY));
	public static final PropertyContainer DEFAULT_BOW = PropertyContainer.of(List.of(BaseAttribute.of(2f, DRAW_POWER_ATTRIBUTE_TYPE), BaseAttribute.of(1f, DRAW_SPEED_ATTRIBUTE_TYPE)));

	private final Attribute power;
	private final Attribute accuracy;
	private final boolean canBeCritical = true;

	public LaunchProjectileHandler(Attribute power, Attribute accuracy) {
		this.power = power;
		this.accuracy = accuracy;
		this.service = StateService.INSTANCE;
	}

	public static LaunchProjectileHandler fromJson(JsonObject json) {
		Attribute power = FeatureUtils.of(json, "draw_power", DRAW_POWER_ATTRIBUTE_TYPE, 0);
		Attribute accuracy = FeatureUtils.of(json, "accuracy", Accuracy.KEY, 0);

		return new LaunchProjectileHandler(power, accuracy);
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

		int useTime = stack.getMaxUseTime() - remainingUseTicks;

		if (useTime > 1) {
			fireArrow(world, playerEntity, arrowStack, useTime, stack.copyWithCount(1));
			removeItemFromState(stack, playerEntity, playerEntity.getActiveHand());
			world.playSound(
					null,
					playerEntity.getX(),
					playerEntity.getY(),
					playerEntity.getZ(),
					SoundEvents.ENTITY_ARROW_SHOOT,
					SoundCategory.PLAYERS,
					1.0F,
					1.0F / (world.getRandom().nextFloat() * 0.4F + 1.2F) + ((float) useTime / 10) * 0.5F
			);
			if (!isCreativeMode(playerEntity)) {
				decrementArrowStack(playerEntity, arrowStack);
			}
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

			if (!player.isCreative()) {
				newBow.damage(1, player, (p) -> p.sendToolBreakStatus(hand));
			}
		}
	}

	private void fireArrow(World world, PlayerEntity shooter, ItemStack arrowStack, int useTime, ItemStack bow) {
		if (world.isClient) {
			return;
		}

		ArrowItem arrowItem = (arrowStack.getItem() instanceof ArrowItem) ? (ArrowItem) arrowStack.getItem() : (ArrowItem) Items.ARROW;

		PersistentProjectileEntity projectile = createProjectile(arrowItem, arrowStack, shooter, world);

		float pullProgress = getPullProgress(useTime, getDrawTime(shooter));

		if (pullProgress == 1.0F && canBeCritical) {
			projectile.setCritical(true);
		}

		LaunchParams params = createParams(pullProgress, bow);
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
		float drawTime = ComputedAttributeBuilder.of(DRAW_SPEED_ATTRIBUTE_TYPE)
				.addSource(container(shooter))
				.build()
				.asFloat();

		return Math.max(drawTime, 0.1f);
	}


	private PersistentProjectileEntity createProjectile(ArrowItem arrowItem, ItemStack arrowStack, PlayerEntity shooter, World world) {
		return arrowItem.createArrow(world, arrowStack, shooter);
	}

	private void applyVelocity(PersistentProjectileEntity projectile, PlayerEntity shooter, LaunchParams params) {
		projectile.setVelocity(shooter, shooter.getPitch(), shooter.getYaw(), params.roll(), params.power(), accuracyToDivergence(params.accuracy()));
	}


	/**
	 * Converts an accuracy value to a divergence value using an exponential decay function.
	 *
	 * @param accuracy The accuracy value, ranging from 0 to 100 or higher.
	 * @return The calculated divergence value.
	 */
	public static float accuracyToDivergence(float accuracy) {
		double a = 20; // Initial divergence when accuracy is 0
		double b = -0.045;
		double pow = 1.1;

		// Calculate divergence
		double divergence = a * Math.exp(b * Math.pow(accuracy, pow));

		// Divergence should never be negative
		return (float) Math.max(divergence, 0);
	}

	private LaunchParams createParams(float pullProgress, ItemStack bow) {
		float power = ComputedAttributeBuilder.of(DRAW_POWER_ATTRIBUTE_TYPE)
				.addSource(this.power)
				.addSource(service.convert(bow).map(PropertyContainer.class::cast).orElse(DEFAULT_BOW))
				.build()
				.asFloat();

		float accuracy = ComputedAttributeBuilder.of(Accuracy.KEY)
				.addSource(this.accuracy)
				.addSource(service.convert(bow).map(PropertyContainer.class::cast).orElse(DEFAULT_BOW))
				.build()
				.asFloat();

		float roll = 0.0F;
		return new LaunchParams(power * pullProgress, accuracy, roll);
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
