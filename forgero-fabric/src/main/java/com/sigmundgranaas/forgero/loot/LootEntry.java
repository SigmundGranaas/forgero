package com.sigmundgranaas.forgero.loot;

import net.minecraft.loot.LootTable;
import net.minecraft.util.Identifier;

public interface LootEntry {
    static LootEntry of(StateFilter filter, Identifier target) {
        return new LootEntryImpl.LootEntryImplBuilder().weight(1).target(target).filter(filter).build();
    }

    static LootEntry of(StateFilter.StateFilterBuilder filter, Identifier target) {
        return new LootEntryImpl.LootEntryImplBuilder().weight(1).target(target).filter(filter.build()).build();
    }

    void apply(LootTable.Builder builder);

    boolean matches(Identifier id);
}
