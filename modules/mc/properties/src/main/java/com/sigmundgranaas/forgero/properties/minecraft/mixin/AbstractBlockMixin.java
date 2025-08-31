package com.sigmundgranaas.forgero.properties.minecraft.mixin;

import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.BlockBreakingManager;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {
	@Inject(at = @At("RETURN"), method = "calcBlockBreakingDelta", cancellable = true)
	public void calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
		BlockBreakingManager.getBreakingResult(player, pos)
				.map(BlockBreakingManager.BlockBreakingResult::speed)
				.map(speed -> speed == -1.0f ? 0f : speed)
				.ifPresent(cir::setReturnValue);
	}
}
