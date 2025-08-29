// FILE: /home/sigmund/Documents/projects/forgero/1-20/fabric/modules/render/src/main/java/com/sigmundgranaas/forgero/render/model/item/ForgeroModelProvider.java
package com.sigmundgranaas.forgero.render.model.item;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.model.api.item.Model;
import com.sigmundgranaas.forgero.render.ForgeroClient;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelResolver;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ForgeroModelProvider implements ModelResolver {

	@Override
	public @Nullable UnbakedModel resolveModel(Context context) {
		final ForgeroClient.ClientServices services = ForgeroClient.services;

		Identifier requestedId = context.id();
		String path = requestedId.getPath();

		if (path.startsWith("item/")) {
			path = path.substring("item/".length());
		}
		OpenIdentifier forgeroId = new OpenIdentifier(requestedId.getNamespace(), path);
		OpenIdentifier forgeroIdNormalized = new OpenIdentifier(requestedId.getNamespace(), path.replace("_", "-"));

		Optional<Model> modelOpt = services.modelRegistry().find(forgeroId).or(() -> services.modelRegistry().find(forgeroIdNormalized));

		if (modelOpt.isPresent()) {
			OpenIdentifier componentId = modelOpt.get().getTarget().orElse(modelOpt.get().getIdentifier());
			Optional<Component> baselineComponentOpt = services.componentRegistry().get(componentId);

			if (baselineComponentOpt.isPresent()) {
				return new UnbakedForgeroModel(
						baselineComponentOpt.get(),
						services.itemToComponent(),
						services.modelRegistry()
				);
			}
		}
		return null;
	}
}
