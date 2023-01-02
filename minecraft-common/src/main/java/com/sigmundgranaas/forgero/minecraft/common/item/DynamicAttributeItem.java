package com.sigmundgranaas.forgero.minecraft.common.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.sigmundgranaas.forgero.minecraft.common.mixins.ItemUUIDMixin;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.*;
import com.sigmundgranaas.forgero.core.property.AttributeType;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.Target;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.BlockTags;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface DynamicAttributeItem extends DynamicAttributeTool, DynamicDurability, DynamicEffectiveNess, DynamicMiningLevel, DynamicMiningSpeed {

    UUID TEST_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A34DB5CF");
    UUID ADDITION_ATTACK_DAMAGE_MODIFIER_ID = UUID.fromString("CB3F55D5-655C-4F38-A497-9C13A33DB5CF");

    PropertyContainer dynamicProperties(ItemStack stack);

    PropertyContainer defaultProperties();

    default boolean isEquippable() {
        return true;
    }

    @Override
    default int getMiningLevel(ItemStack stack) {
        return (int) dynamicProperties(stack).stream().applyAttribute(Target.EMPTY, AttributeType.MINING_LEVEL);
    }

    @Override
    default int getMiningLevel() {
        return (int) defaultProperties().stream().applyAttribute(Target.EMPTY, AttributeType.MINING_LEVEL);
    }

    default boolean isCorrectMiningLevel(BlockState state){
        int i = this.getMiningLevel();
        if (state.isIn(BlockTags.NEEDS_DIAMOND_TOOL) && i < 3) {
            return false;
        } else if (state.isIn(BlockTags.NEEDS_IRON_TOOL) && i < 2) {
            return false;
        } else if (state.isIn(BlockTags.NEEDS_STONE_TOOL) && i < 1) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    default Multimap<EntityAttribute, EntityAttributeModifier> getDynamicModifiers(EquipmentSlot slot, ItemStack stack, @Nullable LivingEntity user) {
        if (slot.equals(EquipmentSlot.MAINHAND) && stack.getItem() instanceof DynamicAttributeItem && isEquippable()) {
            Target target = Target.createEmptyTarget();
            ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
            float currentToolDamage = dynamicProperties(stack).stream().applyAttribute(target, AttributeType.ATTACK_DAMAGE);
            float baseToolDamage = defaultProperties().stream().applyAttribute(AttributeType.ATTACK_DAMAGE);
            //Base attack damage
            builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(((ItemUUIDMixin) stack.getItem()).getATTACK_DAMAGE_MODIFIER_ID(), "Tool modifier", baseToolDamage, EntityAttributeModifier.Operation.ADDITION));

            //Attack damage addition
            builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ADDITION_ATTACK_DAMAGE_MODIFIER_ID, "Attack Damage Addition", currentToolDamage - baseToolDamage, EntityAttributeModifier.Operation.ADDITION));

            //Attack speed
            float baseAttackSpeed = dynamicProperties(stack).stream().applyAttribute(target, AttributeType.ATTACK_SPEED);
            float currentAttackSpeed = defaultProperties().stream().applyAttribute(AttributeType.ATTACK_SPEED);
            if (currentAttackSpeed != baseAttackSpeed) {
                builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(TEST_UUID, "Tool attack speed addition", baseAttackSpeed - currentAttackSpeed, EntityAttributeModifier.Operation.ADDITION));
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

    default int getItemBarStep(ItemStack stack) {
        return Math.round(13.0F - (float) stack.getDamage() * 13.0F / (float) getDurability(stack));
    }

    @Override
    default float getMiningSpeedMultiplier(BlockState state, ItemStack stack) {
        if (stack.getItem() instanceof DynamicAttributeItem dynamic && isEffectiveOn(state)) {
            Target target = new BlockBreakingEfficiencyTarget(state);
            return dynamic.dynamicProperties(stack).stream().applyAttribute(target, AttributeType.MINING_SPEED);
        }
        return 1f;
    }
}
