package com.sigmundgranaas.forgero.properties.minecraft.mixin;

import com.sigmundgranaas.forgero.properties.minecraft.loot.LootManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(LivingEntity.class)
public abstract class LivingEntityLootMixin extends Entity {
	// Shadow the protected field to gain access
	@Shadow
	@Nullable
	protected PlayerEntity attackingPlayer;

	// Shadow methods from both LivingEntity and its superclass (Entity)
	@Shadow
	public abstract Identifier getLootTable();

	@Shadow
	protected abstract boolean shouldDropLoot();

	@Shadow
	public abstract long getLootTableSeed();

	// Constructor required by the compiler because we are extending Entity
	public LivingEntityLootMixin(net.minecraft.entity.EntityType<?> type, net.minecraft.world.World world) {
		super(type, world);
	}

	@Inject(method = "dropLoot", at = @At("HEAD"), cancellable = true)
	private void forgero$handleAndModifyEntityLoot(DamageSource damageSource, boolean causedByPlayer, CallbackInfo ci) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (self.getWorld().isClient()) {
			return;
		}

		// Replicate vanilla conditions from drop() and dropLoot()
		if (!this.shouldDropLoot() || !self.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
			return;
		}

		Identifier identifier = this.getLootTable();
		LootTable lootTable = self.getWorld().getServer().getLootManager().getLootTable(identifier);

		// Replicate vanilla LootContext creation
		LootContextParameterSet.Builder builder = new LootContextParameterSet.Builder((ServerWorld) self.getWorld())
				.add(LootContextParameters.THIS_ENTITY, self)
				.add(LootContextParameters.ORIGIN, self.getPos())
				.add(LootContextParameters.DAMAGE_SOURCE, damageSource)
				.addOptional(LootContextParameters.KILLER_ENTITY, damageSource.getAttacker())
				.addOptional(LootContextParameters.DIRECT_KILLER_ENTITY, damageSource.getSource());

		// Use the shadowed attackingPlayer field
		if (causedByPlayer && this.attackingPlayer != null) {
			builder = builder.add(LootContextParameters.LAST_DAMAGE_PLAYER, this.attackingPlayer).luck(this.attackingPlayer.getLuck());
		}

		LootContextParameterSet parameterSet = builder.build(LootContextTypes.ENTITY);
		LootContext context = new LootContext.Builder(parameterSet).random(this.getLootTableSeed()).build(null);

		// Generate loot into a temporary list
		List<ItemStack> originalLoot = new ArrayList<>();
		lootTable.generateLoot(parameterSet, this.getLootTableSeed(), originalLoot::add);

		// Pass the original loot to our manager for modification
		List<ItemStack> modifiedLoot = LootManager.handleEntityLoot(originalLoot, context);

		// Drop the final, modified loot. dropStack is available because we extend Entity.
		modifiedLoot.forEach(this::dropStack);

		// Cancel the original method to prevent vanilla from dropping the loot again
		ci.cancel();
	}
}
