package com.sigmundgranaas.forgero.recipegen.api.template;

import com.google.gson.JsonObject;

import net.minecraft.util.Identifier;

/**
 * Represents a template source with its identifier and content.
 */
public interface TemplateSource {

	/**
	 * Gets the identifier of this template source.
	 *
	 * @return The template identifier
	 */
	Identifier id();

	/**
	 * Gets the template content as a JSON object.
	 *
	 * @return The template JSON
	 */
	JsonObject template();

	/**
	 * Creates a new TemplateSource instance.
	 *
	 * @param id       The template identifier
	 * @param template The template JSON
	 * @return A new TemplateSource instance
	 */
	static TemplateSource of(Identifier id, JsonObject template) {
		return new TemplateSourceImpl(id, template);
	}
}

record TemplateSourceImpl(Identifier id, JsonObject template) implements TemplateSource {
}
