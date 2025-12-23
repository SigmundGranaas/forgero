package com.sigmundgranaas.forgero.properties.minecraft.entityfilter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

/**
 * Filters entities based on whether they have a specific entity type tag.
 */
public record HasTagFilter(String tag) implements EntityFilter {
	public static final String TYPE = "forgero:has_tag";
	public static final Codec<HasTagFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("tag").forGetter(HasTagFilter::tag)
	).apply(instance, HasTagFilter::new));

	@Override
	public boolean test(Entity source, Entity candidate) {
		Identifier tagId = Identifier.tryParse(tag);
		if (tagId == null) {
			return false;
		}

		TagKey<?> tagKey = TagKey.of(Registries.ENTITY_TYPE.getKey(), tagId);
		return Registries.ENTITY_TYPE.getEntry(candidate.getType())
				.streamTags()
				.anyMatch(tag -> tag.equals(tagKey));
	}

	@Override
	public String type() {
		return TYPE;
	}
}
