package com.sigmundgranaas.forgero.item.adapter;

import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.tool.ToolDescriptionWriter;
import com.sigmundgranaas.forgero.core.toolpart.ToolPartDescriptionWriter;
import com.sigmundgranaas.forgero.core.toolpart.binding.ToolPartBinding;
import com.sigmundgranaas.forgero.core.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHead;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.List;

public class DescriptionWriter implements ToolDescriptionWriter, ToolPartDescriptionWriter {
    private List<Text> tooltip;

    public DescriptionWriter(List<Text> tooltip) {
        this.tooltip = tooltip;
    }


    @Override
    public void addSecondaryMaterial(SecondaryMaterial material) {
        tooltip.add(new LiteralText(String.format("Secondary material: %s", material.getName())));
    }

    @Override
    public void addGem() {

    }

    @Override
    public void addPrimaryMaterial(PrimaryMaterial material) {
        tooltip.add(new LiteralText(String.format("Primary material: %s", material.getName())));
    }

    @Override
    public void addMiningLevel(int miningLevel) {
        tooltip.add(new LiteralText(String.format("Mining level: %s", miningLevel)));
    }

    @Override
    public void addMiningMultiplier(float multiplier) {
        tooltip.add(new LiteralText(String.format("Mining multiplier: %s", multiplier)));
    }

    @Override
    public void addHead(ToolPartHead head) {
        tooltip.add(new LiteralText(String.format("Head: %s %s", head.getToolPartName(), head.getPrimaryMaterial().getName())));
        head.createToolPartDescription(this);
    }

    @Override
    public void addHandle(ToolPartHandle handle) {
        tooltip.add(new LiteralText(String.format("Handle: %s %s", handle.getToolPartName(), handle.getPrimaryMaterial().getName())));
        handle.createToolPartDescription(this);
    }

    @Override
    public void addBinding(ToolPartBinding binding) {
        tooltip.add(new LiteralText(String.format("Binding: %s %s", binding.getToolPartName(), binding.getPrimaryMaterial().getName())));
        binding.createToolPartDescription(this);
    }
}
