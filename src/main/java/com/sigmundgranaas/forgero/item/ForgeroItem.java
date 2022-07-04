package com.sigmundgranaas.forgero.item;

import com.sigmundgranaas.forgero.core.data.ForgeroDataResource;
import com.sigmundgranaas.forgero.core.resource.ForgeroResource;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public interface ForgeroItem<T extends Item> extends ForgeroResource, Comparable<Object> {
    T getItem();

    default Identifier getIdentifier() {
        return new Identifier(getNameSpace(), getStringIdentifier());
    }

    @Override
    default int compareTo(@NotNull Object o) {
        if (o == this) {
            return 0;
        }
        if (o instanceof ForgeroItem forgeroItem) {
            return this.getResourceName().compareTo(forgeroItem.getResourceName());
        }
        return 0;
    }
}
