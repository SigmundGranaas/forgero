package com.sigmundgranaas.forgero.resource.data.v2;

import com.sigmundgranaas.forgero.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.state.Identifiable;

import java.util.Collections;
import java.util.List;

import static com.sigmundgranaas.forgero.util.Identifiers.EMPTY_IDENTIFIER;

public interface DataPackage extends Identifiable {
    static DataPackage of(List<DataResource> data) {
        return new DefaultPackage(Collections.emptyList(), 6, data, EMPTY_IDENTIFIER, EMPTY_IDENTIFIER);
    }

    static DataPackage of(DataResource data) {
        return new DefaultPackage(data.dependencies(), 6, List.of(data), data.nameSpace(), data.name());
    }

    int priority();

    List<String> dependencies();

    List<DataResource> data();
}
