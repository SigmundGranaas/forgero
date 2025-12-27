package com.sigmundgranaas.forgero.drp.impl.builder;

import com.sigmundgranaas.forgero.drp.api.tag.TagBuilder;
import net.minecraft.util.Identifier;

/**
 * Implementation of TagBuilder.TagEntry.
 */
public record TagEntryImpl(Identifier id, boolean isTag, boolean isOptional) implements TagBuilder.TagEntry {

	public static TagEntryImpl item(Identifier id) {
		return new TagEntryImpl(id, false, false);
	}

	public static TagEntryImpl tag(Identifier id) {
		return new TagEntryImpl(id, true, false);
	}

	public static TagEntryImpl optionalItem(Identifier id) {
		return new TagEntryImpl(id, false, true);
	}

	public static TagEntryImpl optionalTag(Identifier id) {
		return new TagEntryImpl(id, true, true);
	}
}
