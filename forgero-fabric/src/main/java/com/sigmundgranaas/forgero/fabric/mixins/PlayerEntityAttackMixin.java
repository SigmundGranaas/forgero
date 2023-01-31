package com.sigmundgranaas.forgero.fabric.mixins;

import com.sigmundgranaas.forgero.core.property.AttributeType;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackDamage;
import com.sigmundgranaas.forgero.minecraft.common.item.StateItem;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.EntityTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityAttackMixin {
    @ModifyVariable(method = "attack", at = @At("STORE"), ordinal = 0)
    private float injected(float x, Entity target) {
        if (((PlayerEntity) (Object) this).getMainHandStack().getItem() instanceof StateItem item) {
            var stack = ((PlayerEntity) (Object) this).getMainHandStack();
            var tool = item.dynamicState(stack);
            float initialAttackDamage = tool.stream().applyAttribute(AttributeType.ATTACK_DAMAGE);
            float attackDamageTarget = AttackDamage.apply(tool, new EntityTarget(target.getType()));
            if (initialAttackDamage != attackDamageTarget) {
                return x + attackDamageTarget - initialAttackDamage;
            }
        }
        return x;
    }
}


