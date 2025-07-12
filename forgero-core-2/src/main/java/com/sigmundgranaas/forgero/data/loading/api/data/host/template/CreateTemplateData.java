package com.sigmundgranaas.forgero.data.loading.api.data.host.template;

import org.jetbrains.annotations.Nullable;

/**
 * Templated DTO for creating a new host platform item.
 * All string fields can contain placeholders like {material.name}.
 *
 * @param id          The ID template for the new item.
 * @param className   The class name identifier for the new item.
 * @param item_group  An optional item group/creative tab ID template.
 */
public record CreateTemplateData(
		String id,
		String className,
		@Nullable String item_group
) {
}
