package com.sigmundgranaas.forgero.bow.item;

import java.util.List;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.StateProvider;
import com.sigmundgranaas.forgero.minecraft.common.customdata.CustomNameVisitor;
import com.sigmundgranaas.forgero.minecraft.common.item.ToolStateItem;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.StateWriter;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.Writer;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class DynamicBowItem extends BowItem implements ToolStateItem {
	private final StateProvider DEFAULT;

	public DynamicBowItem(Settings settings, StateProvider defaultState, StateService service) {
		super(settings);
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

	public int getRange() {
		return RANGE;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		return dynamicUse(world, user, hand);
	}


	@Override
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		dynamicOnStoppedUsing(stack, world, user, remainingUseTicks);
	}
}
