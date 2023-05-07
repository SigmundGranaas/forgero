package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.FORGERO_IDENTIFIER;
import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.SOUL_IDENTIFIER;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.soul.Soul;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.SoulEncoder;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.SoulParser;
import com.sigmundgranaas.forgero.minecraft.common.mixins.OreBlockXp;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SoulHandler {
	private final ItemStack stack;

	private final Soul soul;

	public SoulHandler(ItemStack stack, Soul soul) {
		this.stack = stack;
		this.soul = soul;
	}

	public static Optional<SoulHandler> of(ItemStack stack) {
		return SoulParser.of(stack).map(value -> new SoulHandler(stack, value));
	}

	public void processMobKill(Entity entity, World world, PlayerEntity player) {
		if (entity instanceof LivingEntity livingEntity) {
			var handledSoul = soul.addXp(livingEntity.getXpToDrop() * 15);
			if (handledSoul.getLevel() > soul.getLevel()) {
				handleLevelUp(handledSoul, world, player);
			}
			handledSoul.trackMob(Registries.ENTITY_TYPE.getId(entity.getType()).toString(), 1);
			stack.getOrCreateNbt().getCompound(FORGERO_IDENTIFIER).put(SOUL_IDENTIFIER, SoulEncoder.ENCODER.encode(handledSoul));
		}

	}


	public void processBlockBreak(BlockPos pos, World world, PlayerEntity player) {
		BlockState state = world.getBlockState(pos);
		float xp = (state.getHardness(world, pos) * 1);
		if (state.getBlock() instanceof OreBlockXp ore) {
			xp = ore.getExperienceDropped().get(net.minecraft.util.math.random.Random.create()) * 15;
		}
		var handledSoul = soul.addXp(xp);
		if (handledSoul.getLevel() > soul.getLevel()) {
			handleLevelUp(handledSoul, world, player);
		}
		handledSoul.trackBlock(Registries.BLOCK.getId(state.getBlock()).toString(), 1);
		stack.getOrCreateNbt().getCompound(FORGERO_IDENTIFIER).put(SOUL_IDENTIFIER, SoulEncoder.ENCODER.encode(handledSoul));
	}

	private void handleLevelUp(Soul soul, World world, PlayerEntity player) {
		if (!world.isClient()) {
			player.sendMessage(Text.literal(String.format("%s leveled to level %s", soul.name(), soul.getLevel())));
		}
		world.sendEntityStatus(player, EntityStatuses.ENTITY_STATUS_SOUL_LEVEL_UP);
	}
}
