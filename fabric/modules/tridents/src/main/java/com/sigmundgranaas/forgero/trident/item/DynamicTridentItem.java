package com.sigmundgranaas.forgero.trident.item;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.StateProvider;
import com.sigmundgranaas.forgero.minecraft.common.customdata.CustomNameVisitor;
import com.sigmundgranaas.forgero.minecraft.common.item.ToolStateItem;

import com.sigmundgranaas.forgero.minecraft.common.tooltip.StateWriter;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.Writer;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class DynamicTridentItem extends TridentItem implements ToolStateItem {
	private final StateProvider DEFAULT;


	public DynamicTridentItem(Settings settings, StateProvider aDefault) {
		super(settings);
		DEFAULT = aDefault;
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
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack itemStack = user.getStackInHand(hand);
		if (itemStack.getDamage() >= itemStack.getMaxDamage() - 1) {
			return TypedActionResult.fail(itemStack);
		} else if (EnchantmentHelper.getRiptide(itemStack) > 0 && !user.isTouchingWaterOrRain()) {
			return TypedActionResult.fail(itemStack);
		} else {
			user.setCurrentHand(hand);
			return ToolStateItem.super.dynamicUse(world, user, hand);
		}
	}

	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		if (user instanceof PlayerEntity playerEntity) {
			int i = this.getMaxUseTime(stack) - remainingUseTicks;
			if (i >= 10) {
				int j = EnchantmentHelper.getRiptide(stack);
				if (j <= 0 || playerEntity.isTouchingWaterOrRain()) {
					if (!world.isClient) {
						stack.damage(1, playerEntity, (p) -> {
							p.sendToolBreakStatus(user.getActiveHand());
						});
						if (j == 0) {
							ToolStateItem.super.dynamicOnStoppedUsing(stack, world, user, remainingUseTicks);
						}
					}

					playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
					if (j > 0) {
						float f = playerEntity.getYaw();
						float g = playerEntity.getPitch();
						float h = -MathHelper.sin(f * 0.017453292F) * MathHelper.cos(g * 0.017453292F);
						float k = -MathHelper.sin(g * 0.017453292F);
						float l = MathHelper.cos(f * 0.017453292F) * MathHelper.cos(g * 0.017453292F);
						float m = MathHelper.sqrt(h * h + k * k + l * l);
						float n = 3.0F * ((1.0F + (float)j) / 4.0F);
						h *= n / m;
						k *= n / m;
						l *= n / m;
						playerEntity.addVelocity(h, k, l);
						playerEntity.useRiptide(20);
						if (playerEntity.isOnGround()) {
							float o = 1.1999999F;
							playerEntity.move(MovementType.SELF, new Vec3d(0.0, 1.1999999284744263, 0.0));
						}

						SoundEvent soundEvent;
						if (j >= 3) {
							soundEvent = SoundEvents.ITEM_TRIDENT_RIPTIDE_3;
						} else if (j == 2) {
							soundEvent = SoundEvents.ITEM_TRIDENT_RIPTIDE_2;
						} else {
							soundEvent = SoundEvents.ITEM_TRIDENT_RIPTIDE_1;
						}

						world.playSoundFromEntity(null, playerEntity, soundEvent, SoundCategory.PLAYERS, 1.0F, 1.0F);
					}
				}
			}
		}
	}
}
