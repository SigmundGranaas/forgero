package com.sigmundgranaas.forgero.core.model;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.Identifiable;
import com.sigmundgranaas.forgero.core.state.Slot;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.core.util.match.NameMatch;

public record TemplatedModelEntry(String template) implements ModelMatcher {

	@Override
	public boolean match(Matchable state, MatchContext context) {
		return true;
	}

	@Override
	public Optional<ModelTemplate> get(Matchable state, ModelProvider provider, MatchContext context) {
		var templateModel = provider.find(Identifiable.of(template))
				.filter(matcher -> matcher.match(state, context))
				.flatMap(matcher -> matcher.get(state, provider, context));
		if (state instanceof Composite construct) {
			context.add(construct.type()).add(new NameMatch(construct.name()));
			var compositeModelTemplate = new CompositeModelTemplate();
			templateModel.ifPresent(compositeModelTemplate::add);
			templateModel.ifPresent(model -> addModelToContext(model, context));
			construct.slots().stream()
					.filter(Slot::filled)
					.map(slot ->
							CompositeModelEntry.findUpgradeModel(slot, construct, context, provider)
					).flatMap(Optional::stream)
					.forEach(compositeModelTemplate::add);
			return Optional.of(compositeModelTemplate);
		} else {
			return templateModel;
		}
	}

	private void addModelToContext(ModelTemplate template, MatchContext context) {
		if (template instanceof PaletteTemplateModel templateModel) {
			context.add((new ModelMatchEntry(templateModel.template())));
		}
	}
}
