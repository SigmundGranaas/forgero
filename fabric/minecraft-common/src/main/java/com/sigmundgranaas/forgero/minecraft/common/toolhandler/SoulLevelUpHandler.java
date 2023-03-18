package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import com.sigmundgranaas.forgero.core.property.v2.RunnableHandler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class SoulLevelUpHandler implements RunnableHandler {

	private final MinecraftClient client;

	private final PlayerEntity entity;

	private final World world;

	public SoulLevelUpHandler(MinecraftClient client, PlayerEntity entity, World world) {
		this.client = client;
		this.entity = entity;
		this.world = world;
	}

	public static SoulLevelUpHandler of(MinecraftClient client, PlayerEntity entity, World world) {
		return new SoulLevelUpHandler(client, entity, world);
	}

	@Override
	public String type() {
		return "SOUL_LEVEL_UP_EFFECT";
	}

	@Override
	public void run() {
		this.client.particleManager.addEmitter(entity, ParticleTypes.TOTEM_OF_UNDYING, 30);
		this.world.playSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_TOTEM_USE, entity.getSoundCategory(), 1.0f, 1.0f, false);
	}
}
