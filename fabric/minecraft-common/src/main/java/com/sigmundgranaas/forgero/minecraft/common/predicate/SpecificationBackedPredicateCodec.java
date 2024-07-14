package com.sigmundgranaas.forgero.minecraft.common.predicate;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

public class SpecificationBackedPredicateCodec<T> extends MapCodec<GroupEntry<KeyPair<Predicate<T>>>> {
	private final String key;
	private final SpecificationRegistry<Codec<KeyPair<Predicate<T>>>> codecs;

	public SpecificationBackedPredicateCodec(String key, SpecificationRegistry<Codec<KeyPair<Predicate<T>>>> codec) {
		this.key = key;
		this.codecs = codec;
	}

	@Override
	public <R> Stream<R> keys(DynamicOps<R> ops) {
		return codecs.keySet().stream().map(ops::createString);
	}

	@Override
	public <R> RecordBuilder<R> encode(GroupEntry<KeyPair<Predicate<T>>> input, DynamicOps<R> ops, RecordBuilder<R> prefix) {
		var temp = prefix;
		for (var element : input.entries()) {
			var codec = codecs.apply(element.key()).map(KeyPair::value);
			if (codec.isPresent()) {
				var res = codec.get().encodeStart(ops, element).result();
				if (res.isPresent()) {
					temp = temp.add(ops.createString(element.key()), res.get());
				}
			}
		}
		temp = prefix.add("type", ops.createString(key));
		return temp;
	}

	@Override
	public <R> DataResult<GroupEntry<KeyPair<Predicate<T>>>> decode(DynamicOps<R> ops, MapLike<R> input) {
		String type = ops.getStringValue(input.get("type")).result().orElse("");
		if (!type.equals(this.key)) {
			return DataResult.error(() -> "Incorrect type");
		}

		List<KeyPair<Predicate<T>>> elements = input.entries()
				.map(entry -> {
					var key = ops.getStringValue(entry.getFirst()).result().orElse("");
					return codecs.apply(key)
							.map(KeyPair::value)
							.map(codec -> codec.parse(ops, entry.getSecond()))
							.flatMap(DataResult::result);
				})
				.flatMap(Optional::stream)
				.collect(Collectors.toList());
		return DataResult.success(new GroupEntry<>(key, elements));
	}
}

