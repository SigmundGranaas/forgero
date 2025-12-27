package com.sigmundgranaas.forgero.drp.impl.builder;

import com.sigmundgranaas.forgero.drp.api.tag.TagBuilder;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base implementation for tag builders.
 */
public abstract class AbstractTagBuilder<T extends TagBuilder<T>> implements TagBuilder<T> {

	protected final Identifier id;
	protected final String type;
	protected final List<TagEntry> entries = new ArrayList<>();
	protected boolean replace = false;

	protected AbstractTagBuilder(Identifier id, String type) {
		this.id = id;
		this.type = type;
	}

	@Override
	public Identifier getId() {
		return id;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T add(String id) {
		return add(Identifier.tryParse(id));
	}

	@Override
	@SuppressWarnings("unchecked")
	public T add(Identifier id) {
		entries.add(TagEntryImpl.item(id));
		return (T) this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T addAll(List<Identifier> ids) {
		ids.forEach(this::add);
		return (T) this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T addAll(String... ids) {
		for (String id : ids) {
			add(id);
		}
		return (T) this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T includeTag(String tagId) {
		String normalized = tagId.startsWith("#") ? tagId.substring(1) : tagId;
		return includeTag(Identifier.tryParse(normalized));
	}

	@Override
	@SuppressWarnings("unchecked")
	public T includeTag(Identifier tagId) {
		entries.add(TagEntryImpl.tag(tagId));
		return (T) this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T setReplace(boolean replace) {
		this.replace = replace;
		return (T) this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T addOptional(Identifier id) {
		entries.add(TagEntryImpl.optionalItem(id));
		return (T) this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T addOptional(String id) {
		return addOptional(Identifier.tryParse(id));
	}

	@Override
	@SuppressWarnings("unchecked")
	public T includeOptionalTag(Identifier tagId) {
		entries.add(TagEntryImpl.optionalTag(tagId));
		return (T) this;
	}

	@Override
	public boolean isReplace() {
		return replace;
	}

	@Override
	public List<TagEntry> getEntries() {
		return List.copyOf(entries);
	}
}
