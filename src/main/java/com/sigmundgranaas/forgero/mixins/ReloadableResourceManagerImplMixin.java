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

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;

@Mixin(LifecycledResourceManagerImpl.class)
public abstract class ReloadableResourceManagerImplMixin {

    @Shadow
    public abstract Resource getResource(Identifier id);

    @Inject(method = "getResource(Lnet/minecraft/util/Identifier;)Lnet/minecraft/resource/Resource;", at = @At("HEAD"), cancellable = true)
    public void getResource(Identifier id, CallbackInfoReturnable<Resource> cir) throws IOException {

        if (id.getNamespace().equals(ForgeroInitializer.MOD_NAMESPACE) && id.getPath().contains(".png") && id.getPath().split(ELEMENT_SEPARATOR).length > 1 && !id.getPath().contains("transparent")) {
            FabricTextureIdentifierFactory factory = new FabricTextureIdentifierFactory();

            Optional<ToolPartModelTextureIdentifier> identifierResult = factory.createToolPartTextureIdentifier(id.getPath());

            if (identifierResult.isPresent() && ForgeroToolPartTextureRegistry.getInstance(factory).isGeneratedTexture(identifierResult.get())) {

                //if (!PaletteResourceRegistry.getInstance().premadePalette(identifierResult.get().getPaletteIdentifier())) {
                TextureLoader loader = new FabricTextureLoader(MinecraftClient.getInstance().getResourceManager()::getResource);

                Texture toolPartTexture = CachedToolPartTextureService.getInstance(loader).getTexture(identifierResult.get());

                Resource resource = new ResourceImpl(ForgeroInitializer.MOD_NAMESPACE, id, toolPartTexture.getStream(), null);

                cir.setReturnValue(resource);
                // }
            }
        }
    }
}