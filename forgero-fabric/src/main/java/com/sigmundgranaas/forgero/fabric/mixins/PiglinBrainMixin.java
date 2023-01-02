package com.sigmundgranaas.forgero.fabric.mixins;

import com.sigmundgranaas.forgero.core.property.passive.Static;
import com.sigmundgranaas.forgero.core.property.passive.StaticPassiveType;
import com.sigmundgranaas.forgero.minecraft.common.item.StateItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PiglinBrain.class)
public abstract class PiglinBrainMixin {

    @Inject(method = "isGoldenItem", at = @At("HEAD"), cancellable = true)
    private static void isGoldenItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (isGoldenForgeroTool(stack)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "wearsGoldArmor", at = @At("HEAD"), cancellable = true)
    private static void wearsGold(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (isGoldenForgeroTool(entity.getMainHandStack())) {
            cir.setReturnValue(true);
        } else if (isGoldenForgeroTool(entity.getOffHandStack())) {
            cir.setReturnValue(true);
        }
    }

    private static boolean isGoldenForgeroTool(ItemStack stack) {
        if (stack.getItem() instanceof StateItem holder) {
            Optional<Static> goldenProperty = holder.dynamicState(stack)
                    .stream()
                    .getStaticPassiveProperties()
                    .filter(element -> element.getStaticType() == StaticPassiveType.GOLDEN)
                    .findAny();
            return goldenProperty.isPresent();
        }
        return false;
    }
}
