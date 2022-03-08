package com.sigmundgranaas.forgero.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.sigmundgranaas.forgero.core.properties.attribute.Target;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHead;
import com.sigmundgranaas.forgero.item.adapter.FabricToForgeroToolAdapter;
import net.fabricmc.fabric.api.tool.attribute.v1.DynamicAttributeTool;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface ForgeroToolItem extends DynamicAttributeTool {
    UUID TEST_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A34DB5CF");
    static final UUID ATTACK_DAMAGE_MODIFIER_ID = UUID.fromString("CB3F55D5-645C-4F38-A497-9C13A33DB5CF");

    FabricToForgeroToolAdapter adapter = FabricToForgeroToolAdapter.createAdapter();

    Identifier getIdentifier();

    ForgeroToolTypes getToolType();

    ForgeroTool getTool();

    ToolPartHead getHead();

    ToolPartHandle getHandle();

    default int getDurability(ItemStack stack) {
        ForgeroTool forgeroTool = FabricToForgeroToolAdapter.createAdapter().getTool(stack).orElse(getTool());
        return forgeroTool.getDurability(Target.createEmptyTarget());
    }

    default int getCustomItemBarStep(ItemStack stack) {
        ForgeroTool forgeroTool = FabricToForgeroToolAdapter.createAdapter().getTool(stack).orElse(getTool());
        return Math.round(13.0f - (float) stack.getDamage() * 13.0f / (float) forgeroTool.getDurability(Target.createEmptyTarget()));
    }

    FabricToForgeroToolAdapter getToolAdapter();

    Tag<Item> getToolTags();

    @Override
    default Multimap<EntityAttribute, EntityAttributeModifier> getDynamicModifiers(EquipmentSlot slot, ItemStack stack, @Nullable LivingEntity user) {
        if (slot.equals(EquipmentSlot.MAINHAND)) {
            ForgeroTool tool = FabricToForgeroToolAdapter.createAdapter().getTool(stack).orElse(this.getTool());

            Target target = Target.createEmptyTarget();
            ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
            builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Attack Damage Addition", tool.getAttackDamage(target) - getTool().getAttackDamage(target), EntityAttributeModifier.Operation.ADDITION));
            builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(TEST_UUID, "Tool attack speed addition", tool.getAttackSpeed(target) - getTool().getAttackSpeed(target), EntityAttributeModifier.Operation.ADDITION));
            //builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Tool modifier", getAttackDamage(), EntityAttributeModifier.Operation.ADDITION));
            return builder.build();
        } else {
            return EMPTY;
        }
    }

    @Override
    default int getMiningLevel(Tag<Item> tag, BlockState state, ItemStack stack, @Nullable LivingEntity user) {
        if (tag.equals(getToolTags())) {
            ForgeroTool forgeroTool = getToolAdapter().getTool(stack).orElse(getTool());
            Target target = Target.createEmptyTarget();
            int miningLevel = forgeroTool.getMiningLevel(target);
            return miningLevel;
        }

        return 0;
    }

    @Override
    default float getMiningSpeedMultiplier(Tag<Item> tag, BlockState state, ItemStack stack, @Nullable LivingEntity user) {
        if (tag.equals(getToolTags())) {
            ForgeroTool forgeroTool = getToolAdapter().getTool(stack).orElse(getTool());
            Target target = Target.createEmptyTarget();
            float miningSpeedMultiplier = forgeroTool.getMiningSpeedMultiplier(target);
            return miningSpeedMultiplier;
        }

        return 1f;
    }

    default ForgeroTool convertItemStack(ItemStack toolStack, ForgeroTool baseTool) {
        return adapter.getTool(toolStack).orElse(baseTool);
    }
}
