package com.sigmundgranaas.forgero.minecraft.common.item.tool;

import java.util.List;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.StateProvider;
import com.sigmundgranaas.forgero.minecraft.common.customdata.CustomNameVisitor;
import com.sigmundgranaas.forgero.minecraft.common.item.ToolStateItem;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.StateWriter;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.Writer;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class DynamicAxeItem extends AxeItem implements ToolStateItem {
	private final StateProvider DEFAULT;

	public DynamicAxeItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings, StateProvider defaultState) {
		super(toolMaterial, attackDamage, attackSpeed, settings);
		this.DEFAULT = defaultState;
	}

	@Override
	public int getItemBarStep(ItemStack stack) {
		return ToolStateItem.super.getItemBarStep(stack);
	}

	public int getItemBarColor(ItemStack stack) {
		return getDurabilityColor(stack);
	}

	@Override
	public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
		StateWriter.of(dynamicState(itemStack)).write(tooltip, tooltipContext);
		super.appendTooltip(itemStack, world, tooltip, tooltipContext);
	}

	@Override
	public Text getName() {
		return Writer.nameToTranslatableText(this);
	}

	@Override
	public Text getName(ItemStack stack) {
		var state = dynamicState(stack);
		return CustomNameVisitor.of(state.customData())
				.map(replacer -> replacer.replace(state.name()))
				.map(Writer::nameToTranslatableText)
				.orElseGet(this::getName);
	}


	@Override
	public State defaultState() {
		return DEFAULT.get();
	}


}

