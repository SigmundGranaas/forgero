package com.sigmundgranaas.forgero.item.items;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.attribute.Target;
import com.sigmundgranaas.forgero.core.schematic.Schematic;
import com.sigmundgranaas.forgero.item.adapter.DescriptionWriter;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SchematicItem extends Item {
    private final Schematic schematic;

    public SchematicItem(Settings settings, Schematic pattern) {
        super(settings);

        this.schematic = pattern;
    }


    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        new DescriptionWriter(tooltip).writeSchematicDescription(getSchematic());
        new DescriptionWriter(tooltip).addToolPartProperties(Property.stream(schematic.getProperties(Target.createEmptyTarget())));
    }

    public Schematic getSchematic() {
        return schematic;
    }
}
