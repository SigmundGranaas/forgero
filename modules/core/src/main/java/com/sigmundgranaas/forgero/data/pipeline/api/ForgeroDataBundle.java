package com.sigmundgranaas.forgero.data.pipeline.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.common.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.data.loading.api.data.host.HostData;

import java.util.Map;

public record ForgeroDataBundle(
		TaggedRegistry<Component> componentRegistry,
		TagResolver tagResolver,
		Map<OpenIdentifier, HostData> hostItemMap
) {
}
