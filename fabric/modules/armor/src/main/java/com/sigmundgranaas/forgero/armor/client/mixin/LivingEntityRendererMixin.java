package com.sigmundgranaas.forgero.armor.client.mixin;

import com.sigmundgranaas.forgero.armor.ForgeroClient;
import com.sigmundgranaas.forgero.armor.client.model.ForgeroArmorFeatureRenderer;
import com.sigmundgranaas.forgero.armor.client.model.ForgeroArmorModelManager;
import com.sigmundgranaas.forgero.armor.client.model.ForgeroArmorTextureManager;
import com.sigmundgranaas.forgero.model.registry.api.armor.ArmorModelRegistry;
import com.sigmundgranaas.forgero.model.registry.api.item.ItemModelRegistry;
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
	private void onInit(EntityRendererFactory.Context ctx, EntityModel model, float shadowRadius, CallbackInfo ci) {
		// No need to cast 'this' to LivingEntityRenderer anymore.
		// We can directly call the invoker on 'this'.

		ItemModelRegistry itemModelRegistry = ForgeroClient.modelRegistry;
		ArmorModelRegistry armorModelRegistry = ForgeroClient.armorModelRegistry;

		ArmorModelResolver armorModelResolver = new RecursiveArmorModelResolver(armorModelRegistry);
		ForgeroArmorTextureManager textureManager = new ForgeroArmorTextureManager(itemModelRegistry);
		ForgeroArmorModelManager modelManager = new ForgeroArmorModelManager(ctx.getModelLoader());

		@SuppressWarnings("unchecked")
		ForgeroArmorFeatureRenderer<T, M> forgeroArmorRenderer = new ForgeroArmorFeatureRenderer<>((LivingEntityRenderer<T, M>) (Object) this, ForgeroClient.itemToComponent, armorModelResolver, textureManager, modelManager);

		// Call the invoker instead of the original protected method
		this.callAddFeature(forgeroArmorRenderer);
	}
}
