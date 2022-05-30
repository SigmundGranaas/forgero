package com.sigmundgranaas.forgero.registry.impl;

import com.sigmundgranaas.forgero.core.registry.impl.ConcurrentResourceRegistry;
import com.sigmundgranaas.forgero.item.ForgeroItem;
import com.sigmundgranaas.forgero.registry.RegistryHandler;

import java.util.Map;

public abstract class ConcurrentItemResourceRegistry<T extends ForgeroItem<?>> extends ConcurrentResourceRegistry<T> {
    protected ConcurrentItemResourceRegistry(Map<String, T> resources) {
        super(resources);
    }

    public void register(RegistryHandler helper) {
        getResourcesAsList().forEach(resource -> helper.register(resource.getItem(), resource.getIdentifier()));
    }
}
