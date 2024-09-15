package com.sigmundgranaas.forgero.core.property.v2.feature;

import java.util.Optional;
import java.util.function.BiFunction;

import com.google.gson.JsonElement;

public interface FeatureBuilder<T extends Feature> {
	static <T extends Feature> AbstractFeatureBuilder<T> of(String type, BiFunction<BasePredicateData, JsonElement, T> baseBuilder) {
		return new AbstractFeatureBuilder<T>(type) {
			@Override
			protected T buildFromBase(BasePredicateData data, JsonElement element) {
				return baseBuilder.apply(data, element);
			}
		};
	}

	Optional<T> build(JsonElement element);

	abstract class AbstractFeatureBuilder<T extends Feature> implements FeatureBuilder<T> {
		private final String typeValue;

		protected AbstractFeatureBuilder(String typeValue) {
			this.typeValue = typeValue;
		}


		@Override
		public Optional<T> build(JsonElement element) {
			return BasePredicateData.of(element)
					.filter(base -> base.type().equals(typeValue))
					.map(base -> this.buildFromBase(base, element));
		}

		protected abstract T buildFromBase(BasePredicateData data, JsonElement element);
	}
}
