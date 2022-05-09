package com.sigmundgranaas.forgero.mixins;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;


@Mixin(PlayerEntity.class)
public abstract class PlayerEntityAttackMixin {
    @ModifyVariable(method = "attack", at = @At("STORE"), ordinal = 0)
    private float injected(float x, Entity target) {
        if (((PlayerEntity) (Object) this).getMainHandStack().getItem() instanceof ForgeroToolItem toolItem) {
            ForgeroInitializer.LOGGER.info(target.getName());
            return 100 * x;
        }
        return x;
    }
}


