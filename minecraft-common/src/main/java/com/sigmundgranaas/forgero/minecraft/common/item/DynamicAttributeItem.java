package com.sigmundgranaas.forgero.minecraft.common.item;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.property.AttributeType;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.minecraft.common.mixins.ItemUUIDMixin;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.FORGERO_IDENTIFIER;

public interface DynamicAttributeItem extends DynamicAttributeTool, DynamicDurability, DynamicEffectiveNess, DynamicMiningLevel, DynamicMiningSpeed {
    LoadingCache<ItemStack, Integer> durabilityCache = CacheBuilder.newBuilder()
            .maximumSize(600)
            .expireAfterAccess(Duration.of(1, ChronoUnit.MINUTES))
            .build(new CacheLoader<>() {
                @Override
                public @NotNull Integer load(@NotNull ItemStack stack) {
                    if (stack.getItem() instanceof DynamicAttributeItem dynamic) {
                        return (int) dynamic.dynamicProperties(stack).stream().applyAttribute(AttributeType.DURABILITY);
                    }
                    return 1;
                }
            });

    LoadingCache<ItemStack, ImmutableMultimap<EntityAttribute, EntityAttributeModifier>> multiMapCache = CacheBuilder.newBuilder()
            .maximumSize(600)
            .expireAfterAccess(Duration.of(1, ChronoUnit.MINUTES))
            .build(new CacheLoader<>() {
                @Override
                public @NotNull ImmutableMultimap<EntityAttribute, EntityAttributeModifier> load(@NotNull ItemStack stack) {
                    return ImmutableMultimap.<EntityAttribute, EntityAttributeModifier>builder().build();
                }
            });

    LoadingCache<ItemStack, Float> miningSpeedCache = CacheBuilder.newBuilder()
            .maximumSize(600)
            .expireAfterAccess(Duration.of(1, ChronoUnit.MINUTES))
            .build(new CacheLoader<>() {
                @Override
                public @NotNull Float load(@NotNull ItemStack stack) {
                    return 1f;
                }
            });

    LoadingCache<String, Integer> defaultDurabilityCache = CacheBuilder.newBuilder()
            .maximumSize(600)
            .expireAfterAccess(Duration.of(1, ChronoUnit.MINUTES))
            .build(new CacheLoader<>() {
                @Override
                public @NotNull Integer load(@NotNull String id) {
                    return ForgeroStateRegistry.stateFinder().find(id).map(state -> state.stream().applyAttribute(AttributeType.DURABILITY)).orElse(1f).intValue();
                }
            });

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

    default boolean isCorrectMiningLevel(BlockState state) {
        int level = this.getMiningLevel();

        for (int i = 1; i < 10; i++) {
            TagKey<Block> key = TagKey.of(Registry.BLOCK_KEY, new Identifier(String.format("fabric:needs_tool_level_%s", i)));
            if (state.isIn(key) && level < i) {
                return false;
            }
        }

        if (state.isIn(BlockTags.NEEDS_DIAMOND_TOOL) && level < 3) {
            return false;
        } else if (state.isIn(BlockTags.NEEDS_IRON_TOOL) && level < 2) {
            return false;
        } else if (state.isIn(BlockTags.NEEDS_STONE_TOOL) && level < 1) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    default Multimap<EntityAttribute, EntityAttributeModifier> getDynamicModifiers(EquipmentSlot slot, ItemStack stack, @Nullable LivingEntity user) {
        if (slot.equals(EquipmentSlot.MAINHAND) && stack.getItem() instanceof DynamicAttributeItem && isEquippable()) {
            try {
                return multiMapCache.get(stack, () -> createMultiMap(stack));
            } catch (ExecutionException e) {
                return EMPTY;
            }
        } else {
            return EMPTY;
        }
    }

    private ImmutableMultimap<EntityAttribute, EntityAttributeModifier> createMultiMap(ItemStack stack) {
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
    }

    @Override
    default int getDurability(ItemStack stack) {
        if (stack.hasNbt() && stack.getOrCreateNbt().contains(FORGERO_IDENTIFIER)) {
            return durabilityCache.getUnchecked(stack);
        } else {
            if (stack.getItem() instanceof StateItem state) {
                return defaultDurabilityCache.getUnchecked(state.identifier());
            }
        }
        return 1;
    }

    default int getItemBarStep(ItemStack stack) {
        return Math.round(13.0F - (float) stack.getDamage() * 13.0F / (float) getDurability(stack));
    }

    default int getDurabilityColor(ItemStack stack) {
        var durability = (float) getDurability(stack);
        float f = Math.max(0.0F, (durability - (float) stack.getDamage()) / durability);
        return MathHelper.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    @Override
    default float getMiningSpeedMultiplier(BlockState state, ItemStack stack) {
        if (stack.getItem() instanceof DynamicAttributeItem dynamic && isEffectiveOn(state)) {
            try {
                return miningSpeedCache.get(stack, () -> mingSpeedCalculation(dynamic, stack, state));
            } catch (ExecutionException e) {
                return 1f;
            }
        }
        return 1f;
    }

    private float mingSpeedCalculation(DynamicAttributeItem dynamic, ItemStack stack, BlockState state) {
        Target target = new BlockBreakingEfficiencyTarget(state);
        return dynamic.dynamicProperties(stack).stream().applyAttribute(target, AttributeType.MINING_SPEED);
    }
}
