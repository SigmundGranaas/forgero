package com.sigmundgranaas.forgero.item;

import com.sigmundgranaas.forgero.conversion.StateConverter;
import com.sigmundgranaas.forgero.item.adapter.AttributeWriter;
import com.sigmundgranaas.forgero.item.adapter.CompositeWriter;
import com.sigmundgranaas.forgero.state.Composite;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.type.Type;
import com.sigmundgranaas.forgero.util.match.Context;
import com.sigmundgranaas.forgero.util.match.Matchable;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.List;

public class DefaultStateItem extends Item implements StateItem, State {
    private final State DEFAULT;

    public DefaultStateItem(Settings settings, State defaultState) {
        super(settings);
        this.DEFAULT = defaultState;
    }

    @Override
    public State defaultState() {
        return DEFAULT;
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
        AttributeWriter.write(state, tooltip, tooltipContext);
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
}
