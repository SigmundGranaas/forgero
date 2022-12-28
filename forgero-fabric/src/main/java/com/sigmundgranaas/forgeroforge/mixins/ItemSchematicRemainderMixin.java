package com.sigmundgranaas.forgeroforge.mixins;

import com.sigmundgranaas.forgero.state.State;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemSchematicRemainderMixin {
    @Inject(method = "hasRecipeRemainder", at = @At("HEAD"), cancellable = true)
    public void hasRecipeRemainderNew(CallbackInfoReturnable<Boolean> cir) {
        if ((this) instanceof State state && state.name().contains("schematic")) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method = "getRecipeRemainder", at = @At("HEAD"), cancellable = true)
    public void getRecipeRemainder(CallbackInfoReturnable<Item> cir) {
        if ((this) instanceof State state && state.name().contains("schematic")) {
            var schem = (Item) ((Object) this);
            cir.setReturnValue(schem);
            cir.cancel();
        }
    }
}
