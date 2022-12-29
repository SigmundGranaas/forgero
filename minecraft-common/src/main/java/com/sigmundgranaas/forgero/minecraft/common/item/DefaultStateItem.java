package com.sigmundgranaas.forgero.minecraft.common.item;

import com.sigmundgranaas.forgero.minecraft.common.item.tooltip.StateWriter;
import com.sigmundgranaas.forgero.minecraft.common.item.tooltip.Writer;
import com.sigmundgranaas.forgero.property.PropertyContainer;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.state.StateProvider;
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
    private final StateProvider DEFAULT;

    public DefaultStateItem(Settings settings, StateProvider defaultState) {
        super(settings);
        this.DEFAULT = defaultState;
    }

    @Override
    public State defaultState() {
        return DEFAULT.get();
    }

    @Override
    public Text getName() {
        return Writer.nameToTranslatableText(defaultState());
    }

    @Override
    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        StateWriter.of(state(itemStack)).write(tooltip, tooltipContext);
        super.appendTooltip(itemStack, world, tooltip, tooltipContext);
    }

    @Override
    public Text getName(ItemStack stack) {
        return getName();
    }

    @Override
    public String name() {
        return defaultState().name();
    }

    @Override
    public String nameSpace() {
        return defaultState().nameSpace();
    }

    @Override
    public Type type() {
        return defaultState().type();
    }

    @Override
    public boolean test(Matchable match, Context context) {
        return defaultState().test(match, context);
    }

    @Override
    public PropertyContainer dynamicProperties(ItemStack stack) {
        return dynamicState(stack);
    }

    @Override
    public PropertyContainer defaultProperties() {
        return defaultState();
    }

    @Override
    public boolean isEquippable() {
        return false;
    }
}
