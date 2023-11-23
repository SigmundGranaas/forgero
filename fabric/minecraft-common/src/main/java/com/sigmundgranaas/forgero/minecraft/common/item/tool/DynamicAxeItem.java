package com.sigmundgranaas.forgero.minecraft.common.item.tool;

import java.util.List;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.StateProvider;
import com.sigmundgranaas.forgero.minecraft.common.customdata.CustomNameVisitor;
import com.sigmundgranaas.forgero.minecraft.common.item.ToolStateItem;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.StateWriter;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.Writer;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ToolMaterial;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
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


	@Override
	public UseAction getUseAction(ItemStack stack) {
		return ToolStateItem.super.getUseAction(stack);
	}

	@Override
	public int getMaxUseTime(ItemStack stack) {
		return ToolStateItem.super.getMaxUseTime(stack);
	}

	@Override
	public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
		ToolStateItem.super.usageTick(world, user, stack, remainingUseTicks);
	}

	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		return ToolStateItem.super.finishUsing(stack, world, user);
	}

	@Override
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		ToolStateItem.super.onStoppedUsing(stack, world, user, remainingUseTicks);
	}

	@Override
	public boolean isUsedOnRelease(ItemStack stack) {
		return ToolStateItem.super.isUsedOnRelease(stack);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		return ToolStateItem.super.useOnBlock(context);

	}

	@Override
	public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
		return ToolStateItem.super.useOnEntity(stack, user, entity, hand);

	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		return ToolStateItem.super.use(world, user, hand);
	}
}

