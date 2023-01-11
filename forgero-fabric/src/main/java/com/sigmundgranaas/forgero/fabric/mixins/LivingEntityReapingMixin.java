package com.sigmundgranaas.forgero.fabric.mixins;

import com.sigmundgranaas.forgero.core.property.passive.StaticPassiveType;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.minecraft.common.conversion.StateConverter;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityReapingMixin {

    @Inject(method = "onDeath", at = @At("HEAD"))
    public void onDeathByReaping(DamageSource source, CallbackInfo ci) {
        if (source.getAttacker() instanceof LivingEntity entity) {
            ItemStack stack = entity.getMainHandStack();
            var converted = StateConverter.of(stack);
            if (converted.isPresent() && converted.get() instanceof Composite construct) {
                if (construct.stream().getStaticPassiveProperties().anyMatch(prop -> prop.getStaticType() == StaticPassiveType.SOUL_REAPING)) {
                    ItemStack soul = new ItemStack(Items.DIAMOND);
                    LivingEntity thisEntity = (LivingEntity) (Object) this;
                    ItemEntity itemEntity = new ItemEntity(entity.world, thisEntity.getX(), thisEntity.getY(), thisEntity.getZ(), soul);
                    thisEntity.getWorld().spawnEntity(itemEntity);
                }
            }
        }
    }
}
