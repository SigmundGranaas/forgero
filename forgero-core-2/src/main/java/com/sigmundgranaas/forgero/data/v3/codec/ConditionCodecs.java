package com.sigmundgranaas.forgero.data.v3.codec;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.data.v3.dto.condition.*; // Import all new DTOs
// Removed unused import: import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;

import java.util.List;

public class ConditionCodecs {

	/**
	 * A custom codec that achieves polymorphic decoding for PredicateData.
	 * It reads the "type" field, looks up the corresponding codec in the PredicateCodecRegistry,
	 * and then uses that specific codec to parse the full JSON object.
	 */
	private static final Codec<PredicateData> PREDICATE_CODEC = new Codec<>() {
		@Override
		public <T> DataResult<Pair<PredicateData, T>> decode(DynamicOps<T> ops, T input) {
			return ops.get(input, "type")
					.flatMap(ops::getStringValue)
					.flatMap(PredicateCodecRegistry::get) // Registry takes String, converts internally
					.flatMap(codec -> codec.decode(ops, input)
							// Widen the generic type from Pair<? extends PredicateData, T> to Pair<PredicateData, T>
							// This is safe because any instance of a subtype of PredicateData is also an instance of PredicateData.
							.mapError(err -> "Failed to decode PredicateData: " + err)
							.map(pair -> Pair.of(pair.getFirst(), pair.getSecond()))
					);
		}

		@Override
		@SuppressWarnings({"unchecked", "rawtypes"})
		public <T> DataResult<T> encode(PredicateData input, DynamicOps<T> ops, T prefix) {
			// Get the codec for the input's specific type.
			// We cast to a raw Codec to bypass compile-time generic checks.
			// This is safe because we trust the registry to provide the correct codec
			// for the given input's type, which will accept the input object at runtime.
			return PredicateCodecRegistry.get(input.type().toString()) // Registry takes String
					.flatMap(codec -> ((Codec) codec).encode(input, ops, prefix))
					.mapError(err -> "Failed to encode PredicateData: " + err); // Added mapError for better context
		}
	};


	/**
	 * Codec for ConditionData. It can parse either a single predicate object or an array of them.
	 */
	public static final Codec<ConditionData> CONDITION_DATA_CODEC;

	static {
		// Register core predicate types.
		// These codecs are private helpers within this class, so they can be defined here.

		// TagMatchPredicateData (for self_has_tag, root_has_tag)
		var tagMatchCodec = RecordCodecBuilder.<TagMatchPredicateData>create(instance ->
				instance.group(
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(TagMatchPredicateData::type), // Changed to OpenIdentifierCodec
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("tag").forGetter(TagMatchPredicateData::tag) // Changed to OpenIdentifierCodec
				).apply(instance, TagMatchPredicateData::new));

		PredicateCodecRegistry.register("forgero:self_has_tag", tagMatchCodec);
		PredicateCodecRegistry.register("forgero:root_has_tag", tagMatchCodec);

		// InSlotTypePredicateData
		var inSlotTypeCodec = RecordCodecBuilder.<InSlotTypePredicateData>create(instance ->
				instance.group(
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(InSlotTypePredicateData::type), // Changed to OpenIdentifierCodec
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("slot_type").forGetter(InSlotTypePredicateData::slotType) // Changed to OpenIdentifierCodec
				).apply(instance, InSlotTypePredicateData::new));

		PredicateCodecRegistry.register("forgero:in_slot_type", inSlotTypeCodec);

		// SlotContainsPredicateData
		var slotContainsCodec = RecordCodecBuilder.<SlotContainsPredicateData>create(instance ->
				instance.group(
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(SlotContainsPredicateData::type), // Changed to OpenIdentifierCodec
						Codec.STRING.fieldOf("slot").forGetter(SlotContainsPredicateData::slot), // Remains String
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("tag").forGetter(SlotContainsPredicateData::tag) // Changed to OpenIdentifierCodec
				).apply(instance, SlotContainsPredicateData::new));

		PredicateCodecRegistry.register("forgero:slot_contains", slotContainsCodec);

		// AndPredicateData
		// Note: PREDICATE_CODEC is used here recursively for the list of predicates.
		var andPredicateCodec = RecordCodecBuilder.<AndPredicateData>create(instance ->
				instance.group(
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(AndPredicateData::type), // Changed to OpenIdentifierCodec
						Codec.list(PREDICATE_CODEC).fieldOf("predicates").forGetter(AndPredicateData::predicates)
				).apply(instance, AndPredicateData::new));

		PredicateCodecRegistry.register("forgero:and", andPredicateCodec);

		// OrPredicateData
		var orPredicateCodec = RecordCodecBuilder.<OrPredicateData>create(instance ->
				instance.group(
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(OrPredicateData::type), // Changed to OpenIdentifierCodec
						Codec.list(PREDICATE_CODEC).fieldOf("predicates").forGetter(OrPredicateData::predicates)
				).apply(instance, OrPredicateData::new));

		PredicateCodecRegistry.register("forgero:or", orPredicateCodec);

		// NotPredicateData
		var notPredicateCodec = RecordCodecBuilder.<NotPredicateData>create(instance ->
				instance.group(
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(NotPredicateData::type), // Changed to OpenIdentifierCodec
						PREDICATE_CODEC.fieldOf("predicate").forGetter(NotPredicateData::predicate)
				).apply(instance, NotPredicateData::new));

		PredicateCodecRegistry.register("forgero:not", notPredicateCodec);


		// Codec for a list of predicates, for the JSON array case.
		final Codec<List<PredicateData>> PREDICATE_LIST_CODEC = Codec.list(PREDICATE_CODEC);
		// Codec that handles a single predicate object and wraps/unwraps it from a list.
		final Codec<List<PredicateData>> SINGLE_PREDICATE_AS_LIST_CODEC = PREDICATE_CODEC.xmap(List::of, list -> list.get(0));

		// A flexible codec that tries to parse an array first, then falls back to a single object.
		final Codec<List<PredicateData>> FLEXIBLE_PREDICATE_CODEC = Codec.either(PREDICATE_LIST_CODEC, SINGLE_PREDICATE_AS_LIST_CODEC)
				.xmap(
						either -> either.map(list -> list, singleAsList -> singleAsList),
						list -> (list.size() == 1) ? Either.right(list) : Either.left(list)
				);

		CONDITION_DATA_CODEC = FLEXIBLE_PREDICATE_CODEC.xmap(ConditionData::new, ConditionData::predicates);
	}
}
