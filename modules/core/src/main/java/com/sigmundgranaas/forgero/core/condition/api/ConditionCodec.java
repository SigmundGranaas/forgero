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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class ConditionCodec implements Codec<Condition> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConditionCodec.class);

	private final Codec<StaticCondition> staticDispatcher;
	private final Codec<DynamicCondition> dynamicDispatcher;
	private final Map<String, LogicalConditionHandler> logicalHandlers;

	private static final Codec<List<JsonElement>> PREDICATE_LIST_CODEC =
			Codec.either(Codec.list(JsonElementCodec.INSTANCE), JsonElementCodec.INSTANCE)
					.xmap(
							either -> either.map(list -> list, List::of),
							list -> list.size() == 1 ? Either.right(list.get(0)) : Either.left(list)
					);

	/**
	 * Handler for logical conditions (AND, OR, NOT) with codec creation and factory methods.
	 */
	private record LogicalConditionHandler(
			Function<Codec<Condition>, Codec<Condition>> codecFactory,
			Function<Condition, Object> fromFactory
	) {}

	public ConditionCodec(Map<String, Codec<? extends StaticCondition>> staticCodecs, Map<String, Codec<? extends DynamicCondition>> dynamicCodecs) {
		this.staticDispatcher = createDispatcher(staticCodecs, StaticCondition.class);
		this.dynamicDispatcher = createDispatcher(dynamicCodecs, DynamicCondition.class);
		this.logicalHandlers = buildLogicalHandlers();
	}

	private Map<String, LogicalConditionHandler> buildLogicalHandlers() {
		Map<String, LogicalConditionHandler> handlers = new HashMap<>();
		handlers.put(AndCondition.TYPE.toString(), new LogicalConditionHandler(AndCondition::codec, AndCondition::from));
		handlers.put(OrCondition.TYPE.toString(), new LogicalConditionHandler(OrCondition::codec, OrCondition::from));
		handlers.put(NotCondition.TYPE.toString(), new LogicalConditionHandler(NotCondition::codec, NotCondition::from));
		return handlers;
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
		// First, try to parse as wrapper format: { "static": [...], "dynamic": [...] }
		var wrapperResult = tryDecodeWrapperFormat(ops, input);
		if (wrapperResult.result().isPresent()) {
			return wrapperResult;
		}

		// Fall back to legacy direct predicate list format
		return PREDICATE_LIST_CODEC.decode(ops, input).flatMap(pair -> {
			List<JsonElement> predicates = pair.getFirst();
			List<StaticCondition> staticResults = new ArrayList<>();
			List<DynamicCondition> dynamicResults = new ArrayList<>();

			for (JsonElement predicateJson : predicates) {
				parsePredicateJson(predicateJson, staticResults, dynamicResults);
			}
			return DataResult.success(Pair.of(new Condition(staticResults, dynamicResults), pair.getSecond()));
		});
	}

	/**
	 * Attempts to decode the wrapper format: { "static": [...], "dynamic": [...] }
	 * This is the primary format used in material JSON files.
	 */
	private <T> DataResult<Pair<Condition, T>> tryDecodeWrapperFormat(DynamicOps<T> ops, T input) {
		// Check if input has "static" or "dynamic" keys
		var staticKeyResult = ops.get(input, "static");
		var dynamicKeyResult = ops.get(input, "dynamic");

		boolean hasStaticKey = staticKeyResult.result().isPresent();
		boolean hasDynamicKey = dynamicKeyResult.result().isPresent();

		if (!hasStaticKey && !hasDynamicKey) {
			return DataResult.error(() -> "Not a wrapper format");
		}

		List<StaticCondition> staticResults = new ArrayList<>();
		List<DynamicCondition> dynamicResults = new ArrayList<>();

		// Parse static conditions
		if (hasStaticKey) {
			T staticList = staticKeyResult.result().get();
			var listResult = Codec.list(JsonElementCodec.INSTANCE).decode(ops, staticList);
			listResult.result().ifPresent(pair -> {
				for (JsonElement predicateJson : pair.getFirst()) {
					parsePredicateJson(predicateJson, staticResults, dynamicResults);
				}
			});
		}

		// Parse dynamic conditions
		if (hasDynamicKey) {
			T dynamicList = dynamicKeyResult.result().get();
			var listResult = Codec.list(JsonElementCodec.INSTANCE).decode(ops, dynamicList);
			listResult.result().ifPresent(pair -> {
				for (JsonElement predicateJson : pair.getFirst()) {
					parsePredicateJson(predicateJson, staticResults, dynamicResults);
				}
			});
		}

		return DataResult.success(Pair.of(new Condition(staticResults, dynamicResults), ops.empty()));
	}

	/**
	 * Parses a single predicate JSON element and adds it to the appropriate list.
	 */
	private void parsePredicateJson(JsonElement predicateJson, List<StaticCondition> staticResults, List<DynamicCondition> dynamicResults) {
		if (!predicateJson.isJsonObject() || predicateJson.getAsJsonObject().get("type") == null) {
			LOGGER.error("Predicate must be a JSON object with a 'type' field: {}", predicateJson);
			return;
		}
		String type = predicateJson.getAsJsonObject().get("type").getAsString();

		LogicalConditionHandler handler = logicalHandlers.get(type);
		if (handler != null) {
			DataResult<Condition> logicalResult = handler.codecFactory().apply(this).parse(JsonOps.INSTANCE, predicateJson);
			logicalResult.result().ifPresent(condition -> {
				Object result = handler.fromFactory().apply(condition);
				if (result instanceof StaticCondition sc) {
					staticResults.add(sc);
				} else if (result instanceof DynamicCondition dc) {
					dynamicResults.add(dc);
				}
			});
			logicalResult.error().ifPresent(error -> LOGGER.warn("Failed to parse logical condition: {}", error.message()));
		} else {
			// Try static first, then dynamic
			DataResult<StaticCondition> staticResult = staticDispatcher.parse(JsonOps.INSTANCE, predicateJson);
			DataResult<DynamicCondition> dynamicResult = dynamicDispatcher.parse(JsonOps.INSTANCE, predicateJson);

			boolean parsedAny = false;
			if (staticResult.result().isPresent()) {
				staticResults.add(staticResult.result().get());
				parsedAny = true;
			}
			if (dynamicResult.result().isPresent()) {
				dynamicResults.add(dynamicResult.result().get());
				parsedAny = true;
			}

			// CRITICAL: Log error if neither static nor dynamic codec recognized the type
			// This prevents silent failures where conditions are silently dropped
			if (!parsedAny) {
				LOGGER.error("CONDITION PARSE FAILURE: Unknown condition type '{}'. " +
						"This condition will be IGNORED, which may cause attribute leaks! " +
						"Ensure the condition codec is registered. JSON: {}",
						type, predicateJson);
			}
		}
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
		return tryEncodeLogical(condition, ops, prefix)
				.orElseGet(() -> encodeNonLogical(condition, ops, prefix));
	}

	private <T> java.util.Optional<DataResult<T>> tryEncodeLogical(Object condition, DynamicOps<T> ops, T prefix) {
		Condition toEncode = null;
		OpenIdentifier typeId = null;
		String handlerKey = null;

		if (condition instanceof AndCondition.AndStatic logical) {
			toEncode = logical.toCondition();
			typeId = logical.type();
			handlerKey = AndCondition.TYPE.toString();
		} else if (condition instanceof AndCondition.AndDynamic logical) {
			toEncode = logical.toCondition();
			typeId = logical.type();
			handlerKey = AndCondition.TYPE.toString();
		} else if (condition instanceof OrCondition.OrStatic logical) {
			toEncode = logical.toCondition();
			typeId = logical.type();
			handlerKey = OrCondition.TYPE.toString();
		} else if (condition instanceof OrCondition.OrDynamic logical) {
			toEncode = logical.toCondition();
			typeId = logical.type();
			handlerKey = OrCondition.TYPE.toString();
		} else if (condition instanceof NotCondition.NotStatic logical) {
			toEncode = logical.toCondition();
			typeId = logical.type();
			handlerKey = NotCondition.TYPE.toString();
		} else if (condition instanceof NotCondition.NotDynamic logical) {
			toEncode = logical.toCondition();
			typeId = logical.type();
			handlerKey = NotCondition.TYPE.toString();
		}

		if (toEncode == null || handlerKey == null) {
			return java.util.Optional.empty();
		}

		LogicalConditionHandler handler = logicalHandlers.get(handlerKey);
		Codec<Condition> codec = handler.codecFactory().apply(this);
		final OpenIdentifier finalTypeId = typeId;
		DataResult<T> result = codec.encode(toEncode, ops, prefix)
				.flatMap(content -> ops.mergeToMap(content, ops.createString("type"), ops.createString(finalTypeId.toString())));
		return java.util.Optional.of(result);
	}

	private <T> DataResult<T> encodeNonLogical(Object condition, DynamicOps<T> ops, T prefix) {
		if (condition instanceof StaticCondition sc) {
			return staticDispatcher.encode(sc, ops, prefix);
		} else if (condition instanceof DynamicCondition dc) {
			return dynamicDispatcher.encode(dc, ops, prefix);
		}
		return DataResult.error(() -> "Unknown condition type for encoding: " + condition.getClass().getName());
	}
}
