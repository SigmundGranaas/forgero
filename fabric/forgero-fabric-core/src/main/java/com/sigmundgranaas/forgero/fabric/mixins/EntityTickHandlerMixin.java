package com.sigmundgranaas.forgero.fabric.mixins;

import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.ENTITY;
import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.WORLD;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.feature.tick.EntityTickFeatureExecutor;
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

	private MatchContext forgero$context;

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
	public void forgero$entityBaseTickHandler(CallbackInfo callbackInfo) {
		//noinspection ConstantValue
		if (this.getMainHandStack().getItem() instanceof PropertyContainer && ((Object) this instanceof LivingEntity entity)) {
			if(forgero$context == null){
				this.forgero$context = MatchContext.of(new MatchContext.KeyValuePair(ENTITY, entity), new MatchContext.KeyValuePair(WORLD, entity.getWorld()));
			}
			EntityTickFeatureExecutor.initFromMainHandStack(entity, forgero$context).execute(forgero$context);
		}
	}
}
