package com.sigmundgranaas.forgero.fabric.mixins;

import com.google.common.collect.Multimap;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.DynamicDurability;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.DynamicEffectiveNess;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.DynamicMiningSpeed;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.MultiMapMergeHandler;

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

	@Shadow
	public abstract int getDamage();

	@Shadow
	public abstract int getMaxDamage();

	@Inject(at = @At("RETURN"), method = "isSuitableFor", cancellable = true)
	public void isEffectiveOn(BlockState state, CallbackInfoReturnable<Boolean> info) {
		if (this.getItem() instanceof DynamicEffectiveNess holder) {
			info.setReturnValue(holder.isEffective(state, (ItemStack) (Object) this));
		}
	}

	@Inject(at = @At("RETURN"), method = "getMiningSpeedMultiplier", cancellable = true)
	public void getMiningSpeedMultiplier(BlockState state, CallbackInfoReturnable<Float> info) {
		if (this.getItem() instanceof DynamicMiningSpeed holder) {
			float customSpeed = holder.getMiningSpeedMultiplier(state, (ItemStack) (Object) this);
			info.setReturnValue(customSpeed);
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
		return new MultiMapMergeHandler().modifyAttributeModifiersMap(multimap, slot, stack, contextEntity);
	}

	@Inject(method = "getMaxDamage", at = @At("HEAD"), cancellable = true)
	public void getCustomDurability(CallbackInfoReturnable<Integer> cir) {
		if (this.getItem() instanceof DynamicDurability tool) {
			cir.setReturnValue(tool.getDurability((ItemStack) (Object) this));
		}
	}

	@Inject(method = "getItemBarStep", at = @At("HEAD"), cancellable = true)
	public void getItemBarStep(CallbackInfoReturnable<Integer> cir) {
		if (this.getItem() instanceof DynamicDurability) {
			cir.setReturnValue(Math.round(13.0f - (float) getDamage() * 13.0f / getMaxDamage()));
		}
	}
}
