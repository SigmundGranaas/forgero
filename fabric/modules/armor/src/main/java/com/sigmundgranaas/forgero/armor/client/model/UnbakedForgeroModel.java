package com.sigmundgranaas.forgero.armor.client.model;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.model.registry.api.ModelRegistry;
import net.minecraft.client.render.model.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

/**

 An unbaked model for a single type of Forgero component.

 It holds the baseline component and a function to resolve components from item stacks,

 passing these dependencies down to the BakedForgeroModel during the baking process.
 */
public class UnbakedForgeroModel implements UnbakedModel {

	private final Component baselineComponent;
	private final Function<ItemStack, Optional<Component>> itemToComponentConverter;
	private final ModelRegistry modelRegistry;

	public UnbakedForgeroModel(Component baselineComponent, Function<ItemStack, Optional<Component>> itemToComponentConverter, ModelRegistry modelRegistry) {
		this.baselineComponent = baselineComponent;
		this.itemToComponentConverter = itemToComponentConverter;
		this.modelRegistry = modelRegistry;
	}

	@Override
	public Collection<Identifier> getModelDependencies() {
		// Find our own model definition to see if it has a parent.
		return modelRegistry.find(baselineComponent.id())
				.flatMap(com.sigmundgranaas.forgero.model.api.Model::getParent)
				.map(parentId -> new Identifier(parentId.toString()))
				.map(Collections::singletonList)
				.orElse(Collections.emptyList());
	}

	@Override
	public void setParents(Function<Identifier, UnbakedModel> modelLoader) {
	}

	@Nullable
	@Override
	public BakedModel bake(Baker baker, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings settings, Identifier modelId) {
		return new BakedForgeroModel(baker, textureGetter, settings, modelId, baselineComponent, itemToComponentConverter, modelRegistry);
	}
}
