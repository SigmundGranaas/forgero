package com.sigmundgranaas.forgero.render.mixin;

import com.sigmundgranaas.forgero.render.ForgeroClient;
import com.sigmundgranaas.forgero.render.model.armor.ForgeroArmorFeatureRenderer;
import com.sigmundgranaas.forgero.model.resolution.api.armor.ArmorModelResolver;
import com.sigmundgranaas.forgero.model.resolution.impl.RecursiveArmorModelResolver;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ExecutionException;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends BipedEntityModel<T>> {
	/**
	 * Creates an invoker for the protected 'addFeature' method. This allows us to call it
	 * from our mixin as if it were public.
	 *
	 * @param feature The feature renderer to add.
	 * @return The result of the original addFeature call.
	 */
	@Invoker("addFeature")
	protected abstract boolean callAddFeature(FeatureRenderer<T, M> feature);

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(EntityRendererFactory.Context ctx, EntityModel model, float shadowRadius, CallbackInfo ci) throws ExecutionException, InterruptedException {
		// Armor services are now fetched from the central ForgeroClient container.
		// These services are replaced during a resource reload, ensuring their caches are cleared.
		ArmorModelResolver armorModelResolver = new RecursiveArmorModelResolver(ForgeroClient.services.armorModelRegistry());

		@SuppressWarnings("unchecked")
		ForgeroArmorFeatureRenderer<T, M> forgeroArmorRenderer = new ForgeroArmorFeatureRenderer<>(
				(LivingEntityRenderer<T, M>) (Object) this,
				ForgeroClient.services.itemToComponent(),
				armorModelResolver,
				ForgeroClient.services.armorTextureManager(),
				ForgeroClient.services.armorModelManager()
		);

		this.callAddFeature(forgeroArmorRenderer);
	}
}
