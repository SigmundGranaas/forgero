package com.sigmundgranaas.forgero.core.model;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.Slot;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.composite.Constructed;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.core.util.match.NameMatch;
import org.jetbrains.annotations.NotNull;

public class CompositeModelEntry implements ModelMatcher {
	public static Optional<ModelTemplate> findUpgradeModel(Slot upgradeSlot, Composite construct, MatchContext context, ModelProvider provider) {
		if (upgradeSlot.filled()) {
			if (upgradeSlot.get().get() instanceof Composite upgradeConstruct) {
				return provider.find(upgradeConstruct).filter(matcher -> matcher.match(upgradeConstruct, context)).flatMap(matcher -> matcher.get(upgradeConstruct, provider, context));
			} else if (upgradeSlot.get().get().type().test(Type.PART)) {
				return provider.find(upgradeSlot.get().get()).filter(matcher -> matcher.match(upgradeSlot.get().get(), context)).flatMap(matcher -> matcher.get(upgradeSlot.get().get(), provider, context));
			} else {
				return provider.find(construct).filter(matcher -> matcher.match(upgradeSlot, context)).flatMap(matcher -> matcher.get(upgradeSlot, provider, context));
			}
		}
		return Optional.empty();
	}

	@Override
	public boolean match(Matchable state, MatchContext context) {
		return state instanceof Composite;
	}

	@Override
	public Optional<ModelTemplate> get(Matchable state, ModelProvider provider, MatchContext context) {
		if (state instanceof Composite composite) {
			var compositeModelTemplate = new CompositeModelTemplate();
			context.add(composite.type())
					.add(new NameMatch(composite.name()));
			if (composite instanceof Constructed construct) {
				construct.parts().stream()
						.map(stateEntry -> convert(stateEntry, provider, context))
						.flatMap(Optional::stream)
						.forEach(compositeModelTemplate::add);

				var models = compositeModelTemplate.getModels().stream()
						.filter(PaletteTemplateModel.class::isInstance)
						.map(PaletteTemplateModel.class::cast)
						.toList();

				for (PaletteTemplateModel model : models) {
					context.add(new ModelMatchEntry(model.template()));
				}
			}

			composite.slots().stream()
					.map(slot -> findUpgradeModel(slot, composite, context, provider))
					.flatMap(Optional::stream)
					.forEach(compositeModelTemplate::add);

			return Optional.of(compositeModelTemplate);
		}
		return Optional.empty();
	}

	private Optional<ModelTemplate> convert(State state, ModelProvider provider, MatchContext context) {
		var modelMatcher = provider.find(state);
		return modelMatcher.filter(matcher -> matcher.match(state, context)).flatMap(matcher -> matcher.get(state, provider, context));
	}

	@Override
	public int compareTo(@NotNull ModelMatcher o) {
		return 0;
	}
}
