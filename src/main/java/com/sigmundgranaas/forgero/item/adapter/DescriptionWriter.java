package com.sigmundgranaas.forgero.item.adapter;

import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.gem.GemDescriptionWriter;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.property.AttributeType;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyStream;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.property.active.ActiveProperty;
import com.sigmundgranaas.forgero.core.property.active.PatternBreaking;
import com.sigmundgranaas.forgero.core.property.active.VeinBreaking;
import com.sigmundgranaas.forgero.core.schematic.Schematic;
import com.sigmundgranaas.forgero.core.tool.ToolDescriptionWriter;
import com.sigmundgranaas.forgero.core.toolpart.ToolPartDescriptionWriter;
import com.sigmundgranaas.forgero.core.toolpart.binding.ToolPartBinding;
import com.sigmundgranaas.forgero.core.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHead;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;

import java.util.List;
import java.util.stream.Collectors;

public record DescriptionWriter(
        List<Text> tooltip) implements ToolDescriptionWriter, ToolPartDescriptionWriter, GemDescriptionWriter {


    public static Rarity getRarityFromInt(int rarity) {
        if (rarity >= 100) {
            return Rarity.EPIC;
        } else if (rarity >= 80) {
            return Rarity.RARE;
        } else if (rarity >= 30) {
            return Rarity.UNCOMMON;
        }
        return Rarity.COMMON;
    }

    @Override
    public void addSecondaryMaterial(SecondaryMaterial material) {
        Rarity rarity = getRarityFromInt((int) Property.stream(material.getSecondaryProperties()).applyAttribute(Target.createEmptyTarget(), AttributeType.RARITY));
        MutableText mutableText = new LiteralText("  Secondary: ").formatted(Formatting.GRAY);
        mutableText.append(new LiteralText(String.format("%s", material.getName())).formatted(rarity.formatting));
        tooltip.add(mutableText);
    }

    @Override
    public void addGem(Gem gem) {
        Rarity rarity = getRarityFromGemLevel(gem.getLevel());
        MutableText mutableText = new LiteralText("  Gem: ").formatted(Formatting.GRAY);
        mutableText.append(new LiteralText(String.format("%s, level %s", gem.getName(), gem.getLevel())).formatted(rarity.formatting));
        tooltip.add(mutableText);
    }

    private Rarity getRarityFromGemLevel(int level) {
        int index = level / 2;
        if (level > 4) {
            index = 4;
        } else if (index <= 0) {
            index = 1;
        }
        return Rarity.values()[index - 1];
    }

    @Override
    public void addPrimaryMaterial(PrimaryMaterial material) {
        Rarity rarity = getRarityFromInt((int) Property.stream(material.getPrimaryProperties()).applyAttribute(Target.createEmptyTarget(), AttributeType.RARITY));
        MutableText mutableText = new LiteralText("  Primary: ").formatted(Formatting.GRAY);
        mutableText.append(new LiteralText(String.format("%s", material.getName())).formatted(rarity.formatting));
        tooltip.add(mutableText);
    }

    @Override
    public void addToolPartProperties(PropertyStream stream) {
        List<Property> properties = stream.collect(Collectors.toList());

        addActiveProperty(Property.stream(properties).getActiveProperties().toList());

        tooltip.add(new LiteralText("Attributes: "));
        addAllAttribute(properties);
    }

    private void addToolAttributes(List<Property> attributes) {
        addAttributeInt(attributes, AttributeType.DURABILITY, "Durability");
        addAttribute(attributes, AttributeType.MINING_SPEED, "Mining Speed");
        addAttribute(attributes, AttributeType.MINING_LEVEL, "Mining Level");
    }

    private void addAllAttribute(List<Property> attributes) {
        addAttribute(attributes, AttributeType.ATTACK_DAMAGE, "Attack Damage");
        addAttribute(attributes, AttributeType.ATTACK_SPEED, "Attack Speed");
        addAttributeInt(attributes, AttributeType.DURABILITY, "Durability");
        addAttribute(attributes, AttributeType.MINING_SPEED, "Mining Speed");
        addAttributeInt(attributes, AttributeType.MINING_LEVEL, "Mining Level");
    }

    private void addAttributeInt(List<Property> attributes, AttributeType type, String title) {
        int result = (int) Property.stream(attributes).applyAttribute(Target.createEmptyTarget(), type);
        if (result > 0) {
            MutableText miningLevel = new LiteralText(String.format("  %s : ", title)).formatted(Formatting.GRAY);
            miningLevel.append(new LiteralText(String.format("%s", result)).formatted(Formatting.WHITE));
            tooltip.add(miningLevel);
        }
    }

    private void addAttribute(List<Property> attributes, AttributeType type, String title) {
        float result = Property.stream(attributes).applyAttribute(Target.createEmptyTarget(), type);
        if (result != 0f) {
            MutableText miningLevel = new LiteralText(String.format("  %s : ", title)).formatted(Formatting.GRAY);
            miningLevel.append(new LiteralText(String.format("%s", result)).formatted(Formatting.WHITE));
            tooltip.add(miningLevel);
        }
    }

    @Override
    public void addHead(ToolPartHead head) {
        tooltip.add(new LiteralText("Head: "));
        head.createToolPartDescription(this);
    }

    @Override
    public void addHandle(ToolPartHandle handle) {
        tooltip.add(new LiteralText("Handle: "));
        handle.createToolPartDescription(this);
    }

    @Override
    public void addBinding(ToolPartBinding binding) {
        tooltip.add(new LiteralText("Binding: "));
        binding.createToolPartDescription(this);
    }

    @Override
    public void addToolProperties(PropertyStream stream) {
        List<Property> properties = stream.collect(Collectors.toList());

        addActiveProperty(Property.stream(properties).getActiveProperties().toList());
        tooltip.add(new LiteralText("Attributes: "));

        addToolAttributes(properties);
    }


    public void addSwordProperties(PropertyStream stream) {
        List<Property> properties = stream.collect(Collectors.toList());

        addActiveProperty(Property.stream(properties).getActiveProperties().toList());
        tooltip.add(new LiteralText("Attributes: "));

        addAttributeInt(properties.stream().toList(), AttributeType.DURABILITY, "Durability");
    }

    private void addActiveProperty(List<ActiveProperty> activeProperties) {
        if (activeProperties.size() > 0) {
            tooltip.add(new LiteralText("Properties: "));
        }
        activeProperties.forEach(this::createPropertyDescription);

    }

    private void createPropertyDescription(ActiveProperty property) {
        switch (property.getActiveType()) {
            case VEIN_MINING_PATTERN -> createVeinMiningDescription((VeinBreaking) property);
            case BLOCK_BREAKING_PATTERN -> createPatternMiningDescription((PatternBreaking) property);
        }
    }

    private void createVeinMiningDescription(VeinBreaking property) {
        MutableText mutableText = new LiteralText(" Vein mining").formatted(Formatting.GRAY);
        tooltip.add(mutableText);
        MutableText blocks = new LiteralText("  Blocks: ").formatted(Formatting.GRAY);
        blocks.append(new LiteralText(property.description()).formatted(Formatting.WHITE));
        tooltip.add(blocks);
        MutableText depth = new LiteralText("  Depth: ").formatted(Formatting.GRAY);
        depth.append(new LiteralText(String.valueOf(property.depth())).formatted(Formatting.WHITE));
        tooltip.add(depth);

    }

    private void createPatternMiningDescription(PatternBreaking property) {
        MutableText mutableText = new LiteralText(" Pattern Mining").formatted(Formatting.GRAY);
        tooltip.add(mutableText);

        for (String line : property.getPattern()) {
            MutableText patternText = new LiteralText(String.format("   %s", line)).formatted(Formatting.WHITE);
            tooltip.add(patternText);
        }
    }


    @Override
    public void createGemDescription(Gem gem) {
        Rarity rarity = getRarityFromGemLevel(gem.getLevel());
        MutableText mutableText = new LiteralText("Gem: ").formatted(Formatting.GRAY);
        mutableText.append(new LiteralText(String.format("%s, level%s", gem.getName(), gem.getLevel())).formatted(rarity.formatting));
        tooltip.add(mutableText);

        addActiveProperty(Property.stream(gem.getProperties()).getActiveProperties().toList());

        addAllAttribute(gem.getProperties());
    }

    public void writeSchematicDescription(Schematic pattern) {
        MutableText mutableText = new LiteralText("Material count: ").formatted(Formatting.GRAY);
        mutableText.append(new LiteralText(String.format("%s", pattern.getMaterialCount())));

        tooltip.add(mutableText);
    }
}
