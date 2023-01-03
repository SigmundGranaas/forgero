package com.sigmundgranaas.forgero.fabric.mixins;

import com.sigmundgranaas.forgero.core.property.passive.StaticPassiveType;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.minecraft.common.conversion.StateConverter;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.StateEncoder;
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

import java.util.Optional;

import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.FORGERO_IDENTIFIER;

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

    @Inject(method = "tryUseTotem", at = @At("HEAD"), cancellable = true)
    public void undyingStateItem(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (!source.isOutOfWorld()) {
            Hand[] hands = Hand.values();
            Optional<Composite> undyingState = Optional.empty();
            ItemStack handItem = ItemStack.EMPTY;
            for (Hand hand : hands) {
                handItem = this.getStackInHand(hand);
                var stateOpt = StateConverter.of(handItem);
                if (stateOpt.isPresent() && stateOpt.get() instanceof Composite composite) {
                    if (composite.stream().getStaticPassiveProperties().anyMatch(prop -> prop.getStaticType() == StaticPassiveType.UNDYING)) {
                        undyingState = Optional.of(composite);
                        var newComposite = composite.removeUpgrade("undying-totem");
                        handItem.getOrCreateNbt().put(FORGERO_IDENTIFIER, StateEncoder.ENCODER.encode(newComposite));
                        break;
                    }
                }
            }

            if (undyingState.isPresent()) {
                LivingEntity entity = ((LivingEntity) (Object) this);

                if (entity instanceof ServerPlayerEntity serverPlayerEntity) {
                    serverPlayerEntity.incrementStat(Stats.USED.getOrCreateStat(Items.TOTEM_OF_UNDYING));
                    Criteria.USED_TOTEM.trigger(serverPlayerEntity, handItem);
                }
                handItem.setDamage(handItem.getMaxDamage() / 2);
                this.setHealth(1.0F);
                this.clearStatusEffects();
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 900, 1));
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 100, 1));
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 800, 0));
                entity.world.sendEntityStatus(entity, (byte) 35);
                cir.setReturnValue(true);
            }
        }
    }
}
