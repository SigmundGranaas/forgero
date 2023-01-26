package com.sigmundgranaas.forgero.core.state.composite;

import com.sigmundgranaas.forgero.core.condition.Conditional;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.soul.Soul;
import com.sigmundgranaas.forgero.core.soul.SoulBindable;
import com.sigmundgranaas.forgero.core.state.IdentifiableContainer;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.SlotContainer;
import com.sigmundgranaas.forgero.core.type.Type;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ConstructedTool extends ConstructedComposite implements SoulBindable, Conditional<ConstructedTool> {
    private final State head;
    private final State handle;

    private final List<PropertyContainer> conditions;

    public ConstructedTool(State head, State handle, SlotContainer slots, IdentifiableContainer id) {
        super(slots, id, List.of(head, handle));
        this.conditions = Collections.emptyList();
        this.head = head;
        this.handle = handle;
    }

    public ConstructedTool(State head, State handle, SlotContainer slots, IdentifiableContainer id, List<PropertyContainer> conditions) {
        super(slots, id, List.of(head, handle));
        this.conditions = conditions;
        this.head = head;
        this.handle = handle;
    }

    @Override
    public ConstructedTool upgrade(State upgrade) {
        return toolBuilder().addUpgrade(upgrade).build();
    }

    @Override
    public ConstructedTool removeUpgrade(String id) {
        return this;
    }

    public State getHead() {
        return head;
    }

    public State getHandle() {
        return handle;
    }

    public ToolBuilder toolBuilder() {
        return ToolBuilder.builder(getHead(), getHandle())
                .addSlotContainer(slotContainer.copy())
                .conditions(conditions)
                .type(type())
                .id(identifier());
    }


    @Override
    public @NotNull List<Property> applyProperty(Target target) {
        return Stream.of(super.applyProperty(target), conditionProperties()).flatMap(List::stream).toList();
    }

    @Override
    public @NotNull List<Property> getRootProperties() {
        return Stream.of(super.getRootProperties(), conditionProperties()).flatMap(List::stream).toList();
    }

    @Override
    public ConstructedTool copy() {
        return toolBuilder().build();
    }

    @Override
    public State bind(Soul soul) {
        return toolBuilder().soul(soul).build();
    }

    @Override
    public List<PropertyContainer> conditions() {
        return conditions;
    }

    @Override
    public ConstructedTool applyCondition(PropertyContainer container) {
        return toolBuilder().condition(container).build();
    }

    @Override
    public ConstructedTool removeCondition(String identifier) {
        return toolBuilder().conditions(Conditional.removeConditions(conditions, identifier)).build();
    }

    @Getter
    public static class ToolBuilder extends BaseCompositeBuilder<ToolBuilder> {
        protected State head;
        protected State handle;
        protected List<PropertyContainer> conditions;

        public ToolBuilder(State head, State handle) {
            this.head = head;
            this.handle = handle;
            this.upgradeContainer = SlotContainer.of(Collections.emptyList());
            this.ingredientList = List.of(head, handle);
            this.conditions = new ArrayList<>();
        }

        public static ToolBuilder builder(State head, State handle) {
            return new ToolBuilder(head, handle);
        }

        public static Optional<ToolBuilder> builder(List<State> parts) {
            var head = parts.stream().filter(part -> part.test(Type.TOOL_PART_HEAD) || part.test(Type.SWORD_BLADE)).findFirst();
            var handle = parts.stream().filter(part -> part.test(Type.HANDLE)).findFirst();
            if (head.isPresent() && handle.isPresent()) {
                return Optional.of(builder(head.get(), handle.get()));
            }
            return Optional.empty();
        }

        public ToolBuilder head(State head) {
            this.head = head;
            return this;
        }

        public ToolBuilder handle(State handle) {
            this.handle = handle;
            return this;
        }

        public ToolBuilder condition(PropertyContainer condition) {
            this.conditions.add(condition);
            return this;
        }

        public ToolBuilder conditions(List<PropertyContainer> conditions) {
            this.conditions = conditions;
            return this;
        }

        public SoulBoundTool.SoulBoundToolBuilder soul(Soul soul) {
            return SoulBoundTool.SoulBoundToolBuilder.of(this, soul);
        }

        public ConstructedTool build() {
            compositeName();
            var id = new IdentifiableContainer(name, nameSpace, type);
            return new ConstructedTool(head, handle, upgradeContainer, id, conditions);
        }
    }
}
