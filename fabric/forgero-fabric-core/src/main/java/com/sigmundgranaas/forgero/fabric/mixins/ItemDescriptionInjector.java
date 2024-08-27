package com.sigmundgranaas.forgero.fabric.mixins;

import static com.sigmundgranaas.forgero.minecraft.common.tooltip.Writer.writeModifierSection;

import java.util.List;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.SimpleState;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.item.StateItem;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.DefaultWriter;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;

import net.minecraft.util.UseAction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemDescriptionInjector {

	@Inject(at = @At("TAIL"), method = "use", cancellable = true)
	public void forgero$dynamicUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
		Item item = (Item) (Object) this;
		ItemStack tool = user.getStackInHand(Hand.MAIN_HAND);

		String id = Registries.ITEM.getId(item).toString();
		Optional<State> state = StateService.INSTANCE.find(id);
		Optional<State> toolState = StateService.INSTANCE.convert(tool);

		if (state.isPresent() && hand == Hand.OFF_HAND && toolState.isPresent() && toolState.get() instanceof Composite upgradeable) {
			user.setCurrentHand(hand);
			cir.setReturnValue(TypedActionResult.consume(user.getStackInHand(hand)));
		}
	}

	@Inject(at = @At("HEAD"), method = "finishUsing")
	public void forgero$dynamicUseUpgrade(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
		Item item = (Item) (Object) this;
		ItemStack tool = user.getStackInHand(Hand.MAIN_HAND);

		String id = Registries.ITEM.getId(item).toString();
		Optional<State> state = StateService.INSTANCE.find(id);
		Optional<State> toolState = StateService.INSTANCE.convert(tool);

		if (state.isPresent() && stack.equals(user.getStackInHand(Hand.OFF_HAND)) && toolState.isPresent() && toolState.get() instanceof Composite upgradeable) {
				Composite composite = upgradeable.upgrade(state.get());
				user.setStackInHand(Hand.MAIN_HAND, StateService.INSTANCE.convert(composite).get());
				user.getStackInHand(Hand.OFF_HAND).decrement(1);
			}
	}

	@Inject(at = @At("HEAD"), method = "getMaxUseTime", cancellable = true)
	public void forgero$dynamicUseUpgrade(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
		Item item = (Item) (Object) this;

		String id = Registries.ITEM.getId(item).toString();
		Optional<State> state = StateService.INSTANCE.find(id);

		if (state.isPresent() && !(state.get() instanceof Composite)) {
			cir.setReturnValue(32);
		}
	}

	@Inject(at = @At("HEAD"), method = "getUseAction", cancellable = true)
	public void forgero$dynamicUseAction(ItemStack stack, CallbackInfoReturnable<UseAction> cir) {
		Item item = stack.getItem();

		String id = Registries.ITEM.getId(item).toString();
		Optional<State> state = StateService.INSTANCE.find(id);

		if (state.isPresent() && !(state.get() instanceof Composite)) {
			cir.setReturnValue(UseAction.EAT);
		}
	}

	@SuppressWarnings("DataFlowIssue")
	@Inject(at = @At("HEAD"), method = "appendTooltip")
	public void forgero$InjectForgeroAttributes(ItemStack stack, World world, List<Text> tooltip, TooltipContext context, CallbackInfo ci) {
		Item item = (Item) (Object) this;
		String id = Registries.ITEM.getId(item).toString();
		if (!(item instanceof StateItem) && StateService.INSTANCE.find(id).isPresent()) {
			if (Screen.hasShiftDown()) {
				StateService.INSTANCE.find(id)
						.ifPresent(state -> new DefaultWriter(state).write(tooltip, context));
			} else {
				tooltip.add(writeModifierSection("shift", "to_show_attributes")
				);
			}
		}
	}
}
