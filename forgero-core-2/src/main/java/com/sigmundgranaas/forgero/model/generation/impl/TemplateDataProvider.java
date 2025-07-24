package com.sigmundgranaas.forgero.model.generation.impl;

import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.TargetDTO;

import java.util.List;

/**
 * A generic interface for any template DTO that provides a list of models for a specific target.
 *
 * @param <T> The type of the model DTO this provider holds (e.g., TemplateModelDTO or TemplateArmorModelDTO).
 */
public interface TemplateDataProvider<T> {
	/**
	 * @return The target criteria for which components this template applies to.
	 */
	TargetDTO target();

	/**
	 * @return A list of model templates to be processed for the target.
	 */
	List<T> models();
}
