package com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2;

import com.sigmundgranaas.forgero.core.registry.StateFinder;
import com.sigmundgranaas.forgero.core.state.State;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.COMPOSITE_TYPE;
import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.TOOL_IDENTIFIER;


public class CompositeParser implements CompoundParser<State> {
    protected final StateFinder supplier;

    public CompositeParser(StateFinder supplier) {
        this.supplier = supplier;
    }

    @Override
    public Optional<State> parse(NbtCompound compound) {
        if (!compound.contains(NbtConstants.STATE_TYPE_IDENTIFIER)) {
            return Optional.empty();
        }
        if (compound.contains(COMPOSITE_TYPE) && compound.getString(COMPOSITE_TYPE).equals(TOOL_IDENTIFIER)) {
            return new ToolParser(supplier).parse(compound);
        }
        return new ConstructParser(supplier).parse(compound);
    }

    protected void parseParts(Consumer<State> partConsumer, NbtCompound compound) {
        if (compound.contains(NbtConstants.INGREDIENTS_IDENTIFIER)) {
            parseEntries(compound.getList(NbtConstants.INGREDIENTS_IDENTIFIER, NbtElement.COMPOUND_TYPE)).forEach(partConsumer);
        }
        if (compound.contains(NbtConstants.INGREDIENTS_IDENTIFIER)) {
            parseEntries(compound.getList(NbtConstants.INGREDIENTS_IDENTIFIER, NbtElement.STRING_TYPE)).forEach(partConsumer);
        }
    }

    protected void parseUpgrades(Consumer<State> partConsumer, NbtCompound compound) {
        if (compound.contains(NbtConstants.UPGRADES_IDENTIFIER)) {
            parseEntries(compound.getList(NbtConstants.UPGRADES_IDENTIFIER, NbtElement.COMPOUND_TYPE)).forEach(partConsumer);
        }
        if (compound.contains(NbtConstants.UPGRADES_IDENTIFIER)) {
            parseEntries(compound.getList(NbtConstants.UPGRADES_IDENTIFIER, NbtElement.STRING_TYPE)).forEach(partConsumer);
        }
    }

    private List<State> parseEntries(List<NbtElement> elements) {
        return elements
                .stream()
                .map(this::parseEntry)
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<State> parseEntry(NbtElement element) {
        if (element.getType() == NbtElement.STRING_TYPE) {
            return supplier.find(element.asString());
        } else if (element.getType() == NbtElement.COMPOUND_TYPE) {
            if (element instanceof NbtCompound compound) {
                return parseCompound(compound, supplier::find);
            } else {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private Optional<State> parseCompound(NbtCompound compound, Function<String, Optional<State>> supplier) {
        if (compound.contains(NbtConstants.STATE_TYPE_IDENTIFIER)) {
            if (compound.getString(NbtConstants.STATE_TYPE_IDENTIFIER).equals(NbtConstants.STATE_IDENTIFIER)) {
                return supplier.apply(compound.getString(NbtConstants.ID_IDENTIFIER));
            } else if (compound.getString(NbtConstants.STATE_TYPE_IDENTIFIER).equals(NbtConstants.COMPOSITE_IDENTIFIER)) {
                return parse(compound);
            } else if (compound.getString(NbtConstants.STATE_TYPE_IDENTIFIER).equals(NbtConstants.LEVELED_IDENTIFIER)) {
                return new StateParser(this.supplier).parse(compound);
            }
        }
        return parse(compound);
    }
}
