package com.sigmundgranaas.forgero.data.loading.api.data.host;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * DTO representing a fully resolved mapping between a Forgero component and a host platform item.
 *
 * @param identifiers An optional list of identifiers to search for an existing item.
 * @param create      An optional specification for creating a new item if none is found.
 */
public record HostData(
		@Nullable List<IdentifierEntry> identifiers,
		@Nullable CreateData create
) {
}
