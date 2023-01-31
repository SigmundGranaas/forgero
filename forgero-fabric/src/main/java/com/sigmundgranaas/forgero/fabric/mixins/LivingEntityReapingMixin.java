package com.sigmundgranaas.forgero.fabric.mixins;

import com.sigmundgranaas.forgero.minecraft.common.entity.SoulEntity;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.SoulReapingHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityReapingMixin extends Entity {

    public LivingEntityReapingMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    public void onDeathByReaping(DamageSource source, CallbackInfo ci) {
        //noinspection ConstantValue
        if (source.getAttacker() instanceof PlayerEntity entity && (Object) this instanceof LivingEntity livingEntity && !((Object) this instanceof SoulEntity)) {
            SoulReapingHandler.of(entity, livingEntity).run();
        }
    }
}
