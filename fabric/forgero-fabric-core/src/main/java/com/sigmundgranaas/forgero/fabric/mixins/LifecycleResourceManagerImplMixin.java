package com.sigmundgranaas.forgero.fabric.mixins;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.model.ModelTemplate;
import com.sigmundgranaas.forgero.core.model.PaletteTemplateModel;
import com.sigmundgranaas.forgero.core.texture.V2.FileLoader;
import com.sigmundgranaas.forgero.core.texture.V2.TextureGenerator;
import com.sigmundgranaas.forgero.fabric.client.ForgeroClient;
import com.sigmundgranaas.forgero.fabric.resources.ResourceLoadedFileService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.Resource;
import net.minecraft.resource.metadata.ResourceMetadata;
import net.minecraft.util.Identifier;

@Mixin(LifecycledResourceManagerImpl.class)
public abstract class LifecycleResourceManagerImplMixin {

	@Inject(method = "getResource", at = @At("RETURN"), cancellable = true)
	public void getResource(Identifier id, CallbackInfoReturnable<Optional<Resource>> cir) {
		if (id.getPath().contains(".png") && cir.getReturnValue().isEmpty()) {
			var textureId = id.getPath().replace("textures/item/", id.getNamespace() + ":");
			if (ForgeroClient.TEXTURES.containsKey(textureId)) {
				ModelTemplate template = ForgeroClient.TEXTURES.get(textureId);
				if(template instanceof PaletteTemplateModel paletteTemplateModel){
					FileLoader loader = new ResourceLoadedFileService();
					var texture = TextureGenerator.getInstance(loader, ForgeroClient.PALETTE_REMAP).getTexture(paletteTemplateModel);
					if (texture.isPresent()) {
						var metadata = TextureGenerator.getInstance(loader, ForgeroClient.PALETTE_REMAP).getMetadata(paletteTemplateModel, ".mcmeta");
						Resource resource;
						resource = metadata
								.map(object -> new Resource(id.getNamespace(), texture.get()::getStream, convert(object)))
								.orElseGet(() -> new Resource(id.getNamespace(), texture.get()::getStream));
						cir.setReturnValue(Optional.of(resource));
					}
				}

			}
		}
	}

	private Resource.InputSupplier<ResourceMetadata> convert(JsonObject object) {
		return () -> ResourceMetadata.create(new ByteArrayInputStream(new Gson().toJson(object).getBytes(StandardCharsets.UTF_8)));
	}
}
