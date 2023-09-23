package com.sigmundgranaas.forgero.minecraft.common.match;

import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public record ItemWorldEntityKey(ItemStack stack, @Nullable World world, @Nullable LivingEntity entity) {
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ItemWorldEntityKey that = (ItemWorldEntityKey) o;
		return stack.equals(that.stack) &&
				Objects.equals(world, that.world) &&
				Objects.equals(entity, that.entity);
	}

	@Override
	public int hashCode() {
		if (stack.hasNbt()) {
			return Objects.hash(stack, world, entity);
		} else {
			return Objects.hash(stack.getItem(), world, entity);
		}
	}
}
