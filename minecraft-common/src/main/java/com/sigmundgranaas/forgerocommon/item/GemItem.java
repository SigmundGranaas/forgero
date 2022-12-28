package com.sigmundgranaas.forgerocommon.item;

import com.sigmundgranaas.forgerocommon.conversion.StateConverter;
import com.sigmundgranaas.forgerocommon.item.tooltip.StateWriter;
import com.sigmundgranaas.forgerocommon.item.tooltip.Writer;
import com.sigmundgranaas.forgero.property.PropertyContainer;
import com.sigmundgranaas.forgero.state.LeveledState;
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

public class GemItem extends Item implements StateItem, State {
    private final State DEFAULT;

    public GemItem(Settings settings, State defaultState) {
        super(settings);
        this.DEFAULT = defaultState;
    }

    @Override
    public State defaultState() {
        return DEFAULT;
    }

    @Override
    public Text getName() {
        return Writer.nameToTranslatableText(DEFAULT);
    }

    @Override
    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        StateWriter.of(state(itemStack)).write(tooltip, tooltipContext);
        super.appendTooltip(itemStack, world, tooltip, tooltipContext);
    }

    @Override
    public Text getName(ItemStack stack) {
        var state = StateConverter.of(stack).orElse(DEFAULT);
        var text = Text.empty();
        if (state instanceof LeveledState leveledState) {
            text.append(Text.literal(String.format("Level %s ", leveledState.level())));
        }
        text.append(Writer.nameToTranslatableText(DEFAULT));
        return text;
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
        return dynamicState(stack);
    }

    @Override
    public PropertyContainer defaultProperties() {
        return DEFAULT;
    }

    @Override
    public boolean isEquippable() {
        return false;
    }
}