package com.sigmundgranaas.forgero.core.state.composite;

import com.sigmundgranaas.forgero.core.soul.Soul;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.IdentifiableContainer;
import com.sigmundgranaas.forgero.core.state.MaterialBased;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.SlotContainer;
import com.sigmundgranaas.forgero.core.type.Type;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ConstructedTool extends ConstructedComposite implements MaterialBased {
    private final State head;

    private final State handle;
    private final State baseMaterial;

    public ConstructedTool(State head, State handle, State baseMaterial, SlotContainer slots, IdentifiableContainer id) {
        super(slots, id, List.of(head, handle));
        this.head = head;
        this.baseMaterial = baseMaterial;
        this.handle = handle;
    }

    @Override
    public State baseMaterial() {
        return baseMaterial;
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
        return ToolBuilder.builder(getHead(), getHandle(), baseMaterial())
                .addUpgrades(slots())
                .type(type())
                .id(identifier());
    }

    @Override
    public ConstructedTool copy() {
        return toolBuilder().build();
    }

    @Getter
    public static class ToolBuilder extends BaseCompositeBuilder<ToolBuilder> {
        protected State head;
        protected State handle;
        protected State primaryMaterial;

        public ToolBuilder(State head, State handle, State material) {
            this.handle = handle;
            this.head = head;
            this.primaryMaterial = material;
            this.upgradeContainer = SlotContainer.of(Collections.emptyList());
            this.ingredientList = List.of(head, handle);
        }

        public static ToolBuilder builder(State head, State handle, State material) {
            return new ToolBuilder(head, handle, material);
        }

        public static Optional<ToolBuilder> builder(List<State> parts) {
            var head = parts.stream().filter(part -> part.test(Type.TOOL_PART_HEAD) || part.test(Type.SWORD_BLADE)).findFirst();
            var handle = parts.stream().filter(part -> part.test(Type.HANDLE)).findFirst();
            if (head.isPresent() && handle.isPresent()) {
                if (head.get() instanceof MaterialBased based) {
                    return Optional.of(builder(head.get(), handle.get(), based.baseMaterial()));
                } else if (head.get() instanceof Composite composite) {
                    var material = composite.components().stream()
                            .filter(comp -> comp.test(Type.MATERIAL))
                            .findFirst();
                    return material.map(mat -> builder(head.get(), handle.get(), mat));
                }
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

        public ToolBuilder material(State primaryMaterial) {
            this.primaryMaterial = primaryMaterial;
            return this;
        }

        public SoulBoundTool.SoulBoundToolBuilder soul(Soul soul) {
            return SoulBoundTool.SoulBoundToolBuilder.of(this, soul);
        }

        public ConstructedTool build() {
            var id = new IdentifiableContainer(name, nameSpace, type);
            return new ConstructedTool(head, handle, primaryMaterial, upgradeContainer, id);
        }


    }
}
