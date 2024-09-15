package com.sigmundgranaas.forgero.item;

import static com.sigmundgranaas.forgero.registry.registrar.AttributesRegistrar.*;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Armor;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.*;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.mixins.ItemUUIDMixin;
import com.sigmundgranaas.forgero.service.StateService;
import com.sigmundgranaas.forgero.toolhandler.AdditionalHealthHandler;
import com.sigmundgranaas.forgero.toolhandler.DynamicAttributeTool;
import com.sigmundgranaas.forgero.toolhandler.DynamicDurability;
import com.sigmundgranaas.forgero.toolhandler.DynamicEffectiveNess;
import com.sigmundgranaas.forgero.toolhandler.DynamicMiningLevel;
import com.sigmundgranaas.forgero.toolhandler.DynamicMiningSpeed;
import com.sigmundgranaas.forgero.toolhandler.LuckHandler;

import net.minecraft.entity.player.PlayerEntity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public interface DynamicAttributeItem extends DynamicAttributeTool, DynamicDurability, DynamicEffectiveNess, DynamicMiningLevel, DynamicMiningSpeed {
	LoadingCache<ItemStack, ImmutableMultimap<EntityAttribute, EntityAttributeModifier>> multiMapCache = CacheBuilder.newBuilder()
			.maximumSize(600)
			.expireAfterAccess(Duration.of(1, ChronoUnit.MINUTES))
			.build(new CacheLoader<>() {
				@Override
				public @NotNull
				ImmutableMultimap<EntityAttribute, EntityAttributeModifier> load(@NotNull ItemStack stack) {
					return ImmutableMultimap.<EntityAttribute, EntityAttributeModifier>builder().build();
				}
			});

	LoadingCache<ItemStack, Float> miningSpeedCache = CacheBuilder.newBuilder()
			.maximumSize(600)
			.expireAfterAccess(Duration.of(1, ChronoUnit.MINUTES))
			.build(new CacheLoader<>() {
				@Override
				public @NotNull
				Float load(@NotNull ItemStack stack) {
					return 1f;
				}
			});

	UUID ADDITION_HEALTH_MODIFIER_ID = UUID.randomUUID();
	UUID ADDITION_REACH_MODIFIER_ID = UUID.randomUUID();
	UUID ADDITION_REACH_ATTACK_MODIFIER_ID = UUID.randomUUID();

	UUID ADDITION_LUCK_MODIFIER_ID = UUID.fromString("CC3F55D5-755C-4F38-A497-9C13A33DB5CF");
	UUID ADDITION_ARMOR_MODIFIER_ID = UUID.fromString("AC3F55D5-755C-4F38-A497-9C13A63DB5CF");


	PropertyContainer dynamicProperties(ItemStack stack);

	PropertyContainer defaultProperties();

	default boolean isEquippable() {
		return true;
	}

	@Override
	default int getMiningLevel(ItemStack stack) {
		return ComputedAttribute.of(dynamicProperties(stack), MiningLevel.KEY).asInt();
	}

	@Override
	default int getMiningLevel() {
		return ComputedAttribute.of(defaultProperties(), MiningLevel.KEY).asInt();
	}

	default boolean isCorrectMiningLevel(BlockState state) {
		int level = this.getMiningLevel();

		for (int i = 1; i < 10; i++) {
			TagKey<Block> key = TagKey.of(Registries.BLOCK.getKey(), new Identifier(String.format("fabric:needs_tool_level_%s", i)));
			if (state.isIn(key) && level < i) {
				return false;
			}
		}
		if (state.isIn(BlockTags.NEEDS_DIAMOND_TOOL) && level < 3) {
			return false;
		} else if (state.isIn(BlockTags.NEEDS_IRON_TOOL) && level < 2) {
			return false;
		} else return !state.isIn(BlockTags.NEEDS_STONE_TOOL) || level >= 1;
	}

	@Override
	default Multimap<EntityAttribute, EntityAttributeModifier> getDynamicModifiers(EquipmentSlot slot, ItemStack stack, @Nullable LivingEntity user) {
		if (slot.equals(EquipmentSlot.MAINHAND) && stack.getItem() instanceof DynamicAttributeItem && isEquippable()) {
			try {
				return multiMapCache.get(stack, () -> createMultiMap(stack, user));
			} catch (ExecutionException e) {
				return EMPTY;
			}
		} else {
			return EMPTY;
		}
	}

	private ImmutableMultimap<EntityAttribute, EntityAttributeModifier> createMultiMap(ItemStack stack, LivingEntity user) {
		Matchable target = Matchable.DEFAULT_TRUE;
		MatchContext context = MatchContext.of();
		ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
		//Doing -1 to match vanilla
		float currentToolDamage = ComputedAttribute.apply(dynamicProperties(stack), AttackDamage.KEY, target, context) - 1;
		//Base attack damage
		builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ItemUUIDMixin.getAttackDamageModifierID(), "Tool modifier", currentToolDamage, EntityAttributeModifier.Operation.ADDITION));

		//Additional luck
		int luck = LuckHandler.of(dynamicProperties(stack)).map(ComputedAttribute::asInt).orElse(0);
		builder.put(EntityAttributes.GENERIC_LUCK, new EntityAttributeModifier(ADDITION_LUCK_MODIFIER_ID, "Luck addition", luck, EntityAttributeModifier.Operation.ADDITION));

		//Additional armor
		float armor = ComputedAttribute.of(dynamicProperties(stack), Armor.KEY).asFloat();
		builder.put(EntityAttributes.GENERIC_ARMOR, new EntityAttributeModifier(ADDITION_ARMOR_MODIFIER_ID, "Armor addition", armor, EntityAttributeModifier.Operation.ADDITION));

		//Additional health
		int additionalHealth = AdditionalHealthHandler.of(dynamicProperties(stack)).map(ComputedAttribute::asInt).orElse(0);
		builder.put(EntityAttributes.GENERIC_MAX_HEALTH, new EntityAttributeModifier(ADDITION_HEALTH_MODIFIER_ID, "Health addition", additionalHealth, EntityAttributeModifier.Operation.ADDITION));

		//Attack speed
		float currentAttackSpeed = ComputedAttribute.apply(dynamicProperties(stack), AttackSpeed.KEY, target, context);
		float baseAttackSpeed = ComputedAttribute.apply(defaultProperties(), AttackSpeed.KEY);
		builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(ItemUUIDMixin.getAttackSpeedModifierID(), "Tool attack speed", currentAttackSpeed, EntityAttributeModifier.Operation.ADDITION));

		// Reach
		boolean isCreative =  user instanceof PlayerEntity player && player.isCreative();
		float modifier = isCreative ? 0.5f : 0f;
		float baseReach = isCreative ? 5 : 4.5f;
		float reach = ComputedAttribute.apply(dynamicProperties(stack), Reach.KEY) + modifier;
		float diff = reach - baseReach;

		if(diff != 0){
			builder.put(ReachEntityAttributes.REACH, new EntityAttributeModifier(ADDITION_REACH_MODIFIER_ID, "Forgero reach", diff, EntityAttributeModifier.Operation.ADDITION));
			builder.put(ReachEntityAttributes.ATTACK_RANGE, new EntityAttributeModifier(ADDITION_REACH_ATTACK_MODIFIER_ID, "Forgero attack reach", diff, EntityAttributeModifier.Operation.ADDITION));
		}

		if (ForgeroConfigurationLoader.configuration.useEntityAttributes) {
			// Mining speed
			float miningSpeed = MiningSpeed.apply(dynamicProperties(stack));
			builder.put(MINING_SPEED, new EntityAttributeModifier(BASE_MINING_SPEED_ID, "Tool modifier", miningSpeed, EntityAttributeModifier.Operation.ADDITION));

			// Durability
			int durability = ComputedAttribute.of(dynamicProperties(stack), Durability.KEY).asInt();
			builder.put(DURABILITY, new EntityAttributeModifier(BASE_DURABILITY_ID, "Tool modifier", durability, EntityAttributeModifier.Operation.ADDITION));

			// Mining Level
			int miningLevel = ComputedAttribute.of(dynamicProperties(stack), MiningLevel.KEY).asInt();
			if (miningLevel != 0) {
				builder.put(MINING_LEVEL, new EntityAttributeModifier(BASE_MINING_LEVEL_ID, "Tool modifier", miningLevel, EntityAttributeModifier.Operation.ADDITION));
			}
		}
		return builder.build();
	}

	@Override
	default int getDurability(ItemStack stack) {
		return ComputedAttribute.of(dynamicProperties(stack), Durability.KEY).asInt();
	}

	default int getItemBarStep(ItemStack stack) {
		var durability = getDurability(stack);
		return durability == 0 ? 0 : Math.round(13.0F - (float) stack.getDamage() * 13.0F / (float) durability);
	}

	default int getDurabilityColor(ItemStack stack) {
		var durability = (float) getDurability(stack);
		float f = Math.max(0.0F, (durability - (float) stack.getDamage()) / durability);
		return MathHelper.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
	}

	@Override
	default float getMiningSpeedMultiplier(BlockState state, ItemStack stack) {
		if (stack.getItem() instanceof State dynamic && isEffectiveOn(state, stack)) {
			try {
				return miningSpeedCache.get(stack, () -> mingSpeedCalculation(StateService.INSTANCE.convert(stack).orElse(dynamic), state));
			} catch (ExecutionException e) {
				return 1f;
			}
		}
		return 1f;
	}

	@Override
	default boolean isEffectiveOn(BlockState state, ItemStack stack) {
		return isEffective(state, stack) && isCorrectMiningLevel(state, getMiningLevel(stack));
	}

	private float mingSpeedCalculation(PropertyContainer dynamic, BlockState state) {
		// Todo Fix the BlockBreaking predicate
		//Target target = new BlockBreakingEfficiencyTarget(state);
		return MiningSpeed.apply(dynamic, Matchable.DEFAULT_TRUE, MatchContext.of());
	}
}
