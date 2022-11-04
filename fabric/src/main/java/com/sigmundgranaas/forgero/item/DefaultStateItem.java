package com.sigmundgranaas.forgero.item;

import com.sigmundgranaas.forgero.item.tooltip.StateWriter;
import com.sigmundgranaas.forgero.item.tooltip.Writer;
import com.sigmundgranaas.forgero.property.PropertyContainer;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.type.Type;
import com.sigmundgranaas.forgero.util.match.Context;
import com.sigmundgranaas.forgero.util.match.Matchable;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
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
        MutableText text = Text.literal("");
        for (String element:
                DEFAULT.name().split("-")) {
            text.append(Text.translatable(Writer.toTranslationKey(element)));
            text.append(Text.literal(" "));
        }
        return text;
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
}
