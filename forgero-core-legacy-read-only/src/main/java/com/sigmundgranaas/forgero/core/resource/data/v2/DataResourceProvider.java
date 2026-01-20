package com.sigmundgranaas.forgero.core.resource.data.v2;

import com.sigmundgranaas.forgero.core.resource.data.v2.data.DataResource;

import java.util.Optional;
import java.util.function.Supplier;

public interface DataResourceProvider extends Supplier<Optional<DataResource>> {
}
