package com.sigmundgranaas.forgero.smithing.client;

import com.sigmundgranaas.forgero.smithing.item.custom.SmithingTongsItem;
import com.sigmundgranaas.forgero.smithing.particle.WorkableTemperatureParticleEffects;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class SmithingTongsWorkableParticleHandler {
	private static final double HAND_FORWARD_OFFSET = 0.45D;
	private static final double HAND_SIDE_OFFSET = 0.34D;
	private static final double HAND_VERTICAL_OFFSET = -0.42D;
	private static final double PARTICLE_SPREAD = 0.08D;
	private static final double MIN_SIDE_LENGTH_SQUARED = 1.0E-4D;

	private SmithingTongsWorkableParticleHandler() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(SmithingTongsWorkableParticleHandler::tick);
	}

	private static void tick(MinecraftClient client) {
		if (client.world == null || client.player == null) {
			return;
		}

		spawnForHand(client, Hand.MAIN_HAND);
		spawnForHand(client, Hand.OFF_HAND);
	}

	private static void spawnForHand(MinecraftClient client, Hand hand) {
		ClientPlayerEntity player = client.player;
		ItemStack tongsStack = player.getStackInHand(hand);

		if (!(tongsStack.getItem() instanceof SmithingTongsItem)) {
			return;
		}

		ItemStack stored = SmithingTongsItem.getStoredStack(tongsStack);
		Vec3d particlePos = heldItemParticlePos(player, hand);

		WorkableTemperatureParticleEffects.spawnIfWorkable(
				client.world,
				stored,
				particlePos.x,
				particlePos.y,
				particlePos.z,
				PARTICLE_SPREAD
		);
	}

	private static Vec3d heldItemParticlePos(ClientPlayerEntity player, Hand hand) {
		Vec3d look = player.getRotationVec(1.0F).normalize();
		Vec3d side = look.crossProduct(new Vec3d(0.0D, 1.0D, 0.0D));

		if (side.lengthSquared() < MIN_SIDE_LENGTH_SQUARED) {
			float yawRadians = player.getYaw() * ((float) Math.PI / 180.0F);
			side = new Vec3d(-Math.cos(yawRadians), 0.0D, -Math.sin(yawRadians));
		} else {
			side = side.normalize();
		}

		int handSign = isRightHand(player, hand) ? 1 : -1;

		return player.getEyePos()
				.add(look.multiply(HAND_FORWARD_OFFSET))
				.add(side.multiply(HAND_SIDE_OFFSET * handSign))
				.add(0.0D, HAND_VERTICAL_OFFSET, 0.0D);
	}

	private static boolean isRightHand(ClientPlayerEntity player, Hand hand) {
		boolean mainHandIsRight = player.getMainArm() == Arm.RIGHT;
		return hand == Hand.MAIN_HAND ? mainHandIsRight : !mainHandIsRight;
	}
}
