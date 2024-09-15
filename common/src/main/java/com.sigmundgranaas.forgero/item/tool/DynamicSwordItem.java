package com.sigmundgranaas.forgero.item.tool;

import java.util.List;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.StateProvider;
import com.sigmundgranaas.forgero.customdata.CustomNameVisitor;
import com.sigmundgranaas.forgero.item.ToolStateItem;
import com.sigmundgranaas.forgero.tooltip.StateWriter;
import com.sigmundgranaas.forgero.tooltip.Writer;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.text.Text;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class DynamicSwordItem extends SwordItem implements ToolStateItem {
	private final StateProvider DEFAULT;

	public DynamicSwordItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings, StateProvider defaultState) {
		super(toolMaterial, attackDamage, attackSpeed, settings);
		this.DEFAULT = defaultState;
	}

	@Override
	public State defaultState() {
		return DEFAULT.get();
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
	public UseAction getUseAction(ItemStack stack) {
		return ToolStateItem.super.getUseAction(stack);
	}
}
