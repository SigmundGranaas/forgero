package com.sigmundgranaas.forgero.item;

import com.sigmundgranaas.forgero.core.ForgeroResource;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public interface ForgeroItem<T extends Item> extends ForgeroResource {
    T getItem();

    default Identifier getIdentifier() {
        return new Identifier(getNameSpace(), getStringIdentifier());
    }
}
