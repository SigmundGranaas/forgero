package com.sigmundgranaas.forgero.core.condition.api;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.logical.AndCondition;
import com.sigmundgranaas.forgero.core.condition.logical.NotCondition;
import com.sigmundgranaas.forgero.core.condition.logical.OrCondition;
import com.sigmundgranaas.forgero.data.loading.impl.codec.JsonElementCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ConditionCodec implements Codec<Condition> {
	private final Codec<StaticCondition> staticDispatcher;
	private final Codec<DynamicCondition> dynamicDispatcher;

	private static final Codec<List<JsonElement>> PREDICATE_LIST_CODEC =
			Codec.either(Codec.list(JsonElementCodec.INSTANCE), JsonElementCodec.INSTANCE)
					.xmap(
							either -> either.map(list -> list, List::of),
							list -> list.size() == 1 ? Either.right(list.get(0)) : Either.left(list)
					);

	public ConditionCodec(Map<String, Codec<? extends StaticCondition>> staticCodecs, Map<String, Codec<? extends DynamicCondition>> dynamicCodecs) {
		this.staticDispatcher = createDispatcher(staticCodecs, StaticCondition.class);
		this.dynamicDispatcher = createDispatcher(dynamicCodecs, DynamicCondition.class);
	}

	private <B> Codec<B> createDispatcher(Map<String, Codec<? extends B>> codecs, Class<B> baseClass) {
		return new Codec<B>() {
			@Override
			public <T> DataResult<Pair<B, T>> decode(DynamicOps<T> ops, T input) {
				return ops.get(input, "type")
						.flatMap(ops::getStringValue)
						.flatMap(typeString -> {
							Codec<? extends B> codec = codecs.get(typeString);
							if (codec == null) {
								return DataResult.error(() -> "Unknown type for " + baseClass.getSimpleName() + ": " + typeString);
							}
							return codec.decode(ops, input);
						})
						.map(pair -> Pair.of(pair.getFirst(), pair.getSecond()));
			}

			@Override
			@SuppressWarnings({"unchecked", "rawtypes"})
			public <T> DataResult<T> encode(B input, DynamicOps<T> ops, T prefix) {
				OpenIdentifier typeId;
				if (input instanceof StaticCondition sc) {
					typeId = sc.type();
				} else if (input instanceof DynamicCondition dc) {
					typeId = dc.type();
				} else {
					return DataResult.error(() -> "Cannot encode unknown condition type: " + input.getClass().getName());
				}

				Codec<? extends B> codec = codecs.get(typeId.toString());
				if (codec == null) {
					return DataResult.error(() -> "No codec registered for encoding type: " + typeId);
				}
				return ((Codec) codec).encode(input, ops, prefix);
			}
		};
	}

	@Override
	public <T> DataResult<Pair<Condition, T>> decode(DynamicOps<T> ops, T input) {
		return PREDICATE_LIST_CODEC.decode(ops, input).flatMap(pair -> {
			List<JsonElement> predicates = pair.getFirst();
			List<StaticCondition> staticResults = new ArrayList<>();
			List<DynamicCondition> dynamicResults = new ArrayList<>();

			for (JsonElement predicateJson : predicates) {
				if (!predicateJson.isJsonObject() || predicateJson.getAsJsonObject().get("type") == null) {
					return DataResult.error(() -> "Predicate must be a JSON object with a 'type' field: " + predicateJson);
				}
				String type = predicateJson.getAsJsonObject().get("type").getAsString();
				Object result = null;

				DataResult<Condition> logicalResult = DataResult.error(() -> "Not a logical condition");
				if (type.equals(AndCondition.TYPE.toString())) {
					logicalResult = AndCondition.codec(this).parse(JsonOps.INSTANCE, predicateJson);
				} else if (type.equals(OrCondition.TYPE.toString())) {
					logicalResult = OrCondition.codec(this).parse(JsonOps.INSTANCE, predicateJson);
				} else if (type.equals(NotCondition.TYPE.toString())) {
					logicalResult = NotCondition.codec(this).parse(JsonOps.INSTANCE, predicateJson);
				}

				if (logicalResult.result().isPresent()) {
					if (type.equals(AndCondition.TYPE.toString())) {
						result = AndCondition.from(logicalResult.getOrThrow(false, System.err::println));
					} else if (type.equals(OrCondition.TYPE.toString())) {
						result = OrCondition.from(logicalResult.getOrThrow(false, System.err::println));
					} else if (type.equals(NotCondition.TYPE.toString())) {
						result = NotCondition.from(logicalResult.getOrThrow(false, System.err::println));
					}
				} else {
					staticDispatcher.parse(JsonOps.INSTANCE, predicateJson).result().ifPresent(staticResults::add);
					dynamicDispatcher.parse(JsonOps.INSTANCE, predicateJson).result().ifPresent(dynamicResults::add);
				}

				if (result instanceof StaticCondition sc) {
					staticResults.add(sc);
				} else if (result instanceof DynamicCondition dc) {
					dynamicResults.add(dc);
				}
			}
			return DataResult.success(Pair.of(new Condition(staticResults, dynamicResults), pair.getSecond()));
		});
	}


	@Override
	public <T> DataResult<T> encode(Condition input, DynamicOps<T> ops, T prefix) {
		List<T> encodedList = new ArrayList<>();
		for (DynamicCondition condition : input.dynamicConditions()) {
			encodeSingle(condition, ops, ops.empty()).result().ifPresent(encodedList::add);
		}
		for (StaticCondition condition : input.staticConditions()) {
			encodeSingle(condition, ops, ops.empty()).result().ifPresent(encodedList::add);
		}
		return DataResult.success(ops.createList(encodedList.stream()));
	}

	private <T> DataResult<T> encodeSingle(Object condition, DynamicOps<T> ops, T prefix) {
		DataResult<T> encodedContent;
		OpenIdentifier typeId;

		if (condition instanceof AndCondition.AndStatic logical) {
			encodedContent = AndCondition.codec(this).encode(logical.toCondition(), ops, prefix);
			typeId = logical.type();
		} else if (condition instanceof AndCondition.AndDynamic logical) {
			encodedContent = AndCondition.codec(this).encode(logical.toCondition(), ops, prefix);
			typeId = logical.type();
		} else if (condition instanceof OrCondition.OrStatic logical) {
			encodedContent = OrCondition.codec(this).encode(logical.toCondition(), ops, prefix);
			typeId = logical.type();
		} else if (condition instanceof OrCondition.OrDynamic logical) {
			encodedContent = OrCondition.codec(this).encode(logical.toCondition(), ops, prefix);
			typeId = logical.type();
		} else if (condition instanceof NotCondition.NotStatic logical) {
			encodedContent = NotCondition.codec(this).encode(logical.toCondition(), ops, prefix);
			typeId = logical.type();
		} else if (condition instanceof NotCondition.NotDynamic logical) {
			encodedContent = NotCondition.codec(this).encode(logical.toCondition(), ops, prefix);
			typeId = logical.type();
		} else if (condition instanceof StaticCondition sc) {
			return staticDispatcher.encode(sc, ops, prefix);
		} else if (condition instanceof DynamicCondition dc) {
			return dynamicDispatcher.encode(dc, ops, prefix);
		} else {
			return DataResult.error(() -> "Unknown condition type for encoding: " + condition.getClass().getName());
		}

		return encodedContent.flatMap(content -> ops.mergeToMap(content, ops.createString("type"), ops.createString(typeId.toString())));
	}
}
