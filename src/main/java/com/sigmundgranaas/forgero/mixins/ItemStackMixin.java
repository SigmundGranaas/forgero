package com.sigmundgranaas.forgero.mixins;

import com.sigmundgranaas.forgero.item.forgerotool.tool.instance.ForgeroPickaxeInstance;
import com.sigmundgranaas.forgero.item.forgerotool.tool.instance.ForgeroToolCreator;
import com.sigmundgranaas.forgero.item.forgerotool.tool.instance.ForgeroToolInstance;
import com.sigmundgranaas.forgero.item.forgerotool.tool.item.ForgeroTool;
import net.minecraft.block.BlockState;
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
public abstract class ItemStackMixin {

    @Shadow
    private NbtCompound nbt;

    @Shadow
    public abstract Item getItem();

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<?>> cir) {
        if (this.getItem() instanceof ForgeroTool) {
            Optional<ForgeroToolInstance> tool = ForgeroToolCreator.createForgeroToolInstance(this.nbt, this.getItem());
        }
        cir.setReturnValue(this.getItem().use(world, user, hand));
        cir.cancel();
    }

    @Inject(at = @At("RETURN"), method = "getMiningSpeedMultiplier", cancellable = true)
    public void getMiningSpeedMultiplier(BlockState state, CallbackInfoReturnable<Float> info) {
        float customSpeed = 0F;
        if (this.getItem() instanceof ForgeroTool) {
            Optional<ForgeroToolInstance> tool = ForgeroToolCreator.createForgeroToolInstance(this.nbt, this.getItem());
            if (tool.isPresent() && tool.get() instanceof ForgeroPickaxeInstance forgeroTool) {
                customSpeed = this.getItem().getMiningSpeedMultiplier((ItemStack) (Object) this, state) + forgeroTool.getMiningSpeedMultiplier();

            }
        }
        if (info.getReturnValueF() < customSpeed) {
            info.setReturnValue(customSpeed);
        }
    }
}
