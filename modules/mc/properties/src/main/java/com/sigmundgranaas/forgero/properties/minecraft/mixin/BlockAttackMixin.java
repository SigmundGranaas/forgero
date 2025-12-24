package com.sigmundgranaas.forgero.properties.minecraft.mixin;

import com.sigmundgranaas.forgero.properties.minecraft.onhitblock.OnHitBlockManager;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to intercept block attack events for OnHitBlock property.
 * Triggers when a player attacks/hits a block (left-click on block).
 */
@Mixin(ServerPlayerInteractionManager.class)
public abstract class BlockAttackMixin {

	@Shadow
	@Final
	protected ServerPlayerEntity player;

	/**
	 * Inject into attackBlock to trigger OnHitBlock effects when a player starts attacking a block.
	 */
	@Inject(
			method = "processBlockBreakingAction",
			at = @At("HEAD")
	)
	private void forgero$onAttackBlock(BlockPos pos, net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
		if (action == net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
			if (player != null && !player.getWorld().isClient()) {
				ItemStack stack = player.getMainHandStack();
				if (!stack.isEmpty()) {
					OnHitBlockManager.handleOnHitBlock(stack, player.getWorld(), player, pos);
				}
			}
		}
	}
}
