package com.sigmundgranaas.forgero.fabric.mixins;

import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.minecraft.common.conversion.StateConverter;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkGetTotemMixin {
    @Inject(at = @At("HEAD"), method = "getActiveTotemOfUndying", cancellable = true)
    private static void getTotem(PlayerEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        for (Hand hand : Hand.values()) {
            ItemStack itemStack = entity.getStackInHand(hand);
            if (StateConverter.of(itemStack).filter(state -> state instanceof Composite composite && composite.has("forgero:soul-totem").isPresent()).isPresent()) {
                Item totem = Registry.ITEM.get(new Identifier("forgero:soul-totem"));
                itemStack = new ItemStack(totem);
                if (!itemStack.isEmpty()) {
                    cir.setReturnValue(itemStack);
                }
            }
        }
    }
}
