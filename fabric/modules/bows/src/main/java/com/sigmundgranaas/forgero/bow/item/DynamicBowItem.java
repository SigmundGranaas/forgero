package com.sigmundgranaas.forgero.bow.item;

import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.FORGERO_IDENTIFIER;
import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.UPGRADES_IDENTIFIER;

import java.util.List;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.StateProvider;
import com.sigmundgranaas.forgero.minecraft.common.customdata.CustomNameVisitor;
import com.sigmundgranaas.forgero.minecraft.common.item.ToolStateItem;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.StateWriter;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.Writer;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
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

	/**
	 * Idiotic function for removing arrows from the bow when it is not being used.
	 * For some reason there is no event when stopping to use an item.
	 * <p>
	 * Remove as soon as possible. I mean please get this away from me. But is works for now.
	 *
	 * @param stack
	 * @param world
	 * @param entity   the entity holding the item; usually a player
	 * @param slot
	 * @param selected whether the item is in the selected hotbar slot
	 */
	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, world, entity, slot, selected);
		if (selected) {
			return;
		}
		if (world.isClient()) {
			return;
		}
		if (!stack.hasNbt()) {
			return;
		}

		if (stack.getOrCreateNbt().contains(FORGERO_IDENTIFIER)) {
			NbtCompound nbt = stack.getOrCreateNbt().getCompound(FORGERO_IDENTIFIER);
			if (nbt.contains(UPGRADES_IDENTIFIER) && !nbt.getList(UPGRADES_IDENTIFIER, NbtElement.COMPOUND_TYPE).isEmpty()) {
				NbtList upgrades = nbt.getList(UPGRADES_IDENTIFIER, NbtElement.COMPOUND_TYPE);
				var element = upgrades.get(0);
				if (element.getType() == NbtElement.COMPOUND_TYPE) {
					NbtCompound upgrade = (NbtCompound) element;
					if (upgrade.contains("codecType") && upgrade.getString("codecType").equals("forgero:filled_slot")) {
						upgrade.putString("codecType", "forgero:empty_slot");
					}
				}
			}
		}
	}
}
