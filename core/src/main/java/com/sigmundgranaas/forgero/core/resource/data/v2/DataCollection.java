package com.sigmundgranaas.forgero.core.resource.data.v2;

import com.sigmundgranaas.forgero.core.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.core.type.TypeTree;

import java.util.List;

public record DataCollection(TypeTree tree, List<DataResource> resources) {
}
