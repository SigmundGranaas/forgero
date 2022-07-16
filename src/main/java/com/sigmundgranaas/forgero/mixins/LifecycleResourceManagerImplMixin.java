package com.sigmundgranaas.forgero.mixins;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.client.texture.FabricTextureIdentifierFactory;
import com.sigmundgranaas.forgero.client.texture.FabricTextureLoader;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.ToolPartModelTextureIdentifier;
import com.sigmundgranaas.forgero.core.texture.CachedToolPartTextureService;
import com.sigmundgranaas.forgero.core.texture.ForgeroToolPartTextureRegistry;
import com.sigmundgranaas.forgero.core.texture.Texture;
import com.sigmundgranaas.forgero.core.texture.TextureLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceImpl;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.util.Optional;

@Mixin(LifecycledResourceManagerImpl.class)
public abstract class LifecycleResourceManagerImplMixin {

    @Inject(method = "getResource", at = @At("HEAD"), cancellable = true)
    public void getResource(Identifier id, CallbackInfoReturnable<Resource> cir) throws IOException {

        if (id.getNamespace().equals(ForgeroInitializer.MOD_NAMESPACE) && id.getPath().contains(".png")) {
            String[] elements = id.getPath().split("/");
            var factory = new FabricTextureIdentifierFactory();
            String toolPartName = elements.length > 1 ? elements[elements.length - 1].replace(".png", "") : "INVALID_TEXTURE";
            if (elements.length > 1 && ForgeroToolPartTextureRegistry.getInstance(factory).isRegistered(toolPartName)) {

                Optional<ToolPartModelTextureIdentifier> identifierResult = factory.createToolPartTextureIdentifier(id.getPath());

                if (identifierResult.isPresent() && ForgeroToolPartTextureRegistry.getInstance(factory).isGeneratedTexture(identifierResult.get())) {
                    TextureLoader loader = new FabricTextureLoader(MinecraftClient.getInstance().getResourceManager()::getResource);

                    Texture toolPartTexture = CachedToolPartTextureService.getInstance(loader).getTexture(identifierResult.get());

                    Resource resource = new ResourceImpl(ForgeroInitializer.MOD_NAMESPACE ,id, toolPartTexture.getStream(), null);

                    cir.setReturnValue(resource);
                    // }
                }
            }
        }
    }
}