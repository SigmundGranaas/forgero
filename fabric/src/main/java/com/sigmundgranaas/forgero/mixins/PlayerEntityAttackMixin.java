package com.sigmundgranaas.forgero.mixins;

import com.sigmundgranaas.forgerocore.property.Target;
import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import com.sigmundgranaas.forgero.toolhandler.EntityTarget;
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
            var stack = ((PlayerEntity) (Object) this).getMainHandStack();
            var tool = toolItem.convertItemStack(stack, toolItem.getTool());
            float initialAttackDamage = tool.getAttackDamage(Target.createEmptyTarget());
            float attackDamageTarget = tool.getAttackDamage(new EntityTarget(target.getType()));
            if (initialAttackDamage != attackDamageTarget) {
                return x + attackDamageTarget - initialAttackDamage;
            }
        }
        return x;
    }
}


