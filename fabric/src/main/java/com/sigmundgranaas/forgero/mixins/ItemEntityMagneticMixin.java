package com.sigmundgranaas.forgero.mixins;

import com.sigmundgranaas.forgerocore.tool.ForgeroTool;
import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import com.sigmundgranaas.forgero.toolhandler.MagneticHandler;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ItemEntity.class)
public abstract class ItemEntityMagneticMixin {
    @Shadow
    public abstract ItemStack getStack();

    @Inject(method = "tick", at = @At("RETURN"))
    public void magneticTickInject(CallbackInfo callbackInfo) {
        if (this.getStack().getItem() instanceof ForgeroToolItem toolItem) {
            ForgeroTool tool = toolItem.convertItemStack(this.getStack(), toolItem.getTool());
            var properties = tool.getPropertyStream().getLeveledPassiveProperties().toList();
            ItemEntity itemEntity = ((ItemEntity) (Object) this);
            if (!properties.isEmpty()) {
                MagneticHandler handler = new MagneticHandler(itemEntity);
                var entities = handler.getNearbyEntities(properties.size() + 2, entity -> entity instanceof ItemEntity);
                handler.pullEntities(5, entities);
            }
        }
    }
}
