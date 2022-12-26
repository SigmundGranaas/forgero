package com.sigmundgranaas.forgero.item.nbt.v2;

import com.sigmundgranaas.forgero.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.registry.StateFinder;
import com.sigmundgranaas.forgero.state.Composite;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.type.Type;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.sigmundgranaas.forgero.item.nbt.v2.NbtConstants.*;

@SuppressWarnings("ClassCanBeRecord")
public class CompositeParser implements CompoundParser<State> {
    private final StateFinder supplier;

    public CompositeParser(StateFinder supplier) {
        this.supplier = supplier;
    }

    @Override
    public Optional<State> parse(NbtCompound compound) {
        if (!compound.contains(STATE_TYPE_IDENTIFIER)) {
            return Optional.empty();
        }
        Composite.CompositeBuilder builder = Composite.builder();

        if (compound.contains(ID_IDENTIFIER)) {
            var id = compound.getString(ID_IDENTIFIER);
            var stateOpt = supplier.find(id);

            if (stateOpt.isPresent() && stateOpt.get() instanceof Composite composite) {
                builder = Composite.builder(composite.slots());
            } else if (ForgeroStateRegistry.CONTAINER_TO_STATE.containsKey(id)) {
                return supplier.find(ForgeroStateRegistry.CONTAINER_TO_STATE.get(id));
            }
            builder.id(id);
        } else {
            if (compound.contains(NAME_IDENTIFIER)) {
                builder.name(compound.getString(NAME_IDENTIFIER));
            }

            if (compound.contains(NAMESPACE_IDENTIFIER)) {
                builder.nameSpace(compound.getString(NAMESPACE_IDENTIFIER));
            }
        }
        if (compound.contains(TYPE_IDENTIFIER)) {
            builder.type(Type.of(compound.getString(TYPE_IDENTIFIER)));
        }
        if (compound.contains(INGREDIENTS_IDENTIFIER)) {
            parseEntries(compound.getList(INGREDIENTS_IDENTIFIER, NbtElement.COMPOUND_TYPE)).forEach(builder::addIngredient);
        }
        if (compound.contains(INGREDIENTS_IDENTIFIER)) {
            parseEntries(compound.getList(INGREDIENTS_IDENTIFIER, NbtElement.STRING_TYPE)).forEach(builder::addIngredient);
        }
        if (compound.contains(UPGRADES_IDENTIFIER)) {
            parseEntries(compound.getList(UPGRADES_IDENTIFIER, NbtElement.COMPOUND_TYPE)).forEach(builder::addUpgrade);
        }
        if (compound.contains(UPGRADES_IDENTIFIER)) {
            parseEntries(compound.getList(UPGRADES_IDENTIFIER, NbtElement.STRING_TYPE)).forEach(builder::addUpgrade);
        }
        return Optional.of(builder.build());
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
        if (compound.contains(STATE_TYPE_IDENTIFIER)) {
            if (compound.getString(STATE_TYPE_IDENTIFIER).equals(STATE_IDENTIFIER)) {
                return supplier.apply(compound.getString(ID_IDENTIFIER));
            } else if (compound.getString(STATE_TYPE_IDENTIFIER).equals(COMPOSITE_IDENTIFIER)) {
                return parse(compound);
            } else if (compound.getString(STATE_TYPE_IDENTIFIER).equals(LEVELED_IDENTIFIER)) {
                return new StateParser(this.supplier).parse(compound);
            }
        }
        return parse(compound);
    }
}
