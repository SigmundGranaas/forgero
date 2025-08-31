package com.sigmundgranaas.forgero.data.loading.api.data.host.template;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Templated DTO for a host item mapping.
 *
 * @param identifiers An optional list of identifier templates.
 * @param create      An optional template for creating a new item.
 */
public record HostTemplateData(
		@Nullable List<IdentifierTemplateEntry> identifiers,
		@Nullable CreateTemplateData create
) {
}
