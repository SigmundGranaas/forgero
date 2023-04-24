package com.sigmundgranaas.forgero.quilt.mixins;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.item.StateItem;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.MagneticHandler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMagneticMixin extends Entity {


	public LivingEntityMagneticMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@Shadow
	public abstract ItemStack getMainHandStack();

	@Inject(method = "baseTick", at = @At("RETURN"))
	public void magneticTickInject(CallbackInfo callbackInfo) {
		if (this.getMainHandStack().getItem() instanceof StateItem stateItem) {
			State state = stateItem.dynamicState(getMainHandStack());
			MagneticHandler.of(state, this).ifPresent(Runnable::run);
		}
	}
}
