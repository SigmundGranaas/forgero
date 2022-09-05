package com.sigmundgranaas.forgero.mixins;

import com.sigmundgranaas.forgerocore.gem.EmptyGem;
import com.sigmundgranaas.forgerocore.gem.Gem;
import com.sigmundgranaas.forgerocore.tool.ForgeroTool;
import com.sigmundgranaas.forgerocore.tool.ForgeroToolWithBinding;
import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.NBTFactory;
import com.sigmundgranaas.forgero.registry.ForgeroItemRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackDropGemOnBreakMixin {

    @Shadow
    public abstract Item getItem();

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;"), method = "damage(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V")
    public <T extends LivingEntity> void dropGemOnForgeroToolBreak(int amount, T entity, Consumer<T> breakCallback, CallbackInfo ci) {
        if (getItem() instanceof ForgeroToolItem holder) {
            ForgeroTool tool = holder.convertItemStack((ItemStack) (Object) this, holder.getTool());
            if (!(tool.getToolHead().getState().getGem() instanceof EmptyGem)) {
                entity.dropStack(createItemStackFromGem(tool.getToolHead().getState().getGem()));
            }
            if (!(tool.getToolHandle().getState().getGem() instanceof EmptyGem)) {
                entity.dropStack(createItemStackFromGem(tool.getToolHandle().getState().getGem()));
            }
            if (tool instanceof ForgeroToolWithBinding toolWithBinding && !(tool.getToolHead().getState().getGem() instanceof EmptyGem)) {
                entity.dropStack(createItemStackFromGem(toolWithBinding.getBinding().getState().getGem()));
            }
        }
    }

    private ItemStack createItemStackFromGem(Gem gem) {
        ItemStack output = new ItemStack(ForgeroItemRegistry.GEM_ITEM.stream().filter(gemItem -> gemItem.getGem().getStringIdentifier().equals(gem.getStringIdentifier())).findAny().get());
        NBTFactory.INSTANCE.createNBTFromGem(gem, output.getOrCreateNbt());
        return output;
    }

}


