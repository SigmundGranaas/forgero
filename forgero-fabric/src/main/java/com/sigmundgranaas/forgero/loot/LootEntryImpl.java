package com.sigmundgranaas.forgero.loot;

import lombok.Builder;
import lombok.Data;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.util.Identifier;

@Data
@Builder
public class LootEntryImpl implements LootEntry {
    private Identifier target;
    private StateFilter filter;
    private int weight;

    @Override
    public void apply(LootTable.Builder builder) {
        var pool = LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1));
        filter.filter().forEach(item -> pool.with(ItemEntry.builder(item).weight(1)));
        builder.pool(pool);
    }

    @Override
    public boolean matches(Identifier id) {
        return id.equals(target);
    }
}
