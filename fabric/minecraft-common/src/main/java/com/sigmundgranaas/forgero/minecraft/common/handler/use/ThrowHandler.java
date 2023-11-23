package com.sigmundgranaas.forgero.minecraft.common.handler.use;

import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.world.World;

public class ThrowHandler implements StopHandler {
	public static final String TYPE = "forgero:throw_trident";
	public static final ClassKey<ThrowHandler> KEY = new ClassKey<>(TYPE, ThrowHandler.class);
	public static final JsonBuilder<ThrowHandler> BUILDER = HandlerBuilder.fromObject(ThrowHandler.class, (json) -> new ThrowHandler());

	@Override
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		if (!world.isClient) {
			stack.damage(1, user, p -> p.sendToolBreakStatus(user.getActiveHand()));
			TridentEntity tridentEntity = new TridentEntity(world, user, stack.copy());
			tridentEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, 2.5f, 1.0f);
			world.spawnEntity(tridentEntity);
			world.playSoundFromEntity(null, tridentEntity, SoundEvents.ITEM_TRIDENT_THROW, SoundCategory.PLAYERS, 1.0f, 1.0f);
			stack.decrement(1);
		}
		if (user instanceof PlayerEntity playerEntity) {
			playerEntity.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
		}
	}
}
