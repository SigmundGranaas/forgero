package com.sigmundgranaas.forgero.fabric.mixins;

import com.sigmundgranaas.forgero.core.texture.V2.FileLoader;
import com.sigmundgranaas.forgero.core.texture.V2.TextureGenerator;
import com.sigmundgranaas.forgero.fabric.client.ForgeroClient;
import com.sigmundgranaas.forgero.fabric.resources.ResourceLoadedFileService;
import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(LifecycledResourceManagerImpl.class)
public abstract class LifecycleResourceManagerImplMixin {

    @Shadow
    public abstract Optional<Resource> getResource(Identifier identifier);

    @Inject(method = "getResource", at = @At("RETURN"), cancellable = true)
    public void getResource(Identifier id, CallbackInfoReturnable<Optional<Resource>> cir) {
        if (id.getPath().contains(".png") && cir.getReturnValue().isEmpty()) {
            var textureId = id.getPath().replace("textures/item/", id.getNamespace() + ":");
            if (ForgeroClient.TEXTURES.containsKey(textureId)) {

                FileLoader loader = new ResourceLoadedFileService();
                var texture = TextureGenerator.getInstance(loader, ForgeroClient.PALETTE_REMAP).getTexture(ForgeroClient.TEXTURES.get(textureId));
                if (texture.isPresent()) {
                    Resource resource = new Resource(id.getNamespace(), texture.get()::getStream);

                    cir.setReturnValue(Optional.of(resource));
                }
            }
        }
    }
}