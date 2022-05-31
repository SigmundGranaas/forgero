package com.sigmundgranaas.forgero.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.resource.ForgeroResourceType;
import com.sigmundgranaas.forgero.core.schematic.HeadSchematic;
import com.sigmundgranaas.forgero.core.schematic.Schematic;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHead;
import com.sigmundgranaas.forgero.item.adapter.FabricToForgeroToolAdapter;
import com.sigmundgranaas.forgero.toolhandler.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.tag.TagKey;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

public interface ForgeroToolItem extends DynamicAttributeTool, DynamicDurability, DynamicEffectiveNess, DynamicMiningLevel, DynamicMiningSpeed, PropertyContainer, ForgeroItem<Item> {
    UUID TEST_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A34DB5CF");
    UUID ATTACK_DAMAGE_MODIFIER_ID = UUID.fromString("CB3F55D5-645C-4F38-A497-9C13A33DB5CF");
    FabricToForgeroToolAdapter adapter = FabricToForgeroToolAdapter.createAdapter();

    @Override
    default boolean isEffectiveOn(BlockState state) {
        return state.isIn(getToolTags());
    }

    Identifier getIdentifier();

    ForgeroToolTypes getToolType();

    ForgeroTool getTool();

    @Override
    default ForgeroResourceType getResourceType() {
        return ForgeroResourceType.TOOL;
    }

    @Override
    default String getStringIdentifier() {
        return getIdentifier().getPath();
    }

    @Override
    default String getResourceName() {
        return getIdentifier().getPath();
    }

    ToolPartHead getHead();

    ToolPartHandle getHandle();

    default int getDurability(ItemStack stack) {
        ForgeroTool forgeroTool = FabricToForgeroToolAdapter.createAdapter().getTool(stack).orElse(getTool());
        return forgeroTool.getDurability(Target.createEmptyTarget());
    }


    FabricToForgeroToolAdapter getToolAdapter();

    TagKey<Block> getToolTags();

    @Override
    default Multimap<EntityAttribute, EntityAttributeModifier> getDynamicModifiers(EquipmentSlot slot, ItemStack stack, @Nullable LivingEntity user) {
        if (slot.equals(EquipmentSlot.MAINHAND)) {
            ForgeroTool tool = FabricToForgeroToolAdapter.createAdapter().getTool(stack).orElse(this.getTool());

            Target target = Target.createEmptyTarget();
            ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
            builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Attack Damage Addition", tool.getAttackDamage(target) - getTool().getAttackDamage(target), EntityAttributeModifier.Operation.ADDITION));
            if (tool.getAttackSpeed(target) != getTool().getAttackSpeed(target)) {
                builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(TEST_UUID, "Tool attack speed addition", tool.getAttackSpeed(target) - getTool().getAttackSpeed(target), EntityAttributeModifier.Operation.ADDITION));
            }
            return builder.build();
        } else {
            return EMPTY;
        }
    }


    default float getMiningSpeedMultiplier(BlockState state, ItemStack stack) {
        if (stack.getItem() instanceof MiningToolItem && state.isIn(getToolTags())) {
            ForgeroTool forgeroTool = getToolAdapter().getTool(stack).orElse(getTool());
            Target target = new BlockBreakingEfficiencyTarget(state);
            return forgeroTool.getMiningSpeedMultiplier(target);
        }

        return 1f;
    }

    default ForgeroTool convertItemStack(ItemStack toolStack, ForgeroTool baseTool) {
        return adapter.getTool(toolStack).orElse(getTool());
    }

    default Text getForgeroTranslatableToolName() {
        ForgeroTool tool = getTool();
        MutableText text = new TranslatableText(String.format("item.%s.%s", ForgeroInitializer.MOD_NAMESPACE, tool.getToolHead().getPrimaryMaterial().getResourceName().toLowerCase(Locale.ROOT))).append(" ");
        Schematic schematic = tool.getToolHead().getSchematic();
        if (!schematic.getResourceName().equals("default")) {
            text.append(new TranslatableText(String.format("item.%s.%s", ForgeroInitializer.MOD_NAMESPACE, schematic.getResourceName())).append(" "));
        }

        String headType = switch (((HeadSchematic) schematic).getToolType()) {
            case AXE -> "axe";
            case PICKAXE -> "pickaxe";
            case SHOVEL -> "shovel";
            case SWORD -> "sword";
            case HOE -> "hoe";
        };
        text.append(new TranslatableText(String.format("item.%s.%s", ForgeroInitializer.MOD_NAMESPACE, headType))).append(" ");
        return text;
    }

    @NotNull
    Item getItem();

}
