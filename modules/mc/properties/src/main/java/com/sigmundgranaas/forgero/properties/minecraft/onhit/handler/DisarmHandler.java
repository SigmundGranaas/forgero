package com.sigmundgranaas.forgero.properties.minecraft.onhit.handler;

import com.mojang.serialization.Codec;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

public record DisarmHandler() implements OnHitHandler {
	public static final String TYPE = "forgero:disarm";
	public static final DisarmHandler INSTANCE = new DisarmHandler();
	public static final Codec<DisarmHandler> CODEC = Codec.unit(INSTANCE);

	@Override
	public void onHit(Entity source, Entity target) {
		if (!target.getWorld().isClient && target instanceof LivingEntity livingTarget) {
			ItemStack mainHandStack = livingTarget.getMainHandStack();
			if (!mainHandStack.isEmpty()) {
				ItemEntity itemEntity = new ItemEntity(target.getWorld(), target.getX(), target.getEyeY(), target.getZ(), mainHandStack.copy());

				Random random = target.getWorld().getRandom();
				float f = random.nextFloat() * 0.1F;
				float g = random.nextFloat() * 0.1F;
				itemEntity.setVelocity(-MathHelper.sin(g) * f, 0.2, MathHelper.cos(g) * f);
				target.getWorld().spawnEntity(itemEntity);

				livingTarget.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
			}
		}
	}

	@Override
	public String type() {
		return TYPE;
	}
}
