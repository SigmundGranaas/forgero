package com.sigmundgranaas.forgero.minecraft.common.loot.function;

import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.FORGERO_IDENTIFIER;
import static com.sigmundgranaas.forgero.minecraft.common.registry.registrar.LootFunctionRegistrar.CONDITION_LOOT_FUNCTION_TYPE;

import java.util.Comparator;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.sigmundgranaas.forgero.core.condition.Conditional;
import com.sigmundgranaas.forgero.core.condition.Conditions;
import com.sigmundgranaas.forgero.core.condition.NamedCondition;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.CompoundEncoder;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;

public class ConditionFunction extends ConditionalLootFunction {
	private final StateService service;

	protected ConditionFunction(LootCondition[] conditions, StateService service) {
		super(conditions);
		this.service = service;
	}

	public static ConditionFunction of(LootCondition[] conditions) {
		return new ConditionFunction(conditions, StateService.INSTANCE);
	}

	@Override
	protected ItemStack process(ItemStack stack, LootContext context) {
		var converted = service.convert(stack);
		if (converted.isPresent() && converted.get() instanceof Conditional<?> conditional) {
			var conditions = Conditions.INSTANCE.all().stream()
					.filter(condition -> condition.isApplicable(conditional))
					.filter(condition -> context.getRandom().nextDouble() < condition.getChance() + context.getLuck())
					.sorted(Comparator.comparing(com.sigmundgranaas.forgero.core.condition.LootCondition::getPriority))
					.toList();
			Conditional<?> conditioned = conditional;
			for (NamedCondition container : conditions) {
				conditioned = (Conditional<?>) conditioned.applyCondition(container);
			}
			if (conditioned instanceof PropertyContainer container && conditions.size() > 0) {
				stack.getOrCreateNbt().put(FORGERO_IDENTIFIER, CompoundEncoder.ENCODER.encode(container));
			}
		}
		return stack;
	}

	@Override
	public LootFunctionType getType() {
		return CONDITION_LOOT_FUNCTION_TYPE;
	}

	public static class Builder extends ConditionalLootFunction.Builder<ConditionFunction.Builder> {


		public Builder() {
		}

		protected ConditionFunction.Builder getThisBuilder() {
			return this;
		}

		public LootFunction build() {
			return new ConditionFunction(this.getConditions(), StateService.INSTANCE);
		}
	}

	public static class Serializer extends ConditionalLootFunction.Serializer<ConditionFunction> {
		public Serializer() {
		}

		public void toJson(JsonObject jsonObject, ConditionFunction function, JsonSerializationContext jsonSerializationContext) {
			super.toJson(jsonObject, function, jsonSerializationContext);
		}

		public ConditionFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] lootConditions) {
			return new ConditionFunction(lootConditions, StateService.INSTANCE);
		}
	}
}
