package com.sigmundgranaas.forgero.repairkit.item;

import java.util.Optional;

import com.sigmundgranaas.forgero.minecraft.common.item.StateItem;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class RepairKitItem extends Item {
	private final StateService service;

	public RepairKitItem(Settings settings, StateService service) {
		super(settings);
		this.service = service;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		Optional<ItemStack> tool = getToolStack(user);
		ItemStack repairKitStack = getRepairKitStack(user);
		if (tool.isPresent()) {
			var stack = tool.get();
			stack.setDamage(stack.getDamage() - stack.getMaxDamage() / 3);

			// Sound Effect
			world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_SMITHING_TABLE_USE, SoundCategory.PLAYERS, 1.0F, 1.0F);

			// Particle Effect
			double x = user.getX();
			double y = user.getY() + 1.5; // Spawn particles a bit above the player's feet
			double z = user.getZ();
			world.addParticle(ParticleTypes.ENCHANT, x, y, z, 0.5, 0.5, 0.5);

			return TypedActionResult.success(repairKitStack);
		}
		return TypedActionResult.fail(repairKitStack);
	}

	private ItemStack getRepairKitStack(PlayerEntity user) {
		if (user.getOffHandStack().getItem() instanceof RepairKitItem) {
			return user.getOffHandStack();
		}
		return user.getMainHandStack();
	}

	private Optional<ItemStack> getToolStack(PlayerEntity entity) {
		if (entity.getMainHandStack().getItem() instanceof StateItem) {
			return Optional.of(entity.getMainHandStack()).filter(stack -> stack.getItem().isDamageable());
		} else {
			return Optional.of(entity.getOffHandStack()).filter(stack -> stack.getItem().isDamageable());
		}
	}
}
