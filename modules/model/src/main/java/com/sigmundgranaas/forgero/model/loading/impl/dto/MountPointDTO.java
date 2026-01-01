package com.sigmundgranaas.forgero.model.loading.impl.dto;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record MountPointDTO(String name, List<Integer> position) {

	public static final Codec<MountPointDTO> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("name").forGetter(MountPointDTO::name),
			Codec.list(Codec.INT).optionalFieldOf("position", List.of(0, 0)).forGetter(MountPointDTO::position)
	).apply(instance, MountPointDTO::new));
}
