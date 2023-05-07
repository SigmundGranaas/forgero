package com.sigmundgranaas.forgero.fabric.mixins;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.texture.V2.FileLoader;
import com.sigmundgranaas.forgero.core.texture.V2.TextureGenerator;
import com.sigmundgranaas.forgero.fabric.resources.FileService;
import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceImpl;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;

import static com.sigmundgranaas.forgero.fabric.client.ForgeroClient.TEXTURES;


@Mixin(LifecycledResourceManagerImpl.class)
public abstract class LifecycleResourceManagerImplMixin {

    @Inject(method = "getResource", at = @At("HEAD"), cancellable = true)
    public void getResource(Identifier id, CallbackInfoReturnable<Resource> cir) throws IOException {
        if (id.getPath().contains(".png")) {
            var textureId = id.getPath().replace("textures/item/", id.getNamespace() + ":");
            if (TEXTURES.containsKey(textureId)) {
                FileLoader loader = new FileService();

                var texture = TextureGenerator.getInstance(loader).getTexture(TEXTURES.get(textureId));
                if (texture.isPresent()) {
                    Resource resource = new ResourceImpl(Forgero.NAMESPACE, id, texture.get().getStream(), null);
                    cir.setReturnValue(resource);
                }
            }
        }
    }
}