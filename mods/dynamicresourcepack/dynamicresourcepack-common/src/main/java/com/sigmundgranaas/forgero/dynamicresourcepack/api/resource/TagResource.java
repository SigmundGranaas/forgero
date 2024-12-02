package com.sigmundgranaas.forgero.dynamicresourcepack.api.resource;

import com.mojang.serialization.Codec;

import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.Identifier;

import java.util.ArrayList;

public class TagResource {
	public static final Codec<TagResource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("replace", false).forGetter(TagResource::shouldReplace),
			Codec.list(RecordCodecBuilder.<TagEntry>create(i -> i.group(
							Identifier.CODEC.fieldOf("id").forGetter(TagEntry::id),
							Codec.BOOL.optionalFieldOf("required", true).forGetter(TagEntry::required)
					).apply(i, TagEntry::new)))
					.fieldOf("values")
					.xmap(ArrayList::new, ArrayList::new)
					.forGetter(TagResource::getEntries)
	).apply(instance, TagResource::new));



	private final ArrayList<TagEntry> entries;
	private final boolean replace;

	private TagResource(boolean replace, ArrayList<TagEntry> entries) {
		this.entries = entries;
		this.replace = replace;
	}

	public static TagResource create() {
		return new TagResource(false, new ArrayList<>());
	}

	public static TagResource createReplacingTag() {
		return new TagResource(true, new ArrayList<>());
	}

	public static TagResource of(Identifier... identifiers) {
		TagResource tag = create();
		for (Identifier id : identifiers) {
			tag.entry(id);
		}
		return tag;
	}

	public ArrayList<TagEntry> getEntries() {
		return entries;
	}

	public boolean shouldReplace() {
		return replace;
	}

	public void entry(Identifier identifier) {
		validateIdentifier(identifier);
		entries.add(new TagEntry(identifier, true));
	}

	public void optionalEntry(Identifier identifier) {
		validateIdentifier(identifier);
		entries.add(new TagEntry(identifier, false));
	}

	public void tag(Identifier identifier) {
		entries.add(new TagEntry(identifier, true));
	}

	public void optionalTag(Identifier identifier) {
		if (!identifier.getPath().startsWith("#")) {
			throw new IllegalArgumentException("Tag references must start with #: " + identifier);
		}
		entries.add(new TagEntry(identifier, false));
	}

	private void validateIdentifier(Identifier identifier) {
		if (identifier.getPath().startsWith("#")) {
			throw new IllegalArgumentException("Direct entries cannot start with #: " + identifier);
		}
	}

	public record TagEntry(Identifier id, boolean required) {
	}
}
