package com.sigmundgranaas.forgero.core.tool;

import com.sigmundgranaas.forgero.core.identifier.ForgeroIdentifierFactory;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroToolIdentifier;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.properties.AttributeType;
import com.sigmundgranaas.forgero.core.properties.Property;
import com.sigmundgranaas.forgero.core.properties.attribute.EmptyTarget;
import com.sigmundgranaas.forgero.core.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHead;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        return head.getPrimaryMaterial().getName() + "_" + head.getToolType().getToolName();
    }

    @Override
    public @NotNull
    String getToolIdentifierString() {
        return String.format("%s_%s", head.getPrimaryMaterial().getName(), getToolType().toString().toLowerCase(Locale.ROOT));
    }

    @Override
    public @NotNull
    ForgeroToolTypes getToolType() {
        return head.getToolType();
    }

    @Override
    public int getDurability() {
        return (int) getPropertyStream().applyAttribute(EmptyTarget.createEmptyTarget(), AttributeType.DURABILITY);
    }

    @Override
    public float getAttackDamage() {
        return getPropertyStream().applyAttribute(EmptyTarget.createEmptyTarget(), AttributeType.ATTACK_DAMAGE);
    }

    @Override
    public float getAttackSpeed() {
        return getPropertyStream().applyAttribute(EmptyTarget.createEmptyTarget(), AttributeType.ATTACK_SPEED);
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return getPropertyStream().applyAttribute(EmptyTarget.createEmptyTarget(), AttributeType.MINING_SPEED);
    }

    @Override
    public int getMiningLevel() {
        return (int) getPropertyStream().applyAttribute(EmptyTarget.createEmptyTarget(), AttributeType.MINING_LEVEL);
    }

    @Override
    public PrimaryMaterial getMaterial() {
        return getPrimaryMaterial();
    }

    @Override
    public void createToolDescription(ToolDescriptionWriter writer) {
        writer.addHead(head);
        writer.addHandle(handle);
    }

    @Override
    public double getAttackDamageAddition() {
        return head.getAttackDamageAddition();
    }

    @Override
    public List<Property> getProperties() {
        return Stream.of(head.getState().getProperties(), handle.getState().getProperties()).flatMap(Collection::stream).collect(Collectors.toList());
    }
}
