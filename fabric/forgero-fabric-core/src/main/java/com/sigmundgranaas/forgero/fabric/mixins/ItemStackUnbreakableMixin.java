package com.sigmundgranaas.forgero.fabric.mixins;

import static com.sigmundgranaas.forgero.core.condition.Conditions.BROKEN_TYPE_KEY;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.condition.Conditions;
import com.sigmundgranaas.forgero.core.property.v2.Attribute;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Durability;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainsFeatureCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.PropertyTargetCacheKey;
import com.sigmundgranaas.forgero.core.state.State;
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
			StateService service = StateService.INSTANCE;
			var stack = player.getMainHandStack();
			Optional<State> tool = service.convert(stack);
			if (tool.isPresent() && tool.map(UnbreakableHandler::isUnbreakable).filter(bol -> bol).isPresent()) {
				if (tool.get() instanceof ConstructedTool conditional && !ContainsFeatureCache.check(PropertyTargetCacheKey.of(conditional, BROKEN_TYPE_KEY))) {
					stack.setDamage(Attribute.of(conditional, Durability.KEY).asInt());
					player.getWorld().playSound(player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ITEM_BREAK, player.getSoundCategory(), 0.8f, 0.8f + player.getWorld().random.nextFloat() * 0.4f, false);
					State brokenTool = conditional.applyCondition(Conditions.BROKEN);
					player.getInventory().setStack(player.getInventory().selectedSlot, service.update(brokenTool, stack));
				}
				cir.setReturnValue(false);
			}
		}
	}
}


