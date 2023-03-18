package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import com.sigmundgranaas.forgero.core.property.v2.RunnableHandler;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.minecraft.common.conversion.StateConverter;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class TotemEffectHandler implements RunnableHandler {

	private final MinecraftClient client;

	private final PlayerEntity entity;

	private final World world;

	public TotemEffectHandler(MinecraftClient client, PlayerEntity entity, World world) {
		this.client = client;
		this.entity = entity;
		this.world = world;
	}

	public static TotemEffectHandler of(MinecraftClient client, PlayerEntity entity, World world) {
		return new TotemEffectHandler(client, entity, world);
	}

	@Override
	public String type() {
		return "TOTEM_EFFECT";
	}

	@Override
	public void run() {
		this.client.particleManager.addEmitter(entity, getTotemParticle(), 30);
		this.world.playSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_TOTEM_USE, entity.getSoundCategory(), 1.0f, 1.0f, false);
		if (entity == this.client.player) {
			this.client.gameRenderer.showFloatingItem(getActiveTotem());
		}
	}


	private ItemStack getActiveTotem() {
		for (Hand hand : Hand.values()) {
			ItemStack itemStack = entity.getStackInHand(hand);
			Item totem = Registry.ITEM.get(new Identifier("forgero:soul-totem"));
			if (StateConverter.of(itemStack).filter(state -> state instanceof Composite composite && composite.has("forgero:soul-totem").isPresent()).isPresent()) {
				itemStack = new ItemStack(totem);
				if (!itemStack.isEmpty()) {
					return itemStack;
				}
			} else if (totem != Items.AIR && itemStack.isOf(totem)) {
				return itemStack;
			}
		}
		return new ItemStack(Items.TOTEM_OF_UNDYING);
	}

	public DefaultParticleType getTotemParticle() {
		return ParticleTypes.TOTEM_OF_UNDYING;
	}
}
