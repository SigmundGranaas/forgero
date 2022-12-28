package com.sigmundgranaas.forgeroforge.mixins;

import com.sigmundgranaas.forgeroforge.item.StateItem;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgeroforge.toolhandler.MagneticHandler;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMagneticMixin {

    @Shadow
    public abstract ItemStack getMainHandStack();

    @Inject(method = "baseTick", at = @At("RETURN"))
    public void magneticTickInject(CallbackInfo callbackInfo) {
        if (this.getMainHandStack().getItem() instanceof StateItem stateItem) {
            State state = stateItem.dynamicState(getMainHandStack());
            var properties = state.stream().getLeveledPassiveProperties().toList();
            if (!properties.isEmpty()) {
                MagneticHandler handler = new MagneticHandler(((LivingEntity) (Object) this));
                var entities = handler.getNearbyEntities(properties.size() + 2, entity -> entity instanceof ItemEntity);
                handler.pullEntities(5, entities);

            }

        }
    }
}
