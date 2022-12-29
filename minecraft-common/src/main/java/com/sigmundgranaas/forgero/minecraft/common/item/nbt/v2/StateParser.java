package com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2;

import com.sigmundgranaas.forgero.registry.StateFinder;
import com.sigmundgranaas.forgero.state.State;
import net.minecraft.nbt.NbtCompound;

import java.util.Optional;

import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.*;

public class StateParser implements CompoundParser<State> {
    private final StateFinder supplier;
    private final CompositeParser compositeParser;
    private final LeveledParser leveledParser;

    public StateParser(StateFinder supplier) {
        this.supplier = supplier;
        this.compositeParser = new CompositeParser(supplier);
        this.leveledParser = new LeveledParser(supplier);
    }

    @Override
    public Optional<State> parse(NbtCompound compound) {
        if (!compound.contains(STATE_TYPE_IDENTIFIER)) {
            return Optional.empty();
        } else if (compound.getString(STATE_TYPE_IDENTIFIER).equals(COMPOSITE_IDENTIFIER)) {
            return compositeParser.parse(compound);
        } else if (compound.getString(STATE_TYPE_IDENTIFIER).equals(LEVELED_IDENTIFIER)) {
            return leveledParser.parse(compound);
        }

        return supplier.find(compound.getString(ID_IDENTIFIER));
    }

}
