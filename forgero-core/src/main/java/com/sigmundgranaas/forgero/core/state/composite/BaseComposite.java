package com.sigmundgranaas.forgero.core.state.composite;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.IdentifiableContainer;
import com.sigmundgranaas.forgero.core.state.Slot;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.SlotContainer;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.core.util.match.NameMatch;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public abstract class BaseComposite implements Composite {
	protected final SlotContainer slotContainer;

	protected final IdentifiableContainer id;

	protected BaseComposite(SlotContainer slotContainer, IdentifiableContainer id) {
		this.slotContainer = slotContainer;
		this.id = id;
	}


	@Override
	public @NotNull
	List<Property> applyProperty(Matchable target, MatchContext context) {
		var newContext = context.add(id.type());
		return compositeProperties(target, context);
	}

	@Override
	public @NotNull
	List<Property> getRootProperties() {
		return compositeProperties(Matchable.DEFAULT_TRUE, MatchContext.of());
	}

	@Override
	public List<Property> compositeProperties(Matchable target, MatchContext context) {
		List<Property> upgradeProps = slots().stream()
				.map(state -> state.getRootProperties())
				.flatMap(List::stream)
				.collect(Collectors.toList());

		upgradeProps.addAll(slots().stream().map(slot -> slot.stream().features().toList()).flatMap(List::stream).toList());

		return upgradeProps;
	}


	@Override
	public Optional<State> has(String id) {
		return components().stream()
				.map(comp -> recursiveComponentHas(comp, id))
				.flatMap(Optional::stream)
				.findFirst();
	}

	private Optional<State> recursiveComponentHas(State target, String id) {
		if (target.identifier().contains(id)) {
			return Optional.of(target);
		} else if (target instanceof Composite comp) {
			if (comp.has(id).isPresent()) {
				return comp.has(id);
			}
		}
		return Optional.empty();
	}

	@Override
	public String name() {
		return id.name();
	}

	@Override
	public String identifier() {
		return id.identifier();
	}

	@Override
	public String nameSpace() {
		return id.nameSpace();
	}

	@Override
	public Type type() {
		return id.type();
	}

	@Override
	public boolean test(Matchable match, MatchContext context) {
		if (match instanceof Type typeMatch) {
			if (this.type().test(typeMatch, context)) {
				return true;
			}

		}
		if (match instanceof NameMatch name) {
			if (name.test(this, context)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ImmutableList<State> upgrades() {
		return slotContainer.entries();
	}

	public boolean canUpgrade(State state) {
		return slotContainer.canUpgrade(state);
	}

	@Override
	public List<Slot> slots() {
		return slotContainer.slots();
	}

	@Override
	public SlotContainer getSlotContainer() {
		return this.slotContainer;
	}

	@Getter
	public static abstract class BaseCompositeBuilder<B extends BaseCompositeBuilder<B>> {
		protected List<State> ingredientList;
		protected SlotContainer upgradeContainer;
		protected NameCompositor compositor = new NameCompositor();
		protected Type type = Type.UNDEFINED;
		protected String name;
		protected String nameSpace = Forgero.NAMESPACE;

		public BaseCompositeBuilder() {
			this.ingredientList = new ArrayList<>();
			this.upgradeContainer = SlotContainer.of(Collections.emptyList());
		}

		public B addIngredient(State ingredient) {
			ingredientList.add(ingredient);
			return (B) this;
		}

		public B addIngredients(List<State> ingredients) {
			ingredientList.addAll(ingredients);
			return (B) this;
		}

		public B addUpgrade(State upgrade) {
			upgradeContainer.set(upgrade);
			return (B) this;
		}

		public B addUpgrade(Slot upgrade) {
			upgradeContainer.set(upgrade);
			return (B) this;
		}

		public B addSlotContainer(SlotContainer container) {
			this.upgradeContainer = container;
			return (B) this;
		}

		public B addUpgrades(List<? extends Slot> upgrades) {
			upgrades.forEach(upgradeContainer::set);
			return (B) this;
		}

		public B addUpgrades(ImmutableList<State> upgrades) {
			upgrades.forEach(upgradeContainer::set);
			return (B) this;
		}

		public B type(Type type) {
			this.type = type;
			return (B) this;
		}

		public B name(String name) {
			this.name = name;
			return (B) this;
		}

		public B nameSpace(String nameSpace) {
			this.nameSpace = nameSpace;
			return (B) this;
		}

		public B id(String id) {
			var elements = id.split(":");
			if (elements.length == 2) {
				this.nameSpace = elements[0];
				this.name = elements[1];
			}
			return (B) this;
		}

		protected void compositeName() {
			if (this.name == null && !ingredientList.isEmpty()) {
				this.name = compositor.compositeName(ingredientList);
			}
		}

		public abstract State build();
	}
}
