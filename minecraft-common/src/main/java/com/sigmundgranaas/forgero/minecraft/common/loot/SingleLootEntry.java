package com.sigmundgranaas.forgero.minecraft.common.loot;

import com.sigmundgranaas.forgero.minecraft.common.loot.function.ConditionFunction;
import com.sigmundgranaas.forgero.minecraft.common.loot.function.GemLevelFunction;
import lombok.Builder;
import lombok.Data;
import net.minecraft.item.Item;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.BinomialLootNumberProvider;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.function.Supplier;

@Data
@Builder
public class SingleLootEntry implements LootEntry {
    private List<Identifier> target;
    private Supplier<List<Item>> filter;
    private int weight;
    @Builder.Default
    private int rolls = 1;
    @Builder.Default
    private float chance = 1f;

    @Override
    public void apply(LootTable.Builder builder) {
        var pool = LootPool.builder()
                .rolls(BinomialLootNumberProvider.create(rolls, chance));
        filter.get().forEach(item -> pool.with(ItemEntry.builder(item).apply(new GemLevelFunction.Builder()).apply(new ConditionFunction.Builder()).weight(1)));
        builder.pool(pool);
    }

    @Override
    public boolean matches(Identifier id) {
        return target.stream().anyMatch(target -> target.equals(id));
    }
}
