package com.sigmundgranaas.forgero.core.tool;

import com.sigmundgranaas.forgero.core.data.v1.pojo.ToolPojo;
import com.sigmundgranaas.forgero.core.identifier.ForgeroIdentifierFactory;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroToolIdentifier;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.property.AttributeType;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.property.attribute.ToolTarget;
import com.sigmundgranaas.forgero.core.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHead;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;

public class ForgeroToolBase implements ForgeroTool {
    protected final ToolPartHead head;
    protected final ToolPartHandle handle;

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
    public @NotNull String getStringIdentifier() {
        return getToolIdentifierString();
    }

    @Override
    public ToolPojo toDataResource() {
        return new ToolPojo();
    }

    @Override
    public @NotNull
    String getShortToolIdentifierString() {
        return head.getPrimaryMaterial().getResourceName() + ELEMENT_SEPARATOR + getToolHead().getSchematic().getResourceName();
    }

    @Override
    public @NotNull
    String getToolIdentifierString() {
        return String.format("%s%s%s", head.getPrimaryMaterial().getResourceName(), ELEMENT_SEPARATOR, getToolHead().getSchematic().getResourceName());
    }

    @Override
    public @NotNull
    ForgeroToolTypes getToolType() {
        return head.getToolType();
    }

    @Override
    public int getDurability(Target target) {
        int durability = (int) getPropertyStream(target).applyAttribute(target, AttributeType.DURABILITY);
        return Math.max(durability, 1);
    }

    @Override
    public float getAttackDamage(Target target) {
        float damage = getPropertyStream(target).applyAttribute(target, AttributeType.ATTACK_DAMAGE);
        return damage > 1 ? damage : 1f;
    }

    @Override
    public float getAttackSpeed(Target target) {
        return getPropertyStream(target).applyAttribute(target, AttributeType.ATTACK_SPEED);
    }

    @Override
    public float getMiningSpeedMultiplier(Target target) {
        return getPropertyStream(target).applyAttribute(target, AttributeType.MINING_SPEED);
    }


    @Override
    public PrimaryMaterial getMaterial() {
        return getPrimaryMaterial();
    }

    @Override
    public void createToolDescription(ToolDescriptionWriter writer) {
        writer.addHead(head);
        writer.addHandle(handle);
        writer.addToolProperties(getPropertyStream());
    }

    @Override
    public void createWeaponDescription(ToolDescriptionWriter writer) {
        writer.addHead(head);
        writer.addHandle(handle);
        writer.addSwordProperties(getPropertyStream());
    }

    @Override
    public @NotNull List<Property> getProperties(Target target) {
        Target toolTarget = target.combineTarget(new ToolTarget(Set.of(getToolType().toString())));
        return Stream.of(head.getState().getProperties(toolTarget), handle.getState().getProperties(toolTarget)).flatMap(Collection::stream).collect(Collectors.toList());
    }
}
