package com.sigmundgranaas.forgero.validation.impl;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.validation.api.ContentPackValidator;

import java.nio.file.Path;
import java.util.*;

/**
 * Builder implementation for ContentPackValidator.
 */
public class ContentPackValidatorBuilder implements ContentPackValidator.Builder {
	private List<Path> contentPaths = new ArrayList<>();
	private String defaultNamespace = "forgero";
	private boolean validateModels = true;
	private boolean validateTextures = true;
	private boolean strictTemplateValidation = true;
	private Map<PropertyKey<?>, Codec<? extends List<?>>> propertyCodecs = new HashMap<>();
	private Map<String, Codec<? extends StaticCondition>> staticConditionCodecs = new HashMap<>();
	private Map<String, Codec<? extends DynamicCondition>> dynamicConditionCodecs = new HashMap<>();

	@Override
	public ContentPackValidator.Builder contentPaths(List<Path> paths) {
		this.contentPaths = new ArrayList<>(Objects.requireNonNull(paths, "paths cannot be null"));
		return this;
	}

	@Override
	public ContentPackValidator.Builder defaultNamespace(String namespace) {
		this.defaultNamespace = Objects.requireNonNull(namespace, "namespace cannot be null");
		return this;
	}

	@Override
	public ContentPackValidator.Builder validateModels(boolean validate) {
		this.validateModels = validate;
		return this;
	}

	@Override
	public ContentPackValidator.Builder validateTextures(boolean validate) {
		this.validateTextures = validate;
		return this;
	}

	@Override
	public ContentPackValidator.Builder strictTemplateValidation(boolean strict) {
		this.strictTemplateValidation = strict;
		return this;
	}

	/**
	 * Sets property codecs for parsing properties in definitions.
	 *
	 * @param codecs Map of property key to codec
	 * @return this builder
	 */
	public ContentPackValidatorBuilder propertyCodecs(Map<PropertyKey<?>, Codec<? extends List<?>>> codecs) {
		this.propertyCodecs = new HashMap<>(Objects.requireNonNull(codecs, "codecs cannot be null"));
		return this;
	}

	/**
	 * Sets static condition codecs.
	 *
	 * @param codecs Map of condition type to codec
	 * @return this builder
	 */
	public ContentPackValidatorBuilder staticConditionCodecs(Map<String, Codec<? extends StaticCondition>> codecs) {
		this.staticConditionCodecs = new HashMap<>(Objects.requireNonNull(codecs, "codecs cannot be null"));
		return this;
	}

	/**
	 * Sets dynamic condition codecs.
	 *
	 * @param codecs Map of condition type to codec
	 * @return this builder
	 */
	public ContentPackValidatorBuilder dynamicConditionCodecs(Map<String, Codec<? extends DynamicCondition>> codecs) {
		this.dynamicConditionCodecs = new HashMap<>(Objects.requireNonNull(codecs, "codecs cannot be null"));
		return this;
	}

	@Override
	public ContentPackValidator build() {
		if (contentPaths.isEmpty()) {
			throw new IllegalStateException("At least one content path must be specified");
		}

		return new ContentPackValidatorImpl(
				contentPaths,
				defaultNamespace,
				validateModels,
				validateTextures,
				strictTemplateValidation,
				propertyCodecs,
				staticConditionCodecs,
				dynamicConditionCodecs
		);
	}
}
