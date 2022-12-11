package com.sigmundgranaas.forgero.resource.data.v2;

import com.sigmundgranaas.forgero.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.type.TypeTree;

import java.util.List;

public record DataCollection(TypeTree tree, List<DataResource> resources) {
}
