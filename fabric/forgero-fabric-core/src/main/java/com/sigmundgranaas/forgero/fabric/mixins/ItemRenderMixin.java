package com.sigmundgranaas.forgero.fabric.mixins;

import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.*;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.client.model.CompositeModelVariant;
import com.sigmundgranaas.forgero.minecraft.common.item.StateItem;
import com.sigmundgranaas.forgero.minecraft.common.match.ItemWorldEntityKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * This is a stupid mixin. PLEASE REMOVE WHEN POSSIBLE
 */

@Mixin(ItemRenderer.class)
public abstract class ItemRenderMixin {
	@Unique
	private final LoadingCache<ItemWorldEntityKey, MatchContext> contextCache = CacheBuilder.newBuilder()
			.maximumSize(1000)
			.expireAfterAccess(Duration.ofSeconds(1))
			.build(new CacheLoader<>() {
				@Override
				public @NotNull MatchContext load(@NotNull ItemWorldEntityKey key) {
					return new MatchContext()
							.put(ENTITY, key.entity())
							.put(WORLD, key.world())
							.put(STACK, key.stack());
				}
			});
	@Shadow
	@Final
	private ItemModels models;

	@Inject(at = @At("HEAD"), method = "getModel", cancellable = true)
	public void getModelMixin(ItemStack stack, @Nullable World world, @Nullable LivingEntity entity, int seed, CallbackInfoReturnable<BakedModel> ci) {
		if (stack.getItem() instanceof StateItem) {
			try {
				ItemWorldEntityKey key = new ItemWorldEntityKey(stack, world, entity);
				MatchContext context = contextCache.get(key);

				if (this.models.getModel(stack) instanceof CompositeModelVariant variant) {
					BakedModel model = variant.getModel(stack, context);
					ClientWorld clientWorld = world instanceof ClientWorld ? (ClientWorld) world : null;
					ci.setReturnValue(model.getOverrides().apply(model, stack, clientWorld, entity, seed));
				}
			} catch (ExecutionException ignored) {
			}
		}
	}
}
