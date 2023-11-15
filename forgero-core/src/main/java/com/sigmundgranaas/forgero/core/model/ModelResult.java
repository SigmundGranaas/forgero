package com.sigmundgranaas.forgero.core.model;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.sigmundgranaas.forgero.core.util.match.ContextKey;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import lombok.Getter;

public class ModelResult {
	public static final ContextKey<ModelResult> MODEL_RESULT = new ContextKey<>("MODEL_RESULT", ModelResult.class);
	private final List<Matchable> invalidationOptions;

	@Getter
	@Nullable
	private ModelTemplate template = new CompositeModelTemplate();

	public ModelResult() {
		this.invalidationOptions = new ArrayList<>();
	}

	public boolean isValid(Matchable matchable, MatchContext context) {
		if (invalidationOptions.isEmpty()) {
			return true;
		}
		return invalidationOptions.stream().noneMatch(option -> option.test(matchable, context));
	}

	public ModelResult addOptions(Matchable options) {
		invalidationOptions.add(options);
		return this;
	}

	public ModelResult setTemplate(ModelTemplate template) {
		this.template = template;
		return this;
	}
}
