package com.sigmundgranaas.forgero.model.generation.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.model.loading.api.ModelTemplateProvider;

import java.util.Map;

/**
 * Service responsible for generating model definitions and texture generation tasks
 * based on a set of templates and a collection of fully realized Components.
 */
public interface ModelGenerator {
	/**
	 * Generates models and texture tasks.
	 *
	 * @param components       A map of all generated runtime Components, keyed by their ID.
	 * @param templateProvider Provider for loaded part and contextual model templates.
	 * @return A {@link ModelGenerationResult} containing the generated model DTOs and texture tasks.
	 */
	ModelGenerationResult generate(Map<OpenIdentifier, Component> components, ModelTemplateProvider templateProvider);
}
