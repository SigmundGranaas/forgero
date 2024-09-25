package com.sigmundgranaas.forgero.bow.handler;

import static com.sigmundgranaas.forgero.bow.handler.ChargeCrossbowHandler.isCharged;
import static com.sigmundgranaas.forgero.bow.handler.ChargeCrossbowHandler.isLoaded;
import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.FORGERO_IDENTIFIER;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.StopHandler;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.StateEncoder;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class LoadCrossBowArrowHandler implements StopHandler {
	public static final String TYPE = "forgero:load_crossbow_arrow";
	public static final JsonBuilder<LoadCrossBowArrowHandler> BUILDER = HandlerBuilder.fromObject(LoadCrossBowArrowHandler.class, (json) -> new LoadCrossBowArrowHandler());

	@Override
	public void stoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		boolean hasArrow = !user.getProjectileType(stack).isEmpty();
		if (hasArrow && isCharged(stack) && isLoaded(stack)) {
			addArrowToState(stack, user, user.getActiveHand());
			SoundCategory soundCategory = user instanceof PlayerEntity ? SoundCategory.PLAYERS : SoundCategory.HOSTILE;
			world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_CROSSBOW_LOADING_END, soundCategory, 1.0f, 1.0f / (world.getRandom().nextFloat() * 0.5f + 1.0f) + 0.2f);
		}
	}

	private void addArrowToState(ItemStack bow, LivingEntity player, Hand hand) {
		Optional<State> arrow = StateService.INSTANCE.convert(obtainArrowStack(player));
		Optional<State> bowState = StateService.INSTANCE.convert(bow);
		if (arrow.isPresent() && bowState.isPresent() && bowState.get() instanceof Composite composite) {
			State converted = composite.upgrade(arrow.get());
			ItemStack newBow = bow.copy();
			newBow.getOrCreateNbt().put(FORGERO_IDENTIFIER, StateEncoder.ENCODER.encode(converted));
			player.setStackInHand(hand, newBow);
		}
	}

	private ItemStack obtainArrowStack(LivingEntity entity) {
		ItemStack arrowStack = entity.getProjectileType(entity.getMainHandStack());
		if (arrowStack.isEmpty() && entity instanceof PlayerEntity playerEntity && !playerEntity.getAbilities().creativeMode) {
			arrowStack = new ItemStack(Items.ARROW);
		}
		return arrowStack;
	}
}
