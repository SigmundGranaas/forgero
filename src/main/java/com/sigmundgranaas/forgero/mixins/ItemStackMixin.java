package com.sigmundgranaas.forgero.mixins;

import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow
    public abstract Item getItem();

    @SuppressWarnings("StatementWithEmptyBody")
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<?>> cir) {
        if (this.getItem() instanceof ForgeroToolItem) {
            //Optional<ForgeroToolInstance> tool = ForgeroToolInstanceFactory.INSTANCE.createForgeroToolInstance((ForgeroToolItem) this.getItem(), this.nbt);
        }
        cir.setReturnValue(this.getItem().use(world, user, hand));
        cir.cancel();
    }


    @Inject(method = "getMaxDamage", at = @At("HEAD"), cancellable = true)
    public void getCustomDurability(CallbackInfoReturnable<Integer> cir) {
        if (this.getItem() instanceof ForgeroToolItem) {
            cir.setReturnValue(((ForgeroToolItem) this.getItem()).getDurability((ItemStack) (Object) this));
        }
    }
}
