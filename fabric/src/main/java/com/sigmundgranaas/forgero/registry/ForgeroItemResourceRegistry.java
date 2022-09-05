package com.sigmundgranaas.forgero.registry;

import com.sigmundgranaas.forgerocore.ForgeroResourceRegistry;
import com.sigmundgranaas.forgerocore.data.v1.ForgeroDataResource;
import com.sigmundgranaas.forgero.item.ForgeroItem;

public interface ForgeroItemResourceRegistry<R extends ForgeroDataResource, T extends ForgeroItem<?, R>> extends ForgeroResourceRegistry<T> {
    void register(RegistryHandler handler);
}
