package com.sigmundgranaas.forgero.core.tool;

import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartBinding;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartHandle;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartHead;

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
    public int getDurability() {
        return super.getDurability();
    }

    @Override
    public int getAttackDamage() {
        return super.getAttackDamage();
    }

    @Override
    public float getAttackSpeed() {
        return super.getAttackSpeed();
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return super.getMiningSpeedMultiplier();
    }

    @Override
    public void createToolDescription(ToolDescriptionWriter writer) {
        super.createToolDescription(writer);
        writer.addBinding(binding);
    }
}
