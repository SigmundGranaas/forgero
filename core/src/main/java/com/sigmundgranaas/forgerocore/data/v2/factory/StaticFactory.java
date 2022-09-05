package com.sigmundgranaas.forgerocore.data.v2.factory;

import com.sigmundgranaas.forgerocore.data.v2.data.StaticData;
import com.sigmundgranaas.forgerocore.data.v2.data.TypeData;
import com.sigmundgranaas.forgerocore.data.v2.json.Constants;
import com.sigmundgranaas.forgerocore.data.v2.json.JsonResource;

import java.util.Optional;

public class StaticFactory {
    public Optional<StaticData> createStaticData(JsonResource resource) {
        if (resource.jsonStatic == null) {
            return Optional.empty();
        }
        if (resource.jsonStatic.name.equals(Constants.THIS)) {
            return Optional.of(new StaticData(resource.name, new TypeData(resource.jsonStatic.type, Optional.empty())));
        }
        return Optional.empty();
    }
}
