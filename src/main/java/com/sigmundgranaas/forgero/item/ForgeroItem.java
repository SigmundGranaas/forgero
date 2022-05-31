package com.sigmundgranaas.forgero.item;

import com.sigmundgranaas.forgero.core.data.ForgeroDataResource;
import com.sigmundgranaas.forgero.core.resource.ForgeroResource;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public interface ForgeroItem<T extends Item, R extends ForgeroDataResource> extends ForgeroResource<R> {
    T getItem();

    default Identifier getIdentifier() {
        return new Identifier(getNameSpace(), getStringIdentifier());
    }
}
