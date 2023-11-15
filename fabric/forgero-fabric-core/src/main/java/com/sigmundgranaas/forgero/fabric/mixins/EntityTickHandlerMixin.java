package com.sigmundgranaas.forgero.fabric.mixins;

import static com.sigmundgranaas.forgero.minecraft.common.feature.FeatureUtils.streamFeature;
import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.ENTITY;
import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.WORLD;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.feature.EntityTickFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin(LivingEntity.class)
public abstract class EntityTickHandlerMixin extends Entity {


	public EntityTickHandlerMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@Shadow
	public abstract ItemStack getMainHandStack();

	/**
	 * Injects the Entity tick handler at the end of the baseTick function to run any potential entity tick features
	 *
	 * @param callbackInfo CallbackInfo
	 */
	@Inject(method = "baseTick", at = @At("RETURN"))
	public void forgero$entityBaseTick$EntityTickHandler(CallbackInfo callbackInfo) {
		if (this.getMainHandStack().getItem() instanceof PropertyContainer) {

			LivingEntity entity = (LivingEntity) (Object) this;
			ItemStack main = entity.getMainHandStack();
			MatchContext context = MatchContext.of()
					.put(ENTITY, entity)
					.put(WORLD, entity.world);
			streamFeature(main, context, EntityTickFeature.KEY)
					.forEach(handler -> handler.handle(entity));
		}
	}
}
