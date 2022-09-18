package com.sigmundgranaas.forgero.registry;

import com.sigmundgranaas.forgero.ForgeroResourceRegistry;
import com.sigmundgranaas.forgero.resource.data.v1.ForgeroDataResource;
import com.sigmundgranaas.forgero.item.ForgeroItem;

public interface ForgeroItemResourceRegistry<R extends ForgeroDataResource, T extends ForgeroItem<?, R>> extends ForgeroResourceRegistry<T> {
    void register(RegistryHandler handler);
}
