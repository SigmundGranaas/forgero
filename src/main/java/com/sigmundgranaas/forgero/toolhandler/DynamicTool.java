package com.sigmundgranaas.forgero.toolhandler;


import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DynamicTool {
    Multimap<EntityAttribute, EntityAttributeModifier> EMPTY = ImmutableSetMultimap.of();

    default int getMiningLevel(@NotNull ItemStack tool, @Nullable LivingEntity user) {
        return 0;
    }

    default int getMiningLevel(Tag<Item> tag, BlockState state, ItemStack stack, @Nullable LivingEntity user) {
        return getMiningLevel(stack, user);
    }

    default float getMiningSpeedMultiplier(Tag<Item> tag, BlockState state, ItemStack stack, @Nullable LivingEntity user) {
        return 1.0F;
    }

    default float getMiningSpeedMultiplier(BlockState state, ItemStack stack) {
        return 1.0F;
    }

    default Multimap<EntityAttribute, EntityAttributeModifier> getDynamicModifiers(EquipmentSlot slot, ItemStack stack, @Nullable LivingEntity user) {
        return EMPTY;
    }

    default boolean isEffectiveOn(BlockState state) {
        return false;
    }

    int getDurability(ItemStack stack);

    int getCustomItemBarStep(ItemStack stack);
}
