package com.sigmundgranaas.forgerocommon.toolhandler;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface DynamicAttributeTool {
    Multimap<EntityAttribute, EntityAttributeModifier> EMPTY = ImmutableSetMultimap.of();

    default Multimap<EntityAttribute, EntityAttributeModifier> getDynamicModifiers(EquipmentSlot slot, ItemStack stack, @Nullable LivingEntity user) {
        return EMPTY;
    }
}
