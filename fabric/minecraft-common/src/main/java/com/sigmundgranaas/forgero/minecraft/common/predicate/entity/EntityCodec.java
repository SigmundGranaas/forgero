package com.sigmundgranaas.forgero.minecraft.common.predicate.entity;

import com.mojang.serialization.*;
import com.sigmundgranaas.forgero.minecraft.common.predicate.GroupEntry;
import com.sigmundgranaas.forgero.minecraft.common.predicate.KeyPair;
import com.sigmundgranaas.forgero.minecraft.common.predicate.Predicate;
import com.sigmundgranaas.forgero.minecraft.common.predicate.SpecificationRegistry;
import net.minecraft.entity.Entity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityCodec extends MapCodec<GroupEntry<KeyPair<Predicate<Entity>>>> {
	private final String key;
	private final SpecificationRegistry<Codec<KeyPair<Predicate<Entity>>>> codecs;

	public EntityCodec(String key, SpecificationRegistry<Codec<KeyPair<Predicate<Entity>>>> codec) {
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
	public <T> DataResult<GroupEntry<KeyPair<Predicate<Entity>>>> decode(DynamicOps<T> ops, MapLike<T> input) {
		List<KeyPair<Predicate<Entity>>> elements = input.entries()
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
