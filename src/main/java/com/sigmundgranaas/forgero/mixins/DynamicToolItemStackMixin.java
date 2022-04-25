package com.sigmundgranaas.forgero.mixins;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.sigmundgranaas.forgero.toolhandler.DynamicTool;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Mixin originally used by The Fabric APIs dynamic attribute module, but has since been deprecated.
 * This class is almost an identical copy of the mixin developed by the Fabric project.
 * This is awaiting a rewrite to better suit the purpose of this mod, but is being kept while creating a new tool handler
 * <p>
 * All credits go to the original authors <a href="https://github.com/FabricMC/fabric/tree/1.18/fabric-tool-attribute-api-v1/src/main/java/net/fabricmc/fabric"></a>
 */
@Mixin(ItemStack.class)
public abstract class DynamicToolItemStackMixin {

    @Unique
    @Nullable
    private LivingEntity contextEntity = null;

    @Shadow
    public abstract Item getItem();

    @Inject(at = @At("RETURN"), method = "isSuitableFor", cancellable = true)
    public void isEffectiveOn(BlockState state, CallbackInfoReturnable<Boolean> info) {
        if (this.getItem() instanceof DynamicTool holder) {
            info.setReturnValue(holder.isEffectiveOn(state));
        }
    }

    @Inject(at = @At("RETURN"), method = "getMiningSpeedMultiplier", cancellable = true)
    public void getMiningSpeedMultiplier(BlockState state, CallbackInfoReturnable<Float> info) {
        if (this.getItem() instanceof DynamicTool holder) {
            float customSpeed = holder.getMiningSpeedMultiplier(state, (ItemStack) (Object) this);
            if (info.getReturnValueF() <= customSpeed) {
                info.setReturnValue(customSpeed);
            }
        }

    }

    // This inject stores context about the player viewing an ItemStack's tooltip before attributes are calculated.
    @Environment(EnvType.CLIENT)
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getAttributeModifiers(Lnet/minecraft/entity/EquipmentSlot;)Lcom/google/common/collect/Multimap;"), method = "getTooltip")
    private void storeTooltipAttributeEntityContext(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir) {
        contextEntity = player;
    }

    // This inject removes context specified in the previous inject.
    // This is done to prevent issues with other mods calling getAttributeModifiers.
    @Environment(EnvType.CLIENT)
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getAttributeModifiers(Lnet/minecraft/entity/EquipmentSlot;)Lcom/google/common/collect/Multimap;", shift = At.Shift.AFTER), method = "getTooltip")
    private void revokeTooltipAttributeEntityContext(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir) {
        contextEntity = null;
    }

    @ModifyVariable(method = "getAttributeModifiers", at = @At(value = "RETURN", shift = At.Shift.BEFORE))
    public Multimap<EntityAttribute, EntityAttributeModifier> modifyAttributeModifiersMap(Multimap<EntityAttribute, EntityAttributeModifier> multimap, EquipmentSlot slot) {
        ItemStack stack = (ItemStack) (Object) this;

        // Only perform our custom operations if the tool being operated on is dynamic.
        if (stack.getItem() instanceof DynamicTool holder) {
            // The Multimap passed in is not ordered, so we need to re-assemble the vanilla and modded attributes
            // into a custom, ordered Multimap. If this step is not done, and both vanilla + modded attributes
            // exist at once, the item tooltip attribute lines will randomly switch positions.
            LinkedListMultimap<EntityAttribute, EntityAttributeModifier> orderedAttributes = LinkedListMultimap.create();
            // First, add all vanilla attributes to our ordered Multimap.
            orderedAttributes.putAll(multimap);
            // Second, calculate the dynamic attributes, and add them at the end of our Multimap.
            orderedAttributes.putAll(holder.getDynamicModifiers(slot, stack, contextEntity));
            return orderedAttributes;
        }

        return multimap;
    }

    @Inject(method = "getMaxDamage", at = @At("HEAD"), cancellable = true)
    public void getCustomDurability(CallbackInfoReturnable<Integer> cir) {
        if (this.getItem() instanceof DynamicTool tool) {
            cir.setReturnValue(tool.getDurability((ItemStack) (Object) this));
        }
    }
}
