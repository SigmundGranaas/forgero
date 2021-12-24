package com.sigmundgranaas.forgero.tool;

import com.sigmundgranaas.forgero.identifier.ForgeroIdentifierFactory;
import com.sigmundgranaas.forgero.identifier.ForgeroToolIdentifier;
import com.sigmundgranaas.forgero.item.forgerotool.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.registry.ItemRegister;
import com.sigmundgranaas.forgero.tool.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.tool.toolpart.ToolPartHandle;
import com.sigmundgranaas.forgero.tool.toolpart.ToolPartHead;
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
    public void registerToolItem(ItemRegister register) {
        register.registerTool(this);
    }

    @Override
    public @NotNull ForgeroToolPart getToolHead() {
        return head;
    }

    @Override
    public @NotNull PrimaryMaterial getPrimaryMaterial() {
        return head.getPrimaryMaterial();
    }

    @Override
    public @NotNull ForgeroToolPart getToolHandle() {
        return handle;
    }

    @Override
    public @NotNull ForgeroToolIdentifier getIdentifier() {
        return (ForgeroToolIdentifier) ForgeroIdentifierFactory.INSTANCE.createForgeroIdentifier(getToolIdentifierString());
    }

    @Override
    public @NotNull String getShortToolIdentifierString() {
        return head.getPrimaryMaterial().getName() + "_" + head.getToolTypeName();
    }

    @Override
    public @NotNull String getToolIdentifierString() {
        return head.getPrimaryMaterial().getName() + "_" + head.getToolTypeName() + "_" + head.getPrimaryMaterial().getName() + "_" + head.getToolPartName() + "_" + handle.getPrimaryMaterial().getName() +
                "_" + handle.getToolPartName();
    }

    @Override
    public @NotNull ForgeroToolTypes getToolType() {
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
    public ToolMaterial getMaterial() {
        return (ToolMaterial) getPrimaryMaterial();
    }
}
