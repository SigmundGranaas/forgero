package com.sigmundgranaas.forgero.predicate.flag;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.sigmundgranaas.forgero.predicate.codecs.KeyPair;
import com.sigmundgranaas.forgero.predicate.codecs.KeyProvider;
import com.sigmundgranaas.forgero.predicate.codecs.SpecificationRegistry;

public class FlagGroupCodec<R> implements Codec<KeyPair<FlagGroupPredicate<R>>> {
	private final Codec<FlagGroupPredicate<R>> codec;
	private final SpecificationRegistry<Predicate<R>> registry;
	private final String key;

	public FlagGroupCodec(SpecificationRegistry<Predicate<R>> registry, String key) {
		this.codec = createFlagPredicateCodec(registry, key);
		this.registry = registry;
		this.key = key;
	}

	public Codec<KeyPair<Predicate<R>>> asPredicateCodec() {
		return (Codec) this;
	}

	private static <R> List<FlagPredicateInstance<R>> mapToFlags(Map<String, Boolean> map, KeyProvider<KeyPair<Predicate<R>>> predicateProvider) {
		return map.entrySet().stream()
				.map(entry -> FlagGroupCodec.createFlag(entry.getKey(), entry.getValue(), predicateProvider))
				.flatMap(Optional::stream)
				.collect(Collectors.toList());
	}

	private static <R> Optional<FlagPredicateInstance<R>> createFlag(String key, boolean flag, KeyProvider<KeyPair<Predicate<R>>> predicateProvider) {
		return predicateProvider.apply(key).map(entry -> FlagPredicateInstance.of(entry, flag));
	}

	private static <R> Map<String, Boolean> flagsToMap(List<FlagPredicateInstance<R>> flags) {
		return flags.stream().collect(Collectors.toMap(FlagPredicateInstance::key, FlagPredicateInstance::flag));
	}

	public Codec<FlagGroupPredicate<R>> createFlagPredicateCodec(KeyProvider<KeyPair<Predicate<R>>> predicateProvider, String key) {
		MapCodec<FlagGroupPredicate<R>> localCodec = new MapCodec<>() {
			@Override
			public <T> RecordBuilder<T> encode(FlagGroupPredicate<R> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
				return null;
			}

			@Override
			public <T> DataResult<FlagGroupPredicate<R>> decode(DynamicOps<T> ops, MapLike<T> input) {
				List<FlagPredicateInstance<R>> entries = input.entries()
						.map(entry -> {
							String key = ops.getStringValue(entry.getFirst()).result().orElse("");
							Optional<Boolean> value = ops.getBooleanValue(entry.getSecond()).result();
							return value.flatMap(flag -> createFlag(key, flag, predicateProvider));
						})
						.flatMap(Optional::stream)
						.collect(Collectors.toList());
				return DataResult.success(new FlagGroupPredicate<>(entries));

			}

			@Override
			public <T> Stream<T> keys(DynamicOps<T> ops) {
				return registry.keySet().stream().map(ops::createString);
			}
		};
		return new MapCodec.MapCodecCodec<>(localCodec);
	}

	@Override
	public <T> DataResult<T> encode(KeyPair<FlagGroupPredicate<R>> input, DynamicOps<T> ops, T prefix) {
		return codec.encode(input.value(), ops, prefix);
	}

	@Override
	public <T> DataResult<Pair<KeyPair<FlagGroupPredicate<R>>, T>> decode(DynamicOps<T> ops, T input) {
		return codec.decode(ops, input).map(pair -> pair.mapFirst(res -> KeyPair.pair(key, res)));
	}
}
