package com.sigmundgranaas.forgero.mixins;

import com.sigmundgranaas.forgero.item.forgerotool.tool.instance.ForgeroPickaxeInstance;
import com.sigmundgranaas.forgero.item.forgerotool.tool.instance.ForgeroToolCreator;
import com.sigmundgranaas.forgero.item.forgerotool.tool.instance.ForgeroToolInstance;
import com.sigmundgranaas.forgero.item.forgerotool.tool.item.ForgeroTool;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Shadow
    private Item item;
    @Shadow
    private NbtCompound nbt;

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult> cir) {
        if (this.item instanceof ForgeroTool) {
            Optional<ForgeroToolInstance> tool = ForgeroToolCreator.createForgeroToolInstance(this.nbt, this.item);
            if (tool.isPresent() && tool.get() instanceof ForgeroPickaxeInstance) {
                System.out.println(((ForgeroPickaxeInstance) tool.get()).getDamageMultiplier());
            } else {

            }
        }

        cir.setReturnValue(this.item.use(world, user, hand));
        cir.cancel();
    }
}
