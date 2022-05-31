package com.sigmundgranaas.forgero.registry;

import com.sigmundgranaas.forgero.core.ForgeroResourceRegistry;
import com.sigmundgranaas.forgero.item.ForgeroItem;

public interface ForgeroItemResourceRegistry<T extends ForgeroItem<?>> extends ForgeroResourceRegistry<T> {
    void register(RegistryHandler handler);
}
