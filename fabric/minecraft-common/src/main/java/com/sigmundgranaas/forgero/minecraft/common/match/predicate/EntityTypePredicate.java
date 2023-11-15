package com.sigmundgranaas.forgero.minecraft.common.match.predicate;

import com.google.common.base.Joiner;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.EntityType;

import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

@Data
@NoArgsConstructor
public abstract class EntityTypePredicate {
	public static final EntityTypePredicate ANY = new EntityTypePredicate() {
		@Override
		public boolean matches(EntityType<?> type) {
			return true;
		}

		@Override
		public JsonElement toJson() {
			return JsonNull.INSTANCE;
		}
	};
	private static final Joiner COMMA_JOINER = Joiner.on(", ");

	public static EntityTypePredicate create(EntityType<?> type) {
		return new EntityTypePredicate.Single(type);
	}

	public static EntityTypePredicate create(TagKey<EntityType<?>> tag) {
		return new EntityTypePredicate.Tagged(tag);
	}

	public static EntityTypePredicate fromJson(@Nullable JsonElement json) {
		if (json != null && !json.isJsonNull()) {
			String string = JsonHelper.asString(json, "type");
			Identifier identifier;
			if (string.startsWith("#")) {
				identifier = new Identifier(string.substring(1));
				return new Tagged(TagKey.of(RegistryKeys.ENTITY_TYPE, identifier));
			} else {
				identifier = new Identifier(string);
				EntityType<?> entityType = Registries.ENTITY_TYPE.getOrEmpty(identifier).orElseThrow(() -> new JsonSyntaxException("Unknown entity type '" + identifier + "', valid types are: " + COMMA_JOINER.join(Registries.ENTITY_TYPE.getIds())));
				return new Single(entityType);
			}
		} else {
			return ANY;
		}
	}

	public abstract boolean matches(EntityType<?> type);

	public abstract JsonElement toJson();

	@Getter
	@NoArgsConstructor
	public static class Tagged extends EntityTypePredicate {
		private TagKey<EntityType<?>> tag;

		public Tagged(TagKey<EntityType<?>> tag) {
			this.tag = tag;
		}

		@Override
		public boolean matches(EntityType<?> type) {
			return type.isIn(this.tag);
		}

		@Override
		public JsonElement toJson() {
			return new JsonPrimitive("#" + this.tag.id());
		}
	}

	@Getter
	@NoArgsConstructor
	public static class Single extends EntityTypePredicate {
		private EntityType<?> type;

		public Single(EntityType<?> type) {
			this.type = type;
		}

		@Override
		public boolean matches(EntityType<?> type) {
			return this.type == type;
		}

		@Override
		public JsonElement toJson() {
			return new JsonPrimitive(Registries.ENTITY_TYPE.getId(this.type).toString());
		}
	}
}
