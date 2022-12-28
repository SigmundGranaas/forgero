package com.sigmundgranaas.forgerofabric.mixins;

import com.sigmundgranaas.forgerofabric.resources.FileService;
import com.sigmundgranaas.forgero.texture.V2.FileLoader;
import com.sigmundgranaas.forgero.texture.V2.TextureGenerator;
import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

import static com.sigmundgranaas.forgerofabric.client.ForgeroClient.TEXTURES;

@Mixin(LifecycledResourceManagerImpl.class)
public abstract class LifecycleResourceManagerImplMixin {

    @Inject(method = "getResource", at = @At("HEAD"), cancellable = true)
    public void getResource(Identifier id, CallbackInfoReturnable<Optional<Resource>> cir) {
        if (id.getPath().contains(".png")) {
            var textureId = id.getPath().replace("textures/item/", id.getNamespace() + ":");
            if (TEXTURES.containsKey(textureId)) {
                FileLoader loader = new FileService();

                var texture = TextureGenerator.getInstance(loader).getTexture(TEXTURES.get(textureId));
                if (texture.isPresent()) {
                    Resource resource = new Resource(id.getNamespace(), texture.get()::getStream);

                    cir.setReturnValue(Optional.of(resource));
                }
            }
        }
    }
}