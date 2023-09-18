package com.sigmundgranaas.forgero.core.model;

import java.util.Optional;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.state.Identifiable;
import com.sigmundgranaas.forgero.core.texture.utils.Offset;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PaletteTemplateModel(String palette,
								   String template,
								   int order,
								   @Nullable Offset offset,
								   @Nullable Integer resolution,
								   @Nullable JsonObject displayOverrides) implements ModelTemplate, ModelMatcher, Identifiable {

	@Override
	public Optional<Offset> getOffset() {
		return Optional.ofNullable(offset);
	}

	@Override
	public Integer getResolution() {
		return Optional.ofNullable(resolution).orElse(16);
	}

	@Override
	public Optional<JsonObject> getDisplayOverrides() {
		return Optional.ofNullable(displayOverrides);
	}

	@Override
	public <T> T convert(Converter<T, ModelTemplate> converter) {
		return converter.convert(this);
	}

	@Override
	public boolean match(Matchable state, MatchContext context) {
		return true;
	}

	@Override
	public Optional<ModelTemplate> get(Matchable state, ModelProvider provider, MatchContext context) {
		return Optional.of(this);
	}

	@Override
	public String name() {
		return String.format("%s-%s", palette, template);
	}

	@Override
	public String nameSpace() {
		return String.format("%s", Forgero.NAMESPACE);
	}


	@Override
	public String toString() {
		return name();
	}

	@Override
	public int compareTo(@NotNull ModelMatcher o) {
		if (o instanceof ModelTemplate templateO) {
			return order() - templateO.order();
		}
		return 0;
	}
}
