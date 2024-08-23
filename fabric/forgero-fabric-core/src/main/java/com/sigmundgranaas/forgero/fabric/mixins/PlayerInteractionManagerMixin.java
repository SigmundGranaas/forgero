package com.sigmundgranaas.forgero.fabric.mixins;

import com.sigmundgranaas.forgero.minecraft.common.toolhandler.PropertyHelper;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.SoulHandler;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.block.ToolBlockHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

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
	public void forgero$calcBlockBreakingDelta(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		if (this.currentBreakingProgress >= 1.0F && client.player != null && client.world != null) {
			var soulHandler = SoulHandler.of(client.player.getMainHandStack());
			soulHandler.ifPresent(soul -> soul.processBlockBreak(pos, client.world, client.player));
			PropertyHelper.ofPlayerHands(client.player)
					.flatMap(container -> ToolBlockHandler.of(container, pos, client.player))
					.ifPresent(handler -> handler.handleExceptOrigin(info -> {
						soulHandler.ifPresent(soul -> soul.processBlockBreak(info, client.world, client.player));
						this.breakBlock(info);
					}).cleanUp());
		}
	}
}
