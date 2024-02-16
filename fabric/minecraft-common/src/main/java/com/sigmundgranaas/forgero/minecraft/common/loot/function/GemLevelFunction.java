package com.sigmundgranaas.forgero.minecraft.common.loot.function;

import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.CompoundEncoder.ENCODER;
import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.FORGERO_IDENTIFIER;
import static com.sigmundgranaas.forgero.minecraft.common.registry.registrar.LootFunctionRegistrar.GEM_LOOT_FUNCTION_TYPE;
import static java.lang.Math.exp;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.core.state.LeveledState;
import com.sigmundgranaas.forgero.minecraft.common.item.StateItem;

import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.SetDamageLootFunction;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;

import java.util.List;

public class GemLevelFunction extends ConditionalLootFunction {
	public static Codec<GemLevelFunction> CODEC = RecordCodecBuilder
			.create((instance) -> addConditionsField(instance)
					.apply(instance,(GemLevelFunction::new)));

	protected GemLevelFunction(List<LootCondition> conditions) {
		super(conditions);
	}

	public static GemLevelFunction of(List<LootCondition> conditions) {
		return new GemLevelFunction(conditions);
	}

	@Override
	protected ItemStack process(ItemStack stack, LootContext context) {
		if (stack.getItem() instanceof StateItem stateItem && stateItem.defaultState() instanceof LeveledState levelAble) {
			int lambda = 2;
			double rand = context.getRandom().nextDouble();
			double u_min = exp(-1 * lambda);
			double u_max = exp(-9 * lambda);
			double u = u_min + (1.0 - rand / (1 + 1.0)) * (u_max - u_min);
			double number = Math.log(u) / -lambda;

			var leveled = levelAble.setLevel((int) number);
			var newStack = stack.copy();
			var nbt = ENCODER.encode(leveled);
			newStack.getOrCreateNbt().put(FORGERO_IDENTIFIER, nbt);
			newStack.getOrCreateNbt().putInt("CustomModelData", leveled.level());
			return newStack;
		}
		return stack;
	}

	@Override
	public LootFunctionType getType() {
		return GEM_LOOT_FUNCTION_TYPE;
	}

	public static class Builder extends ConditionalLootFunction.Builder<GemLevelFunction.Builder> {


		public Builder() {
		}

		protected GemLevelFunction.Builder getThisBuilder() {
			return this;
		}

		public LootFunction build() {
			return new GemLevelFunction(this.getConditions());
		}
	}
}
