package com.sigmundgranaas.forgero.item.nbt.v2;

import com.sigmundgranaas.forgero.core.registry.IngredientSupplier;
import com.sigmundgranaas.forgero.core.registry.StateSupplier;
import com.sigmundgranaas.forgero.core.registry.UpgradeSupplier;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.State;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.Optional;

import static com.sigmundgranaas.forgero.item.nbt.v2.NbtConstants.*;

public class CompositeParser implements NbtStateParser {
    private final IngredientSupplier ingredientSupplier;
    private final StateSupplier stateSupplier;
    private final UpgradeSupplier upgradeSupplier;

    public CompositeParser(IngredientSupplier ingredientSupplier, StateSupplier stateSupplier, UpgradeSupplier upgradeSupplier) {
        this.ingredientSupplier = ingredientSupplier;
        this.stateSupplier = stateSupplier;
        this.upgradeSupplier = upgradeSupplier;
    }

    @Override
    public Optional<State> parse(NbtCompound compound) {
        if (!compound.contains(STATE_TYPE_IDENTIFIER)) {
            return Optional.empty();
        }
        Composite.CompositeBuilder builder = Composite.builder();
        if (compound.contains(NAME_IDENTIFIER)) {
            builder.name(compound.getString(NAME_IDENTIFIER));
        }

        //noinspection StatementWithEmptyBody
        if (compound.contains(NAMESPACE_IDENTIFIER)) {
            //builder.name(compound.getString(NAMESPACE_IDENTIFIER));
        }

        if (compound.contains(INGREDIENTS_IDENTIFIER)) {
            compound.getList(INGREDIENTS_IDENTIFIER, NbtElement.STRING_TYPE)
                    .stream()
                    .map(NbtElement::asString)
                    .map(ingredientSupplier::get)
                    .flatMap(Optional::stream)
                    .forEach(builder::add);
        }

        if (compound.contains(UPGRADES_IDENTIFIER)) {
            compound.getList(UPGRADES_IDENTIFIER, NbtElement.STRING_TYPE)
                    .stream()
                    .map(NbtElement::asString)
                    .map(upgradeSupplier::get)
                    .flatMap(Optional::stream)
                    .forEach(builder::add);
        }

        return Optional.of(builder.build());
    }
}
