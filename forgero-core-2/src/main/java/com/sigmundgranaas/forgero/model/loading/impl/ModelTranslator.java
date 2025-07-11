// FILE: /home/sigmund/Documents/projects/forgero/1-20/forgero-core-2/src/main/java/com/sigmundgranaas/forgero/model/loading/impl/ModelTranslator.java
package com.sigmundgranaas.forgero.model.loading.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.CompositeModel;
import com.sigmundgranaas.forgero.model.api.EmptyModel;
import com.sigmundgranaas.forgero.model.api.Model;
import com.sigmundgranaas.forgero.model.api.ModelLayer;
import com.sigmundgranaas.forgero.model.api.ModelSlot;
import com.sigmundgranaas.forgero.model.api.ModelVariant;
import com.sigmundgranaas.forgero.model.api.Offset;
import com.sigmundgranaas.forgero.model.api.TextureModel; // Changed from StaticModel to TextureModel
import com.sigmundgranaas.forgero.model.match.Predicate;
import com.sigmundgranaas.forgero.model.match.predicate.BowPullPredicate;
import com.sigmundgranaas.forgero.model.match.predicate.RootTagPredicate;
import com.sigmundgranaas.forgero.model.loading.impl.dto.*; // Import all new DTOs

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Translates DTO (Data Transfer Object) representations of models, loaded from JSON,
 * into the main Forgero domain model classes.
 */
public class ModelTranslator {

	public Model toDomain(OpenIdentifier id, ModelDTO dto) {
		return switch (dto.type()) {
			case "forgero:composite_model" -> toCompositeModel(id, dto);
			case "forgero:texture_model" -> toTextureModel(id, dto); // Changed to toTextureModel
			case "forgero:empty_model" -> EmptyModel.INSTANCE; // Use the singleton instance
			default -> throw new IllegalArgumentException("Unknown model type: " + dto.type());
		};
	}

	private CompositeModel toCompositeModel(OpenIdentifier id, ModelDTO dto) {
		List<ModelLayer> layers = Optional.ofNullable(dto.layers())
				.orElse(Collections.emptyList())
				.stream()
				.map(this::toModelLayer)
				.toList();

		List<ModelSlot> slots = Optional.ofNullable(dto.slots())
				.orElse(Collections.emptyList())
				.stream()
				.map(this::toModelSlot)
				.toList();

		Optional<OpenIdentifier> target = dto.getTarget().map(OpenIdentifier::new);
		Optional<String> context = dto.getContext();

		return new CompositeModel(id, layers, slots, target, context);
	}

	private ModelLayer toModelLayer(LayerDTO dto) {
		TexturesDTO textures = dto.textures();
		String defaultTexture = textures.defaultTexture();
		Optional<Offset> offset = Optional.ofNullable(dto.offset()).map(arr -> new Offset(arr[0], arr[1]));

		List<ModelVariant> variants = Optional.ofNullable(textures.variants())
				.orElse(Collections.emptyList())
				.stream()
				.map(this::toModelVariant)
				.toList();

		return new ModelLayer(defaultTexture, dto.order(), variants, offset);
	}

	private ModelSlot toModelSlot(SlotDTO dto) {
		RendererDTO renderer = dto.renderer();
		Optional<String> context = Optional.ofNullable(renderer.context());
		return new ModelSlot(dto.id(), dto.order(), context);
	}

	private TextureModel toTextureModel(OpenIdentifier id, ModelDTO dto) { // Changed to TextureModel
		Optional<OpenIdentifier> target = dto.getTarget().map(OpenIdentifier::new);
		Optional<String> context = dto.getContext();

		TexturesDTO textures = dto.textures();
		if (textures != null) {
			List<ModelVariant> variants = Optional.ofNullable(textures.variants())
					.orElse(Collections.emptyList())
					.stream()
					.map(this::toModelVariant)
					.toList();
			return new TextureModel(id, textures.defaultTexture(), variants, Optional.empty(), target, context);
		} else {
			// Fallback for direct "texture" field on root (if used, though spec shows it under "textures")
			return new TextureModel(id, dto.texture(), Collections.emptyList(), Optional.empty(), target, context);
		}
	}

	private ModelVariant toModelVariant(VariantDTO dto) {
		List<Predicate> predicates = dto.predicate().stream().map(this::toPredicate).collect(Collectors.toList());
		Optional<String> texture = Optional.ofNullable(dto.texture());
		Optional<Object> model; // Object type to hold either EmptyModel.INSTANCE or String ID
		if (dto.model() != null && dto.model().equals("forgero:common/empty")) {
			model = Optional.of(EmptyModel.INSTANCE);
		} else {
			model = Optional.ofNullable(dto.model());
		}
		Optional<Offset> offset = Optional.ofNullable(dto.offset()).map(arr -> new Offset(arr[0], arr[1]));

		return new ModelVariant(predicates, texture, model, offset);
	}

	private Predicate toPredicate(PredicateDTO dto) {
		return switch (dto.type()) {
			case "forgero:root_tag" -> new RootTagPredicate(new OpenIdentifier(dto.tag()));
			case "forgero:bow_pull" -> new BowPullPredicate(dto.pull(), dto.pulling());
			default -> throw new IllegalArgumentException("Unknown predicate type: " + dto.type());
		};
	}
}
