package com.sigmundgranaas.forgero.tool;

import com.sigmundgranaas.forgero.identifier.ForgeroIdentifierFactory;
import com.sigmundgranaas.forgero.identifier.ForgeroToolIdentifier;
import com.sigmundgranaas.forgero.item.forgerotool.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.registry.ItemRegister;
import com.sigmundgranaas.forgero.tool.toolpart.ForgeroToolPart;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class DefaultForgeroTool extends AbstractForgeroTool {
    protected DefaultForgeroTool(ForgeroToolPart head, ForgeroToolPart handle) {
        super(head, handle);
    }

    @Override
    public void registerToolItem(ItemRegister register) {
        super.registerToolItem(register);
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
}
