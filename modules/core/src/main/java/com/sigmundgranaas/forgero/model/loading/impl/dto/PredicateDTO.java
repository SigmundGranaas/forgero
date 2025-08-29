package com.sigmundgranaas.forgero.model.loading.impl.dto;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record PredicateDTO(
		String type,
		@Nullable String tag,
		@Nullable Float pull,
		@Nullable Boolean pulling
) {
	public Optional<String> getTag() {
		return Optional.ofNullable(tag);
	}

	public Optional<Float> getPull() {
		return Optional.ofNullable(pull);
	}

	public Optional<Boolean> getPulling() {
		return Optional.ofNullable(pulling);
	}

	public static final Codec<PredicateDTO> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("type").forGetter(PredicateDTO::type),
			Codec.STRING.optionalFieldOf("tag").forGetter(PredicateDTO::getTag),
			Codec.FLOAT.optionalFieldOf("pull").forGetter(PredicateDTO::getPull),
			Codec.BOOL.optionalFieldOf("pulling").forGetter(PredicateDTO::getPulling)
	).apply(instance, (type, tag, pull, pulling) -> new PredicateDTO(type, tag.orElse(null), pull.orElse(null), pulling.orElse(null))));
}
