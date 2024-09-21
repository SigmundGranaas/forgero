package com.sigmundgranaas.forgero.fabric.mixins;

import com.sigmundgranaas.forgero.service.StateService;
import com.sigmundgranaas.forgero.toolhandler.UndyingHandler;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityUndyingMixin {

	@Shadow
	public abstract ItemStack getStackInHand(Hand hand);

	@Shadow
	public abstract void setHealth(float health);

	@Shadow
	public abstract boolean clearStatusEffects();

	@Shadow
	public abstract boolean addStatusEffect(StatusEffectInstance effect);

	@Shadow
	public abstract boolean shouldDropXp();

	@Shadow
	public abstract void setStackInHand(Hand hand, ItemStack stack);

	@Inject(method = "tryUseTotem", at = @At("HEAD"), cancellable = true)
	public void undyingStateItem(DamageSource source, CallbackInfoReturnable<Boolean> cir) {

			Hand[] hands = Hand.values();
			for (Hand hand : hands) {
				ItemStack stack = this.getStackInHand(hand);
				var handler = StateService.INSTANCE.convert(stack)
						.filter(state -> !state.identifier().contains("totem"))
						.flatMap(container -> UndyingHandler.of(container, stack));
				if (handler.isPresent()) {
					executeUndyingEffect(stack);
					handler.get().handle();
					this.setStackInHand(hand, stack.copy());
					cir.setReturnValue(true);
					break;
				}
			}
	}

	private void executeUndyingEffect(ItemStack stack) {
		LivingEntity entity = ((LivingEntity) (Object) this);
		if (entity instanceof ServerPlayerEntity serverPlayerEntity) {
			serverPlayerEntity.incrementStat(Stats.USED.getOrCreateStat(Items.TOTEM_OF_UNDYING));
			Criteria.USED_TOTEM.trigger(serverPlayerEntity, stack);
		}
		stack.setDamage(stack.getMaxDamage() / 2);
		this.setHealth(1.0F);
		this.clearStatusEffects();
		this.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 900, 1));
		this.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 100, 1));
		this.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 800, 0));
		entity.getWorld().sendEntityStatus(entity, (byte) 35);
	}
}
