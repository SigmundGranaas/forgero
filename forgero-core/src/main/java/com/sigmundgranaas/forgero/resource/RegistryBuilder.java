package com.sigmundgranaas.forgero.resource;

import com.sigmundgranaas.forgero.ResourceRegistry;
import com.sigmundgranaas.forgero.resource.data.StateConverter;
import com.sigmundgranaas.forgero.resource.data.v2.DataCollection;
import com.sigmundgranaas.forgero.state.State;

public class RegistryBuilder implements Mapper<ResourceRegistry<State>, DataCollection> {
    @Override
    public ResourceRegistry<State> map(DataCollection collection) {
        StateConverter converter = new StateConverter(collection.tree());
        collection.resources().forEach(converter::convert);

        return ResourceRegistry.of(converter.states(), collection.tree());
    }
}
