package com.sigmundgranaas.forgero.item.nbt.v2;

import com.sigmundgranaas.forgero.registry.IngredientSupplier;
import com.sigmundgranaas.forgero.registry.UpgradeSupplier;
import com.sigmundgranaas.forgero.state.Composite;
import com.sigmundgranaas.forgero.state.Ingredient;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.state.Upgrade;
import com.sigmundgranaas.forgero.type.Type;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.sigmundgranaas.forgero.item.nbt.v2.NbtConstants.*;

@SuppressWarnings("ClassCanBeRecord")
public class CompositeParser implements CompoundParser<Composite> {
    private final IngredientSupplier ingredientSupplier;
    private final UpgradeSupplier upgradeSupplier;

    public CompositeParser(IngredientSupplier ingredientSupplier, UpgradeSupplier upgradeSupplier) {
        this.ingredientSupplier = ingredientSupplier;
        this.upgradeSupplier = upgradeSupplier;
    }

    @Override
    public Optional<Composite> parse(NbtCompound compound) {
        if (!compound.contains(STATE_TYPE_IDENTIFIER)) {
            return Optional.empty();
        }
        Composite.CompositeBuilder builder = Composite.builder();

        if (compound.contains(ID_IDENTIFIER)) {
            var id = compound.getString(ID_IDENTIFIER);
            var stateOpt = ingredientSupplier.get(id);

            if (stateOpt.isPresent() && stateOpt.get() instanceof Composite composite) {
                builder = Composite.builder(composite.slots());
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

        parseIngredients(compound).forEach(builder::addIngredient);
        parseUpgrades(compound).forEach(builder::addUpgrade);

        return Optional.of(builder.build());
    }

    private List<State> parseIngredients(NbtCompound compound) {
        if (compound.contains(INGREDIENTS_IDENTIFIER)) {
            return compound.getList(INGREDIENTS_IDENTIFIER, NbtElement.COMPOUND_TYPE)
                    .stream()
                    .map(this::parseIngredient)
                    .flatMap(Optional::stream)
                    .toList();
        }
        return Collections.emptyList();
    }

    private List<State> parseUpgrades(NbtCompound compound) {
        if (compound.contains(UPGRADES_IDENTIFIER)) {
            return compound.getList(UPGRADES_IDENTIFIER, NbtElement.COMPOUND_TYPE)
                    .stream()
                    .map(this::parseUpgrade)
                    .flatMap(Optional::stream)
                    .toList();
        }
        return Collections.emptyList();
    }

    private Optional<State> parseIngredient(NbtElement element) {
        if (element.getType() == NbtElement.STRING_TYPE) {
            return ingredientSupplier.get(element.asString());
        } else if (element.getType() == NbtElement.COMPOUND_TYPE) {
            if (element instanceof NbtCompound compound) {
                return parseCompound(compound, ingredientSupplier::get, Ingredient::of);
            } else {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private Optional<State> parseUpgrade(NbtElement element) {
        if (element.getType() == NbtElement.STRING_TYPE) {
            return upgradeSupplier.get(element.asString());
        } else if (element.getType() == NbtElement.COMPOUND_TYPE) {
            if (element instanceof NbtCompound compound) {
                return parseCompound(compound, upgradeSupplier::get, Upgrade::of);
            } else {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }


    private <T> Optional<T> parseCompound(NbtCompound compound, Function<String, Optional<T>> supplier, Function<Composite, T> compositeMapper) {
        if (compound.contains(STATE_TYPE_IDENTIFIER)) {
            if (compound.getString(STATE_TYPE_IDENTIFIER).equals(INGREDIENT_IDENTIFIER)) {
                return supplier.apply(compound.getString(ID_IDENTIFIER));
            } else if (compound.getString(STATE_TYPE_IDENTIFIER).equals(COMPOSITE_IDENTIFIER)) {
                return parse(compound).map(compositeMapper);
            }
        }
        return Optional.empty();
    }
}
