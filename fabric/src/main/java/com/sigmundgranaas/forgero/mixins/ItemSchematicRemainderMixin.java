package com.sigmundgranaas.forgero.mixins;

import com.sigmundgranaas.forgero.item.items.SchematicItem;
import com.sigmundgranaas.forgero.state.State;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.sigmundgranaas.forgero.type.Type.SCHEMATIC;

@Mixin(Item.class)
public class ItemSchematicRemainderMixin {

    @Inject(method = "hasRecipeRemainder", at = @At("HEAD"), cancellable = true)
    public void hasRecipeRemainder(CallbackInfoReturnable<Boolean> cir) {
        //noinspection ConstantConditions
        if (((Object) this) instanceof SchematicItem) {
            cir.setReturnValue(true);
            cir.cancel();
        }

    }

    @Inject(method = "hasRecipeRemainder", at = @At("HEAD"), cancellable = true)
    public void hasRecipeRemainderNew(CallbackInfoReturnable<Boolean> cir) {
        if (((Object) this) instanceof State state && state.type().test(SCHEMATIC)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method = "getRecipeRemainder", at = @At("HEAD"), cancellable = true)
    public void getRecipeRemainder(CallbackInfoReturnable<Item> cir) {
        //noinspection ConstantConditions
        if (((Object) this) instanceof SchematicItem) {
            cir.setReturnValue((Item) (Object) this);
            cir.cancel();
        }

    }
}
