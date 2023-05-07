package com.sigmundgranaas.forgero.fabric.mixins;

import static com.sigmundgranaas.forgero.core.condition.Conditions.BROKEN_TYPE_KEY;

import com.sigmundgranaas.forgero.core.condition.Conditions;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Durability;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainsFeatureCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.PropertyTargetCacheKey;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedTool;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.UnbreakableHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.random.Random;

@Mixin(ItemStack.class)
public abstract class ItemStackUnbreakableMixin {

	@Shadow
	public abstract Item getItem();

	@Inject(at = @At(value = "RETURN"), method = "damage(ILnet/minecraft/util/math/random/Random;Lnet/minecraft/server/network/ServerPlayerEntity;)Z", cancellable = true)
	public <T extends LivingEntity> void checkIfToolIsUnbreakable(int amount, Random random, @Nullable ServerPlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValue() && player != null) {
			if (StateService.INSTANCE.convert(player.getMainHandStack()).map(UnbreakableHandler::isUnbreakable).filter(bol -> bol).isPresent()) {
				var stack = player.getMainHandStack();
				var tool = StateService.INSTANCE.convert(stack);
				if (tool.isPresent() && tool.get() instanceof ConstructedTool conditional && !ContainsFeatureCache.check(PropertyTargetCacheKey.of(conditional, BROKEN_TYPE_KEY))) {
					stack.setDamage(Durability.apply(conditional));
					player.world.playSound(player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ITEM_BREAK, player.getSoundCategory(), 0.8f, 0.8f + player.world.random.nextFloat() * 0.4f, false);
					var newStack = StateService.INSTANCE.convert(conditional.applyCondition(Conditions.BROKEN)).orElse(player.getMainHandStack());
					newStack.setDamage(stack.getDamage());
					player.getInventory().setStack(player.getInventory().selectedSlot, newStack);
				}
				cir.setReturnValue(false);
			}
		}
	}
}


