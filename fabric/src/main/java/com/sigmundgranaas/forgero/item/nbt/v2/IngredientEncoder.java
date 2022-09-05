package com.sigmundgranaas.forgero.item.nbt.v2;

import com.sigmundgranaas.forgerocore.state.Composite;
import com.sigmundgranaas.forgerocore.state.Ingredient;
import net.minecraft.nbt.NbtCompound;

import static com.sigmundgranaas.forgero.item.nbt.v2.NbtConstants.INGREDIENT_IDENTIFIER;
import static com.sigmundgranaas.forgero.item.nbt.v2.NbtConstants.STATE_TYPE_IDENTIFIER;

public class IngredientEncoder implements CompoundEncoder<Ingredient> {
    private final IdentifiableEncoder identifiableEncoder;

    public IngredientEncoder() {
        this.identifiableEncoder = new IdentifiableEncoder();
    }

    @Override
    public NbtCompound encode(Ingredient element) {
        if (element instanceof Composite composite) {
            return new CompositeEncoder().encode(composite);
        }
        var compound = identifiableEncoder.encode(element);

        compound.putString(STATE_TYPE_IDENTIFIER, INGREDIENT_IDENTIFIER);
        return compound;
    }
}
