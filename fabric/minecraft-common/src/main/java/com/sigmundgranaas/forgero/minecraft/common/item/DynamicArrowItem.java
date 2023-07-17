package com.sigmundgranaas.forgero.minecraft.common.item;

import java.util.List;

import com.sigmundgranaas.forgero.core.customdata.DataContainer;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.StateProvider;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.StateWriter;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.Writer;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class DynamicArrowItem extends ArrowItem implements StateItem, State {
	private final StateProvider DEFAULT;

	public DynamicArrowItem(Settings settings, StateProvider defaultState) {
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
		StateWriter.of(dynamicState(itemStack)).write(tooltip, tooltipContext);
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
	public boolean test(Matchable match, MatchContext context) {
		return defaultState().test(match, context);
	}


	@Override
	public DataContainer customData() {
		return defaultState().customData();
	}
}
