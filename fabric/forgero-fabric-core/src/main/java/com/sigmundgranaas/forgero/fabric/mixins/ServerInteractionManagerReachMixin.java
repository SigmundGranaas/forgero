package com.sigmundgranaas.forgero.fabric.mixins;

import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerInteractionManagerReachMixin {
	@Shadow
	@Final
	protected ServerPlayerEntity player;

	/**
	 * The purpose of this mixin is to cancel the default behaviour of checking if a player is trying to reach beyond 5 blocks or not.
	 * I assume this has been placed here for anti cheat measures, but this will only allow it to happen if the player actually have longer reach than 4.5.
	 */
	@Redirect(method = "processBlockBreakingAction",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;squaredDistanceTo(Lnet/minecraft/util/math/Vec3d;)D"))
	private double forgero$cancelDistanceCheckIfReachIsHigherThanVanilla(Vec3d instance, Vec3d vec, BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, int sequence) {
		if (StateService.INSTANCE.convert(player.getMainHandStack()).map(state -> ComputedAttribute.apply(state, "forgero:reach")).orElse(0f) > 4.5f) {
			return 0.0;
		}
		return instance.squaredDistanceTo(vec);
	}
}
