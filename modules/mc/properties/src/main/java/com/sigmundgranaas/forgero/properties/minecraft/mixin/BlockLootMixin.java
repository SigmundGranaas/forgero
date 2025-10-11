package com.sigmundgranaas.forgero.properties.minecraft.mixin;

import com.sigmundgranaas.forgero.properties.minecraft.loot.LootManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Block.class)
public abstract class BlockLootMixin {
	@Inject(
			method = "getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)Ljava/util/List;",
			at = @At("RETURN"),
			cancellable = true
	)
	private static void forgero$handleBlockLoot(BlockState state, ServerWorld world, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack stack, CallbackInfoReturnable<List<ItemStack>> cir) {
		LootContextParameterSet.Builder parameterSetBuilder = new LootContextParameterSet.Builder(world)
				.add(LootContextParameters.ORIGIN, pos.toCenterPos())
				.add(LootContextParameters.TOOL, stack)
				.add(LootContextParameters.BLOCK_STATE, state)
				.addOptional(LootContextParameters.BLOCK_ENTITY, blockEntity)
				.addOptional(LootContextParameters.THIS_ENTITY, entity);

		if (entity instanceof PlayerEntity player) {
			parameterSetBuilder.luck(player.getLuck());
		}

		LootContextParameterSet parameterSet = parameterSetBuilder.build(LootContextTypes.BLOCK);
		LootContext context = new LootContext.Builder(parameterSet).build(null);

		List<ItemStack> originalLoot = cir.getReturnValue();
		List<ItemStack> modifiedLoot = LootManager.handleBlockLoot(originalLoot, context);
		cir.setReturnValue(modifiedLoot);
	}
}
