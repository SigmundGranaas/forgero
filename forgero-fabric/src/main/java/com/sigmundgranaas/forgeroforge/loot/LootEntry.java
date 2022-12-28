package com.sigmundgranaas.forgeroforge.loot;

import net.minecraft.loot.LootTable;
import net.minecraft.util.Identifier;

import java.util.List;

public interface LootEntry {
    static LootEntry of(StateFilter filter, Identifier target) {
        return new SingleLootEntry.SingleLootEntryBuilder().weight(1).target(List.of(target)).filter(filter::filter).build();
    }

    static LootEntry of(StateFilter.StateFilterBuilder filter, Identifier target) {
        return new SingleLootEntry.SingleLootEntryBuilder().weight(1).target(List.of(target)).filter(filter.build()::filter).build();
    }

    void apply(LootTable.Builder builder);

    boolean matches(Identifier id);
}
