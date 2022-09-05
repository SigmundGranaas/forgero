package com.sigmundgranaas.forgero.item.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.sigmundgranaas.forgerocore.property.AttributeType;
import com.sigmundgranaas.forgerocore.property.PropertyContainer;
import com.sigmundgranaas.forgerocore.property.Target;
import com.sigmundgranaas.forgero.mixins.ItemUUIDMixin;
import com.sigmundgranaas.forgero.toolhandler.*;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import static com.sigmundgranaas.forgero.item.ForgeroToolItem.ADDITION_ATTACK_DAMAGE_MODIFIER_ID;
import static com.sigmundgranaas.forgero.item.ForgeroToolItem.TEST_UUID;

public interface DynamicAttributeItem extends DynamicAttributeTool, DynamicDurability, DynamicEffectiveNess, DynamicMiningLevel, DynamicMiningSpeed {

    PropertyContainer dynamicProperties(ItemStack stack);

    PropertyContainer defaultProperties();

    @Override
    default Multimap<EntityAttribute, EntityAttributeModifier> getDynamicModifiers(EquipmentSlot slot, ItemStack stack, @Nullable LivingEntity user) {
        if (slot.equals(EquipmentSlot.MAINHAND) && stack.getItem() instanceof DynamicAttributeItem) {
            Target target = Target.createEmptyTarget();
            ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
            float baseToolDamage = dynamicProperties(stack).stream().applyAttribute(target, AttributeType.ATTACK_DAMAGE);
            float currentToolDamage = defaultProperties().stream().applyAttribute(AttributeType.ATTACK_DAMAGE);
            //Base attack damage
            builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(((ItemUUIDMixin) stack.getItem()).getATTACK_DAMAGE_MODIFIER_ID(), "Tool modifier", baseToolDamage, EntityAttributeModifier.Operation.ADDITION));

            //Attack damage addition
            builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ADDITION_ATTACK_DAMAGE_MODIFIER_ID, "Attack Damage Addition", currentToolDamage - baseToolDamage, EntityAttributeModifier.Operation.ADDITION));

            //Attack speed
            float baseAttackSpeed = dynamicProperties(stack).stream().applyAttribute(target, AttributeType.ATTACK_DAMAGE);
            float currentAttackSpeed = defaultProperties().stream().applyAttribute(AttributeType.ATTACK_SPEED);
            if (currentAttackSpeed != baseAttackSpeed) {
                builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(TEST_UUID, "Tool attack speed addition", currentAttackSpeed - baseAttackSpeed, EntityAttributeModifier.Operation.ADDITION));
            }
            return builder.build();
        } else {
            return EMPTY;
        }
    }

    @Override
    default int getDurability(ItemStack stack) {
        return (int) dynamicProperties(stack).stream().applyAttribute(AttributeType.DURABILITY);
    }

    @Override
    default boolean isEffectiveOn(BlockState state) {
        return DynamicEffectiveNess.super.isEffectiveOn(state);
    }

    @Override
    default float getMiningSpeedMultiplier(BlockState state, ItemStack stack) {
        if (stack.getItem() instanceof DynamicAttributeItem dynamic) {
            Target target = new BlockBreakingEfficiencyTarget(state);
            return dynamic.dynamicProperties(stack).stream().applyAttribute(target, AttributeType.MINING_SPEED);
        }
        return 1f;
    }
}
