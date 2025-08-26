package com.sigmundgranaas.forgero.smithing.mixins;

import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.ItemStack;

@Mixin(ItemEntity.class)
public class ItemEntityTemperatureMixin implements com.sigmundgranaas.forgero.smithing.temperature.TemperatureTracked {
    @Unique
    private static final TrackedData<Integer> FORGERO_TEMPERATURE = DataTracker.registerData(ItemEntity.class, TrackedDataHandlerRegistry.INTEGER);

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void forgero$initTemperatureTracker(CallbackInfo ci) {
        ((ItemEntity)(Object)this).getDataTracker().startTracking(FORGERO_TEMPERATURE, 20);
    }

    @Unique
    public void forgero$setTrackedTemperature(int temperature) {
        ((ItemEntity)(Object)this).getDataTracker().set(FORGERO_TEMPERATURE, temperature);
    }

    @Unique
    public int forgero$getTrackedTemperature() {
        return ((ItemEntity)(Object)this).getDataTracker().get(FORGERO_TEMPERATURE);
    }

    // Sync tracked temperature with NBT when stack changes
    @Inject(method = "setStack", at = @At("TAIL"))
    private void forgero$syncTemperatureWithStack(ItemStack stack, CallbackInfo ci) {
        int temp = TemperatureUtils.getTemperature(stack);
        forgero$setTrackedTemperature(temp);
    }
}
