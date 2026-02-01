package com.sigmundgranaas.forgero.model.loading.impl;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.model.api.item.CompositeModel;
import com.sigmundgranaas.forgero.model.api.item.EmptyModel;
import com.sigmundgranaas.forgero.model.api.item.Model;
import com.sigmundgranaas.forgero.model.api.ModelLayer;
import com.sigmundgranaas.forgero.model.api.ModelSlot;
import com.sigmundgranaas.forgero.model.api.ModelVariant;
import com.sigmundgranaas.forgero.model.api.MountPoint;
import com.sigmundgranaas.forgero.model.api.Offset;
import com.sigmundgranaas.forgero.model.api.item.TextureModel;
import com.sigmundgranaas.forgero.model.loading.impl.dto.*;
import com.sigmundgranaas.forgero.model.match.Predicate;
import com.sigmundgranaas.forgero.model.match.predicate.BowPullPredicate;
import com.sigmundgranaas.forgero.model.match.predicate.ChildTagPredicate;
import com.sigmundgranaas.forgero.model.match.predicate.RootTagPredicate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Translates DTO (Data Transfer Object) representations of models, loaded from JSON,
 * into the main Forgero domain model classes.
 */
public class ModelTranslator {
	private final Supplier<TagResolver> resolverSupplier;

	/**
	 * Creates a translator with inheritance-aware tag predicates.
	 */
	public ModelTranslator(Supplier<TagResolver> resolverSupplier) {
		this.resolverSupplier = resolverSupplier;
	}

	/**
	 * Creates a translator with direct-only tag matching (no inheritance).
	 */
	public ModelTranslator() {
		this(null);
	}

	public Model toDomain(OpenIdentifier fileDerivedId, ModelDTO dto) {
		OpenIdentifier finalId = dto.getOpenIdentifierId().orElse(fileDerivedId);

		return switch (dto.type()) {
			case "forgero:composite_model" -> toCompositeModel(finalId, dto);
			case "forgero:texture_model" -> toTextureModel(finalId, dto);
			case "forgero:empty_model" -> EmptyModel.INSTANCE;
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

		List<MountPoint> mountPoints = Optional.ofNullable(dto.mountPoints())
				.orElse(Collections.emptyList())
				.stream()
				.map(this::toMountPoint)
				.toList();

		Optional<OpenIdentifier> target = dto.getTarget().map(OpenIdentifier::parse);
		Optional<String> context = dto.getContext();
		Optional<OpenIdentifier> parent = dto.getParent().map(OpenIdentifier::parse);
		Optional<JsonElement> display = dto.getDisplay();

		return new CompositeModel(id, layers, slots, mountPoints, target, context, parent, display);
	}

	protected ModelLayer toModelLayer(LayerDTO dto) {
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

	protected ModelSlot toModelSlot(SlotDTO dto) {
		RendererDTO renderer = dto.renderer();
		Optional<String> context = Optional.ofNullable(renderer.context());
		return new ModelSlot(dto.id(), dto.order(), context, dto.getTargetMount(), dto.getChildMount(), dto.getDynamicKey());
	}

	private TextureModel toTextureModel(OpenIdentifier id, ModelDTO dto) {
		Optional<OpenIdentifier> target = dto.getTarget().map(OpenIdentifier::parse);
		Optional<String> context = dto.getContext();
		Optional<OpenIdentifier> parent = dto.getParent().map(OpenIdentifier::parse);
		Optional<JsonElement> display = dto.getDisplay();

		List<MountPoint> mountPoints = Optional.ofNullable(dto.mountPoints())
				.orElse(Collections.emptyList())
				.stream()
				.map(this::toMountPoint)
				.toList();

		TexturesDTO textures = dto.textures();
		if (textures != null) {
			List<ModelVariant> variants = Optional.ofNullable(textures.variants())
					.orElse(Collections.emptyList())
					.stream()
					.map(this::toModelVariant)
					.toList();
			return new TextureModel(id, textures.defaultTexture(), variants, Optional.empty(), mountPoints, target, context, parent, display);
		} else {
			return new TextureModel(id, dto.texture(), Collections.emptyList(), Optional.empty(), mountPoints, target, context, parent, display);
		}
	}

	private MountPoint toMountPoint(MountPointDTO dto) {
		List<Integer> pos = dto.position();
		if (pos == null || pos.size() < 2) {
			return new MountPoint(dto.name(), 0, 0);
		}
		int x = pos.get(0);
		int y = pos.get(1);

		// Invert the Y-coordinate to switch from a bottom-left definition
		// to the top-left system used by image processing.
		// We assume a 16x16 canvas for coordinate definition.
		// A user-defined Y=0 (bottom) becomes pixel Y=15 (top).
		// A user-defined Y=15 (top) becomes pixel Y=0 (top).
		int invertedY = 16 - 1 - y;

		return new MountPoint(dto.name(), x, invertedY);
	}

	private ModelVariant toModelVariant(VariantDTO dto) {
		List<Predicate> predicates = dto.predicate().stream().map(this::toPredicate).collect(Collectors.toList());
		Optional<String> texture = Optional.ofNullable(dto.texture());
		Optional<Model> model = Optional.empty();
		if (dto.model() != null && dto.model().equals("forgero:common/empty")) {
			model = Optional.of(EmptyModel.INSTANCE);
		}
		// Note: Non-empty model identifiers are not currently supported for variants.
		// If needed, model resolution should be added here to look up models by identifier.
		Optional<Offset> offset = Optional.ofNullable(dto.offset()).map(arr -> new Offset(arr[0], arr[1]));

		return new ModelVariant(predicates, texture, model, offset);
	}

	private Predicate toPredicate(PredicateDTO dto) {
		return switch (dto.type()) {
			case "forgero:root_tag" -> new RootTagPredicate(OpenIdentifier.parse(dto.tag()), resolverSupplier);
			case "forgero:bow_pull" -> new BowPullPredicate(dto.pull(), dto.pulling());
			case "forgero:child_tag" -> new ChildTagPredicate(OpenIdentifier.parse(dto.tag()), resolverSupplier);
			default -> throw new IllegalArgumentException("Unknown predicate type: " + dto.type());
		};
	}
}
