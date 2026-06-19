package com.sigmundgranaas.forgero.smithing.mixins;

import com.sigmundgranaas.forgero.smithing.item.custom.SmithingTongsItem;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {
	@Unique
	private static final float QUENCH_SWING_ENDPOINT = 0.25F;
	@Unique
	private static final float QUENCH_ANIMATION_STEP = 0.2F;

	@Shadow
	@Final
	private MinecraftClient client;

	@Shadow
	protected abstract void applySwingOffset(MatrixStack matrices, Arm arm, float swingProgress);

	@Unique
	private float forgero$mainHandQuenchProgress;
	@Unique
	private float forgero$previousMainHandQuenchProgress;
	@Unique
	private float forgero$offHandQuenchProgress;
	@Unique
	private float forgero$previousOffHandQuenchProgress;
	@Unique
	private float forgero$renderTickDelta;
	@Unique
	private Hand forgero$renderedHand;
	@Unique
	private ItemStack forgero$renderedStack = ItemStack.EMPTY;

	@Redirect(
			method = "updateHeldItems",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/item/ItemStack;areEqual(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z",
					ordinal = 0
			)
	)
	private boolean forgero$areMainHandStacksEqual(ItemStack left, ItemStack right) {
		return forgero$areEqualOrTemperatureOnly(left, right);
	}

	@Redirect(
			method = "updateHeldItems",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/item/ItemStack;areEqual(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z",
					ordinal = 1
			)
	)
	private boolean forgero$areOffHandStacksEqual(ItemStack left, ItemStack right) {
		return forgero$areEqualOrTemperatureOnly(left, right);
	}

	@Inject(method = "updateHeldItems", at = @At("HEAD"))
	private void forgero$updateQuenchAnimation(CallbackInfo ci) {
		forgero$previousMainHandQuenchProgress = forgero$mainHandQuenchProgress;
		forgero$previousOffHandQuenchProgress = forgero$offHandQuenchProgress;

		boolean mainHandQuenching = forgero$isQuenching(Hand.MAIN_HAND);
		boolean offHandQuenching = forgero$isQuenching(Hand.OFF_HAND);

		forgero$mainHandQuenchProgress = forgero$approachQuenchProgress(
				forgero$mainHandQuenchProgress,
				mainHandQuenching
		);
		forgero$offHandQuenchProgress = forgero$approachQuenchProgress(
				forgero$offHandQuenchProgress,
				offHandQuenching
		);
	}

	@Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
	private void forgero$captureRenderedHand(
			AbstractClientPlayerEntity player,
			float tickDelta,
			float pitch,
			Hand hand,
			float swingProgress,
			ItemStack stack,
			float equipProgress,
			MatrixStack matrices,
			VertexConsumerProvider vertexConsumers,
			int light,
			CallbackInfo ci
	) {
		forgero$renderedHand = hand;
		forgero$renderedStack = stack;
		forgero$renderTickDelta = tickDelta;
	}

	@Inject(
			method = "renderFirstPersonItem",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/render/item/HeldItemRenderer;applyEquipOffset(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;F)V",
					shift = At.Shift.AFTER
			)
	)
	private void forgero$applyQuenchSwingWhileUsing(
			AbstractClientPlayerEntity player,
			float tickDelta,
			float pitch,
			Hand hand,
			float swingProgress,
			ItemStack stack,
			float equipProgress,
			MatrixStack matrices,
			VertexConsumerProvider vertexConsumers,
			int light,
			CallbackInfo ci
	) {
		float quenchProgress = forgero$getRenderedQuenchProgress();

		if (!(stack.getItem() instanceof SmithingTongsItem) || quenchProgress <= 0.0F) {
			return;
		}

		Arm arm = player.getMainArm();

		if (hand == Hand.OFF_HAND) {
			arm = arm == Arm.RIGHT ? Arm.LEFT : Arm.RIGHT;
		}

		applySwingOffset(matrices, arm, QUENCH_SWING_ENDPOINT * quenchProgress);
	}

	@ModifyArg(
			method = "renderFirstPersonItem",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/render/item/HeldItemRenderer;applySwingOffset(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;F)V"
			),
			index = 2
	)
	private float forgero$holdQuenchSwing(float swingProgress) {
		if (!(forgero$renderedStack.getItem() instanceof SmithingTongsItem)) {
			return swingProgress;
		}

		return forgero$getRenderedQuenchProgress() > 0.0F ? 0.0F : swingProgress;
	}

	@Unique
	private float forgero$getRenderedQuenchProgress() {
		float previousProgress = forgero$renderedHand == Hand.OFF_HAND
				? forgero$previousOffHandQuenchProgress
				: forgero$previousMainHandQuenchProgress;
		float currentProgress = forgero$renderedHand == Hand.OFF_HAND
				? forgero$offHandQuenchProgress
				: forgero$mainHandQuenchProgress;
		float quenchProgress = previousProgress
				+ (currentProgress - previousProgress) * forgero$renderTickDelta;
		return quenchProgress;
	}

	@Unique
	private boolean forgero$areEqualOrTemperatureOnly(ItemStack left, ItemStack right) {
		return TemperatureUtils.areEqualIgnoringTemperature(left, right);
	}

	@Unique
	private boolean forgero$isQuenching(Hand hand) {
		return client.player != null
				&& client.player.isUsingItem()
				&& client.player.getActiveHand() == hand
				&& client.player.getActiveItem().getItem() instanceof SmithingTongsItem;
	}

	@Unique
	private float forgero$approachQuenchProgress(float current, boolean quenching) {
		float target = quenching ? 1.0F : 0.0F;

		if (current < target) {
			return Math.min(target, current + QUENCH_ANIMATION_STEP);
		}

		return Math.max(target, current - QUENCH_ANIMATION_STEP);
	}
}
