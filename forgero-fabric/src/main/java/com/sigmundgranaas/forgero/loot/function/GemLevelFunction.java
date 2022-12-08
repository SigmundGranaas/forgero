package com.sigmundgranaas.forgero.loot.function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.sigmundgranaas.forgero.item.StateItem;
import com.sigmundgranaas.forgero.state.LeveledState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;

import static com.sigmundgranaas.forgero.item.nbt.v2.CompoundEncoder.ENCODER;
import static com.sigmundgranaas.forgero.item.nbt.v2.NbtConstants.FORGERO_IDENTIFIER;

public class GemLevelFunction extends ConditionalLootFunction {
    private int level = 1;

    protected GemLevelFunction(LootCondition[] conditions, int level) {
        super(conditions);
        this.level = level;
    }

    public static GemLevelFunction of(LootCondition[] conditions, int level) {
        return new GemLevelFunction(conditions, level);
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        if (stack.getItem() instanceof StateItem stateItem && stateItem.defaultState() instanceof LeveledState levelAble) {
            var leveled = levelAble.setLevel(this.level);
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
        return new LootFunctionType(new GemLevelFunction.Serializer());
    }

    public static class Builder extends ConditionalLootFunction.Builder<GemLevelFunction.Builder> {
        private int level;

        public Builder() {
        }

        protected GemLevelFunction.Builder getThisBuilder() {
            return this;
        }

        public GemLevelFunction.Builder add(int level) {
            this.level = level;
            return this;
        }

        public LootFunction build() {
            return new GemLevelFunction(this.getConditions(), level);
        }
    }

    public static class Serializer extends ConditionalLootFunction.Serializer<GemLevelFunction> {
        public Serializer() {
        }

        public void toJson(JsonObject jsonObject, GemLevelFunction function, JsonSerializationContext jsonSerializationContext) {
            super.toJson(jsonObject, function, jsonSerializationContext);
            jsonObject.addProperty("level", function.level);
        }

        public GemLevelFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] lootConditions) {
            return new GemLevelFunction(lootConditions, jsonObject.get("level").getAsInt());
        }
    }


}
