package com.sigmundgranaas.forgero.properties.minecraft.mixin;

import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.BlockBreakingManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class PlayerInteractionManagerMixin {
	@Shadow
	@Final
	private MinecraftClient client;

	@Shadow
	private float currentBreakingProgress;

	@Shadow
	public abstract boolean breakBlock(BlockPos pos);

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;sendSequencedPacket(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/network/SequencedPacketCreator;)V", shift = At.Shift.AFTER), method = "updateBlockBreakingProgress")
	public void forgero$breakExtraBlocks(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		if (this.currentBreakingProgress >= 1.0F && client.player != null) {
			BlockBreakingManager.getBreakingResult(client.player, pos)
					.ifPresent(result -> {
						result.getAoe().forEach(this::breakBlock);
						BlockBreakingManager.clearCache(client.player, pos);
					});
		}
	}
}
