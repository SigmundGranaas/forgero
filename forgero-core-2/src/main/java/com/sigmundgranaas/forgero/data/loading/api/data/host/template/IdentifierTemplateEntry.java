package com.sigmundgranaas.forgero.data.loading.api.data.host.template;

/**
 * Templated DTO for an identifier entry.
 *
 * @param type The type of identifier ("item" or "tag").
 * @param id   The ID template, which can contain placeholders.
 */
public record IdentifierTemplateEntry(String type, String id) {
}
