package com.sigmundgranaas.forgero.fabric.mixins;

import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.*;

import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.feature.onhit.block.OnHitBlockFeatureExecutor;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.PropertyHelper;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.SoulHandler;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.block.ToolBlockHandler;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class PlayerServerInteractionManagerMixin {

	@Shadow
	@Final
	protected ServerPlayerEntity player;

	@Shadow
	protected ServerWorld world;

	@Shadow
	public abstract void finishMining(BlockPos pos, int sequence, String reason);

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;finishMining(Lnet/minecraft/util/math/BlockPos;ILjava/lang/String;)V"), method = "processBlockBreakingAction")
	public void forgero$processBlockBreakingAction(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
		var soulHandler = SoulHandler.of(player.getMainHandStack());
		soulHandler.ifPresent(soul -> soul.processBlockBreak(pos, world, player));
		PropertyHelper.ofPlayerHands(player)
				.flatMap(container -> ToolBlockHandler.of(container, pos, player))
				.ifPresent(handler -> handler.handleExceptOrigin(info -> {
					soulHandler.ifPresent(soul -> soul.processBlockBreak(info, world, player));
					this.finishMining(info, sequence, "destroyed");
				}).cleanUp());
	}

	@Inject(method = "processBlockBreakingAction", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/server/world/ServerWorld;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;",
			opcode = Opcodes.INVOKEVIRTUAL, shift = At.Shift.AFTER))
	private void forgero$onStartMining(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
		ServerPlayerEntity entity = player;

		MatchContext context = MatchContext.of()
				.put(ENTITY, entity)
				.put(WORLD, entity.getWorld())
				.put(BLOCK_TARGET, pos);

		OnHitBlockFeatureExecutor.initFromMainHandStack(player, pos)
				.executeIfNotCoolingDown(context);
	}
}
