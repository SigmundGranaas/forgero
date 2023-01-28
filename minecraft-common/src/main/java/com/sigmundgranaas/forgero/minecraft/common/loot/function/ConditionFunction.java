package com.sigmundgranaas.forgero.minecraft.common.loot.function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.sigmundgranaas.forgero.core.condition.Conditional;
import com.sigmundgranaas.forgero.core.condition.Conditions;
import com.sigmundgranaas.forgero.core.condition.NamedCondition;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.minecraft.common.conversion.StateConverter;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.CompoundEncoder;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;

import java.util.Comparator;

import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.FORGERO_IDENTIFIER;
import static com.sigmundgranaas.forgero.minecraft.common.loot.function.LootFunctions.CONDITION_LOOT_FUNCTION_TYPE;

public class ConditionFunction extends ConditionalLootFunction {
    protected ConditionFunction(LootCondition[] conditions) {
        super(conditions);
    }

    public static ConditionFunction of(LootCondition[] conditions) {
        return new ConditionFunction(conditions);
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        var converted = StateConverter.of(stack);
        if (converted.isPresent() && converted.get() instanceof Conditional<?> conditional) {
            var conditions = Conditions.INSTANCE.all().stream()
                    .filter(condition -> condition.isApplicable(conditional))
                    .filter(condition -> context.getRandom().nextDouble() < condition.getChance())
                    .sorted(Comparator.comparing(com.sigmundgranaas.forgero.core.condition.LootCondition::getPriority))
                    .toList();
            Conditional<?> conditioned = conditional;
            for (NamedCondition container : conditions) {
                conditioned = (Conditional<?>) conditioned.applyCondition(container);
            }
            if (conditioned instanceof PropertyContainer container) {
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
            return new ConditionFunction(this.getConditions());
        }
    }

    public static class Serializer extends ConditionalLootFunction.Serializer<ConditionFunction> {
        public Serializer() {
        }

        public void toJson(JsonObject jsonObject, ConditionFunction function, JsonSerializationContext jsonSerializationContext) {
            super.toJson(jsonObject, function, jsonSerializationContext);
        }

        public ConditionFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] lootConditions) {
            return new ConditionFunction(lootConditions);
        }
    }
}
