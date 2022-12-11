package com.sigmundgranaas.forgero.model;

import com.sigmundgranaas.forgero.state.Composite;
import com.sigmundgranaas.forgero.state.Identifiable;
import com.sigmundgranaas.forgero.state.Slot;
import com.sigmundgranaas.forgero.util.match.Context;
import com.sigmundgranaas.forgero.util.match.Matchable;
import com.sigmundgranaas.forgero.util.match.NameMatch;

import java.util.Optional;

import static com.sigmundgranaas.forgero.model.CompositeModelEntry.findUpgradeModel;

public record TemplatedModelEntry(String template) implements ModelMatcher {

    @Override
    public boolean match(Matchable state, Context context) {
        return true;
    }

    @Override
    public Optional<ModelTemplate> get(Matchable state, ModelProvider provider, Context context) {
        var templateModel = provider.find(Identifiable.of(template))
                .filter(matcher -> matcher.match(state, context))
                .flatMap(matcher -> matcher.get(state, provider, context));
        if (state instanceof Composite composite) {
            context.add(composite.type()).add(new NameMatch(composite.name()));
            var compositeModelTemplate = new CompositeModelTemplate();
            templateModel.ifPresent(compositeModelTemplate::add);
            templateModel.ifPresent(model -> addModelToContext(model, context));
            composite.slots().stream()
                    .filter(Slot::filled)
                    .map(slot ->
                            findUpgradeModel(slot, composite, context, provider)
                    ).flatMap(Optional::stream)
                    .forEach(compositeModelTemplate::add);
            return Optional.of(compositeModelTemplate);
        } else {
            return templateModel;
        }
    }

    private void addModelToContext(ModelTemplate template, Context context) {
        if (template instanceof PaletteTemplateModel templateModel) {
            context.add((new ModelMatchEntry(templateModel.template())));
        }
    }
}
