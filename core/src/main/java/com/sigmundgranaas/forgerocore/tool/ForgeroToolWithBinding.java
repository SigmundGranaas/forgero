package com.sigmundgranaas.forgerocore.tool;

import com.sigmundgranaas.forgerocore.property.Property;
import com.sigmundgranaas.forgerocore.property.Target;
import com.sigmundgranaas.forgerocore.toolpart.binding.ToolPartBinding;
import com.sigmundgranaas.forgerocore.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgerocore.toolpart.head.ToolPartHead;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ForgeroToolWithBinding extends ForgeroToolBase {
    private final ToolPartBinding binding;

    public ForgeroToolWithBinding(ToolPartHead head, ToolPartHandle handle, ToolPartBinding binding) {
        super(head, handle);
        this.binding = binding;
    }

    public ToolPartBinding getBinding() {
        return binding;
    }

    @Override
    public int getDurability(Target target) {
        return super.getDurability(target);
    }

    @Override
    public float getAttackDamage(Target target) {
        return super.getAttackDamage(target);
    }

    @Override
    public float getAttackSpeed(Target target) {
        return super.getAttackSpeed(target);
    }

    @Override
    public float getMiningSpeedMultiplier(Target target) {
        return super.getMiningSpeedMultiplier(target);
    }

    @Override
    public void createToolDescription(ToolDescriptionWriter writer) {
        writer.addHead(head);
        writer.addHandle(handle);
        writer.addBinding(binding);
        writer.addToolProperties(getPropertyStream());
    }

    @Override
    public @NotNull List<Property> getProperties(Target target) {
        return Stream.of(super.getProperties(target), binding.getState().getProperties(target))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

    }
}
