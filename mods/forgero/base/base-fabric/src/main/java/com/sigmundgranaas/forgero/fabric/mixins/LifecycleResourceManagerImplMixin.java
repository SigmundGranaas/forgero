package com.sigmundgranaas.forgero.fabric.mixins;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import static com.sigmundgranaas.forgero.fabric.client.ForgeroClient.TEXTURES;

import java.util.List;

import com.sigmundgranaas.forgero.core.model.PaletteTemplateModel;
import com.sigmundgranaas.forgero.core.texture.V2.FileLoader;
import com.sigmundgranaas.forgero.core.texture.V2.TextureGenerator;
import com.sigmundgranaas.forgero.fabric.client.ForgeroClient;
import com.sigmundgranaas.forgero.fabric.resources.FileService;
import net.minecraft.resource.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resource.metadata.ResourceMetadata;
import net.minecraft.util.Identifier;
import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.Resource;

@Mixin(LifecycledResourceManagerImpl.class)
public abstract class LifecycleResourceManagerImplMixin {

	@Shadow
	@Final
	private List<ResourcePack> packs;

	@Inject(method = "getResource", at = @At("RETURN"), cancellable = true)
	public void getResource(Identifier id, CallbackInfoReturnable<Optional<Resource>> cir) {
		if (id.getPath().contains(".png") && cir.getReturnValue().isEmpty()) {
			var textureId = id.getPath().replace("textures/item/", id.getNamespace() + ":");
			if (TEXTURES.containsKey(textureId)) {
				FileLoader loader = new FileService();
				var template = TEXTURES.get(textureId);
				if(template instanceof PaletteTemplateModel paletteTemplateModel){
					var texture = TextureGenerator.getInstance(loader, ForgeroClient.PALETTE_REMAP).getTexture(paletteTemplateModel);
					if (texture.isPresent()) {
						var metadata = TextureGenerator.getInstance(loader, ForgeroClient.PALETTE_REMAP).getMetadata(paletteTemplateModel, ".mcmeta");
						Resource resource;

						resource = metadata
								.map(object -> new Resource(packs.get(0), texture.get()::getStream, convert(object)))
								.orElseGet(() -> new Resource(packs.get(0), texture.get()::getStream));
						cir.setReturnValue(Optional.of(resource));
					}
				}
			}
		}
	}

	private InputSupplier<ResourceMetadata> convert(JsonObject object) {
		return () -> ResourceMetadata.create(new ByteArrayInputStream(new Gson().toJson(object).getBytes(StandardCharsets.UTF_8)));
	}
}
