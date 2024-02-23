package com.sigmundgranaas.forgero.minecraft.common.predicate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import net.minecraft.entity.Entity;

public class EntityCodec extends MapCodec<GroupEntry<KeyPair<Predicate<Entity>>>> {
	private final String key;
	private final Map<String, Codec<KeyPair<Predicate<Entity>>>> codecs;

	public EntityCodec(String key, Map<String, Codec<KeyPair<Predicate<Entity>>>> codec) {
		this.key = key;
		this.codecs = codec;
	}

	@Override
	public <T> Stream<T> keys(DynamicOps<T> ops) {
		return codecs.keySet().stream().map(ops::createString);
	}

	@Override
	public <T> RecordBuilder<T> encode(GroupEntry<KeyPair<Predicate<Entity>>> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
		var temp = prefix;
		for (var element : input.entries()) {
			var codec = codecs.get(element.key());
			var res = codec.encodeStart(ops, element).result();
			if (res.isPresent()) {
				temp = temp.add(ops.createString(element.key()), res.get());
			}
		}
		temp = prefix.add("type", ops.createString(key));
		return temp;
	}

	@Override
	public <T> DataResult<GroupEntry<KeyPair<Predicate<Entity>>>> decode(DynamicOps<T> ops, MapLike<T> input) {
		List<KeyPair<Predicate<Entity>>> elements = input.entries()
				.map(entry -> {
					var key = ops.getStringValue(entry.getFirst()).result().orElse("");
					return Optional.ofNullable(codecs.get(key))
							.map(codec -> codec.parse(ops, entry.getSecond()))
							.flatMap(DataResult::result);
				})
				.flatMap(Optional::stream)
				.collect(Collectors.toList());
		return DataResult.success(new GroupEntry<>(key, elements));
	}
}
