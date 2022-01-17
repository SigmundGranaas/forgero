package com.sigmundgranaas.forgero.mixins;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.client.texture.FabricTextureIdentifierFactory;
import com.sigmundgranaas.forgero.client.texture.FabricTextureLoader;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.ToolPartTextureIdentifier;
import com.sigmundgranaas.forgero.core.texture.CachedToolPartTextureService;
import com.sigmundgranaas.forgero.core.texture.Texture;
import com.sigmundgranaas.forgero.core.texture.TextureLoader;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceImpl;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.util.Optional;

@Mixin(ReloadableResourceManagerImpl.class)
public abstract class ReloadableResourceManagerImplMixin {
    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    public abstract Resource getResource(Identifier id);

    @Inject(method = "getResource(Lnet/minecraft/util/Identifier;)Lnet/minecraft/resource/Resource;", at = @At("HEAD"), cancellable = true)
    public void getResource(Identifier id, CallbackInfoReturnable<Resource> cir) throws IOException {

        if (id.getNamespace().equals(Forgero.MOD_NAMESPACE)) {
            Optional<ToolPartTextureIdentifier> identifierResult = new FabricTextureIdentifierFactory().createToolPartTextureIdentifier(id);

            if (identifierResult.isPresent()) {
                TextureLoader loader = new FabricTextureLoader(((ReloadableResourceManagerImpl) (Object) this)::getResource);

                Texture toolPartTexture = CachedToolPartTextureService.getInstance(loader).getTexture(identifierResult.get());

                Resource resource = new ResourceImpl(Forgero.MOD_NAMESPACE, id, toolPartTexture.getStream(), null);

                cir.setReturnValue(resource);
            }
        }
    }
}
