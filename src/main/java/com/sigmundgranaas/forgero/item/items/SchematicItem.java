package com.sigmundgranaas.forgero.item.items;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.ForgeroResourceType;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.schematic.HeadSchematic;
import com.sigmundgranaas.forgero.core.schematic.Schematic;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.item.ForgeroItem;
import com.sigmundgranaas.forgero.item.adapter.DescriptionWriter;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SchematicItem extends Item implements ForgeroItem<SchematicItem>, PropertyContainer {
    private final Schematic schematic;

    public SchematicItem(Settings settings, Schematic pattern) {
        super(settings);

        this.schematic = pattern;
    }

    @Override
    public Text getName() {
        MutableText text;
        if (!getSchematic().getResourceName().equals("default")) {
            text = Text.translatable(String.format("item.%s.%s", ForgeroInitializer.MOD_NAMESPACE, schematic.getResourceName())).append(" ");

        } else {
            text = Text.literal("");
        }
        if (schematic.getType() == ForgeroToolPartTypes.HEAD) {
            String headType = switch (((HeadSchematic) schematic).getToolType()) {
                case AXE -> "axehead";
                case PICKAXE -> "pickaxehead";
                case SHOVEL -> "shovelhead";
                case SWORD -> "sword";
                case HOE -> "hoe";
            };
            text.append(Text.translatable(String.format("item.%s.%s", ForgeroInitializer.MOD_NAMESPACE, headType))).append(" ");
        } else {
            text.append(Text.translatable(String.format("item.%s.%s", ForgeroInitializer.MOD_NAMESPACE, schematic.getType().getName()))).append(" ");
        }
        text.append(Text.translatable(String.format("item.%s.%s", ForgeroInitializer.MOD_NAMESPACE, "schematic")));
        return text;
    }

    @Override
    public Text getName(ItemStack stack) {
        return getName();
    }


    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        new DescriptionWriter(tooltip).writeSchematicDescription(getSchematic());
        new DescriptionWriter(tooltip).addToolPartProperties(Property.stream(schematic.getProperties(Target.createEmptyTarget())));
    }

    public Schematic getSchematic() {
        return schematic;
    }

    @Override
    public String getStringIdentifier() {
        return schematic.getStringIdentifier();
    }

    @Override
    public String getResourceName() {
        return schematic.getResourceName();
    }

    @Override
    public ForgeroResourceType getResourceType() {
        return ForgeroResourceType.SCHEMATIC;
    }

    @Override
    public SchematicItem getItem() {
        return this;
    }
}
