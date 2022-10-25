package com.sigmundgranaas.forgero.item.items.tool;

import com.sigmundgranaas.forgero.conversion.StateConverter;
import com.sigmundgranaas.forgero.item.adapter.CompositeWriter;
import com.sigmundgranaas.forgero.item.items.DynamicAttributeItem;
import com.sigmundgranaas.forgero.property.PropertyContainer;
import com.sigmundgranaas.forgero.state.Composite;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.type.Type;
import com.sigmundgranaas.forgero.util.match.Context;
import com.sigmundgranaas.forgero.util.match.Matchable;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.List;

public class DynamicSwordItem extends SwordItem implements DynamicAttributeItem, State {
    private final State DEFAULT;

    public DynamicSwordItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings, State defaultState) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
        this.DEFAULT = defaultState;
    }

    @Override
    public Text getName() {
        return Text.literal(DEFAULT.name());
    }

    @Override
    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        State state = StateConverter.of(itemStack).orElse(DEFAULT);
        if (state instanceof Composite composite) {
            CompositeWriter.write(composite, tooltip, tooltipContext, 0);
        }
        super.appendTooltip(itemStack, world, tooltip, tooltipContext);
    }

    @Override
    public Text getName(ItemStack stack) {
        return getName();
    }

    @Override
    public String name() {
        return DEFAULT.name();
    }

    @Override
    public String nameSpace() {
        return DEFAULT.nameSpace();
    }

    @Override
    public Type type() {
        return DEFAULT.type();
    }

    @Override
    public boolean test(Matchable match, Context context) {
        return DEFAULT.test(match, context);
    }

    @Override
    public PropertyContainer dynamicProperties(ItemStack stack) {
        return DEFAULT;
    }

    @Override
    public PropertyContainer defaultProperties() {
        return DEFAULT;
    }
}

