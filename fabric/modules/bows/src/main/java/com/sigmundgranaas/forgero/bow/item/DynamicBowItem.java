package com.sigmundgranaas.forgero.bow.item;

import java.util.List;
import java.util.function.Predicate;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.StateProvider;
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
import net.minecraft.util.UseAction;
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
		return getName();
	}

	@Override
	public State defaultState() {
		return DEFAULT.get();
	}

	public Predicate<ItemStack> getProjectiles() {
		return BOW_PROJECTILES;
	}

	public int getRange() {
		return RANGE;
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return dynamicGetUseAction(stack);
	}

	@Override
	public int getMaxUseTime(ItemStack stack) {
		return dynamicGetMaxUseTime(stack);
	}

	@Override
	public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
		dynamicUsageTick(world, user, stack, remainingUseTicks);
	}

	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		return dynamicFinishUsing(stack, world, user);
	}

	@Override
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		dynamicOnStoppedUsing(stack, world, user, remainingUseTicks);
	}

	@Override
	public boolean isUsedOnRelease(ItemStack stack) {
		return dynamicIsUsedOnRelease(stack);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		return dynamicUse(world, user, hand);
	}
}
