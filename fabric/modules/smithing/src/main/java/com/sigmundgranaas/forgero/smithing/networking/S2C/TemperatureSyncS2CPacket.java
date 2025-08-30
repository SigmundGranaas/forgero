package com.sigmundgranaas.forgero.smithing.networking.S2C;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;

public class TemperatureSyncS2CPacket {
	public static final Identifier ID = new Identifier("forgero", "temperature_sync");
	private final int entityId;
	private final int temperature;

	public TemperatureSyncS2CPacket(int entityId, int temperature) {
		this.entityId = entityId;
		this.temperature = temperature;
	}

	public int getEntityId() {
		return entityId;
	}

	public int getTemperature() {
		return temperature;
	}

	// Server-side: send packet to a specific player
	public static void sendToClient(ServerPlayerEntity player, Entity entity, int temperature) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeInt(entity.getId());
		buf.writeInt(temperature);
		ServerPlayNetworking.send(player, ID, buf);
	}

	// Send to all tracking players
	public static void sendToClient(ItemEntity entity, int temperature) {
		if (!(entity.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld)) return;
		for (ServerPlayerEntity player : serverWorld.getPlayers()) {
			sendToClient(player, entity, temperature);
		}
	}

	// Decode from buffer
	public static TemperatureSyncS2CPacket decode(PacketByteBuf buf) {
		int entityId = buf.readInt();
		int temperature = buf.readInt();
		return new TemperatureSyncS2CPacket(entityId, temperature);
	}

	// Encode to buffer
	public void encode(PacketByteBuf buf) {
		buf.writeInt(entityId);
		buf.writeInt(temperature);
	}

	// Client-side handler
	public static void handle(PacketByteBuf buf) {
		TemperatureSyncS2CPacket packet = decode(buf);
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world == null) return;

		client.execute(() -> {
			Entity entity = client.world.getEntityById(packet.entityId);
			if (entity instanceof ItemEntity itemEntity) {
				// Store temperature in NBT or client cache
				itemEntity.getStack().getOrCreateNbt().putInt("forgero_temperature", packet.temperature);
			}
		});
	}

	// Handler for Fabric's PlayChannelHandler
	public static void receive(net.minecraft.client.MinecraftClient client, net.minecraft.client.network.ClientPlayNetworkHandler handler, net.minecraft.network.PacketByteBuf buf, PacketSender responseSender) {
		handle(buf);
	}
}
