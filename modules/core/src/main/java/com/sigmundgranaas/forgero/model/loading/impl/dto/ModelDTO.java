package com.sigmundgranaas.forgero.model.loading.impl.dto;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public record ModelDTO(
		@Nullable String id,
		@Nullable String type,
		@Nullable List<LayerDTO> layers,
		@Nullable List<SlotDTO> slots,
		@Nullable List<MountPointDTO> mountPoints,
		@Nullable String texture,
		@Nullable TexturesDTO textures,
		@Nullable String target,
		@Nullable String context,
		@Nullable String parent,
		@Nullable JsonElement display
) {
	public Optional<List<LayerDTO>> getLayers() {
		return Optional.ofNullable(layers);
	}

	public Optional<List<SlotDTO>> getSlots() {
		return Optional.ofNullable(slots);
	}

	public Optional<List<MountPointDTO>> getMountPoints() {
		return Optional.ofNullable(mountPoints);
	}

	public Optional<String> getTexture() {
		return Optional.ofNullable(texture);
	}

	public Optional<TexturesDTO> getTextures() {
		return Optional.ofNullable(textures);
	}

	public Optional<String> getTarget() {
		return Optional.ofNullable(target);
	}

	public Optional<String> getContext() {
		return Optional.ofNullable(context);
	}

	public Optional<OpenIdentifier> getOpenIdentifierId() {
		return getId().map(OpenIdentifier::parse);
	}

	public Optional<String> getId() {
		return Optional.ofNullable(id);
	}

	public Optional<String> getParent() {
		return Optional.ofNullable(parent);
	}

	public Optional<JsonElement> getDisplay() {
		return Optional.ofNullable(display);
	}
}
