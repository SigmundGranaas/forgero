package com.sigmundgranaas.forgero.core.tool;

import com.sigmundgranaas.forgero.core.identifier.ForgeroIdentifierFactory;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroToolIdentifier;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartHandle;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartHead;
import net.minecraft.item.ToolMaterial;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class ForgeroToolBase implements ForgeroTool {
    private final ToolPartHead head;
    private final ToolPartHandle handle;

    public ForgeroToolBase(ToolPartHead head, ToolPartHandle handle) {
        this.handle = handle;
        this.head = head;
    }

    @Override
    public @NotNull
    ToolPartHead getToolHead() {
        return head;
    }

    @Override
    public @NotNull
    PrimaryMaterial getPrimaryMaterial() {
        return head.getPrimaryMaterial();
    }

    @Override
    public @NotNull
    ToolPartHandle getToolHandle() {
        return handle;
    }

    @Override
    public @NotNull
    ForgeroToolIdentifier getIdentifier() {
        return (ForgeroToolIdentifier) ForgeroIdentifierFactory.INSTANCE.createForgeroIdentifier(getToolIdentifierString());
    }

    @Override
    public @NotNull
    String getShortToolIdentifierString() {
        return head.getPrimaryMaterial().getName() + "_" + head.getToolTypeName();
    }

    @Override
    public @NotNull
    String getToolIdentifierString() {
        return String.format("%s_%s", head.getPrimaryMaterial().getName(), getToolType().toString().toLowerCase(Locale.ROOT));
    }

    @Override
    public @NotNull
    ForgeroToolTypes getToolType() {
        return ForgeroToolTypes.valueOf(head.getToolTypeName().toUpperCase(Locale.ROOT));
    }

    @Override
    public int getDurability() {
        return head.getDurability() + handle.getDurability();
    }

    @Override
    public int getAttackDamage() {
        return head.getSharpness() / 10;
    }

    @Override
    public float getAttackSpeed() {
        return 100f / ((float) head.getWeight() + (float) handle.getWeight());
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return 1f;
    }

    @Override
    public int getMiningLevel() {
        return head.getMiningLevel();
    }

    @Override
    public ToolMaterial getMaterial() {
        return getPrimaryMaterial();
    }

    @Override
    public void createToolDescription(ToolDescriptionWriter writer) {
        writer.addHead(head);
        writer.addHandle(handle);
    }
}
