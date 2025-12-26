package com.sigmundgranaas.forgero.data.pipeline.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.common.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.data.loading.api.data.host.HostData;

import java.util.Map;

/**
 * A container for all the data produced by the Forgero data pipeline.
 * This object is the final result of the initialization process and provides
 * access to the component registry, tag resolver, and host item mapping data.
 *
 * @param componentRegistry The registry of all Forgero components.
 * @param tagResolver       The resolver for tag hierarchies.
 * @param hostItemMap       A map from Forgero component IDs to their corresponding host item mapping data.
 */
public record ForgeroDataBundle(
		TaggedRegistry<Component> componentRegistry,
		TagResolver tagResolver,
		Map<OpenIdentifier, HostData> hostItemMap
) {
}
