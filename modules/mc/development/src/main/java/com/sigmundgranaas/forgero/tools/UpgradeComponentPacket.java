package com.sigmundgranaas.forgero.tools;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

/**
 * A custom packet sent from the client to the server to request an update
 * to a component-based item's NBT data after being modified in the upgrade screen.
 */
public class UpgradeComponentPacket {
	public static final Identifier ID = new Identifier("forgero", "upgrade_component");

	/**
	 * Writes the hand and NBT data to a buffer for sending.
	 */
	public static PacketByteBuf write(Hand hand, NbtCompound componentNbt) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeEnumConstant(hand);
		buf.writeNbt(componentNbt);
		return buf;
	}

	/**
	 * Reads the hand from a received buffer.
	 */
	public static Hand readHand(PacketByteBuf buf) {
		return buf.readEnumConstant(Hand.class);
	}

	/**
	 * Reads the NBT data from a received buffer.
	 */
	public static NbtCompound readNbt(PacketByteBuf buf) {
		return buf.readNbt();
	}
}
