package com.sigmundgranaas.forgero.fabric.mixins;

import com.sigmundgranaas.forgero.minecraft.common.toolhandler.SoulHandler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

	public LivingEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@Shadow
	public abstract boolean isDead();

	@Inject(method = "onDeath", at = @At("RETURN"))
	public void onDeathSoulHandler(DamageSource damageSource, CallbackInfo callbackInfo) {
		var source = damageSource.getSource();
		if (source instanceof PlayerEntity player && isDead()) {
			SoulHandler.of(player.getMainHandStack()).ifPresent(handler -> handler.processMobKill(this, getWorld(), player));
		}
	}
}
