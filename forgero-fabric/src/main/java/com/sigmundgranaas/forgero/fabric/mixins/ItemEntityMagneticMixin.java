package com.sigmundgranaas.forgero.fabric.mixins;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.item.StateItem;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.MagneticHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ItemEntity.class)
public abstract class ItemEntityMagneticMixin extends Entity {
    public ItemEntityMagneticMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    public abstract ItemStack getStack();

    @Inject(method = "tick", at = @At("RETURN"))
    public void magneticTickInject(CallbackInfo callbackInfo) {
        if (this.getStack().getItem() instanceof StateItem stateItem) {
            State state = stateItem.dynamicState(getStack());
            MagneticHandler.of(state, this).ifPresent(MagneticHandler::run);
        }
    }
}
