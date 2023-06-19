package com.sigmundgranaas.forgero.core.state.upgrade.slot;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.property.v2.UpgradePropertyProcessor;
import com.sigmundgranaas.forgero.core.state.Slot;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import org.jetbrains.annotations.NotNull;

public class FilledSlot extends AbstractTypedSlot {
	private final State upgrade;

	public FilledSlot(int index, Type type, State upgrade, String description, Set<Category> categories) {
		super(index, type, description, categories);
		this.upgrade = upgrade;
	}

	@Override
	public @NotNull
	List<Property> getRootProperties() {
		return filterProperties(Target.EMPTY);
	}

	@Override
	public @NotNull
	List<Property> applyProperty(Target target) {
		return filterProperties(target);
	}

	private List<Property> filterProperties(Target target) {
		var properties = upgrade.applyProperty(target);
		return new UpgradePropertyProcessor(categories).process(properties);
	}

	@Override
	public boolean filled() {
		return true;
	}

	@Override
	public Optional<State> get() {
		return Optional.of(upgrade);
	}

	@Override
	public Slot empty() {
		return new EmptySlot(index(), type(), description(), categories);
	}

	@Override
	public Optional<Slot> fill(State slottable, Set<Category> categories) {
		return Optional.empty();
	}

	@Override
	public boolean test(Matchable match, MatchContext context) {
		if (type().test(match, context)) {
			return true;
		} else {
			return upgrade.test(match, context);
		}
	}


	@Override
	public Slot copy() {
		return new FilledSlot(index, type, upgrade, description, categories);
	}
}
