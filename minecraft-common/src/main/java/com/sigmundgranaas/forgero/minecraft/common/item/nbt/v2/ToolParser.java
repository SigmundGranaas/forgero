package com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2;

import com.sigmundgranaas.forgero.core.registry.StateFinder;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedTool;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.SlotContainer;
import com.sigmundgranaas.forgero.core.type.Type;
import net.minecraft.nbt.NbtCompound;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.SOUL_IDENTIFIER;
import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.TYPE_IDENTIFIER;


public class ToolParser extends CompositeParser {
    public ToolParser(StateFinder supplier) {
        super(supplier);
    }

    @Override
    public Optional<State> parse(NbtCompound compound) {
        List<State> parts = new ArrayList<>();
        parseParts(parts::add, compound);
        var id = compound.getString(NbtConstants.ID_IDENTIFIER);
        var stateOpt = supplier.find(id);
        if (parts.size() == 2) {
            var optBuilder = ConstructedTool.ToolBuilder.builder(parts);
            if (optBuilder.isPresent()) {
                var builder = optBuilder.get();
                builder.id(id);
                if (stateOpt.isPresent() && stateOpt.get() instanceof Composite upgradeable) {
                    builder.addSlotContainer(new SlotContainer(upgradeable.slots()));
                }
                parseUpgrades(builder::addUpgrade, compound);
                if (compound.contains(TYPE_IDENTIFIER)) {
                    builder.type(Type.of(compound.getString(TYPE_IDENTIFIER)));
                }
                if (compound.contains(SOUL_IDENTIFIER)) {
                    var soul = SoulParser.PARSER.parse(compound.getCompound(SOUL_IDENTIFIER));
                    if (soul.isPresent()) {
                        return Optional.of(builder.soul(soul.get()).build());
                    }
                }
                return Optional.of(builder.build());
            }
        }

        return Optional.empty();
    }
}
