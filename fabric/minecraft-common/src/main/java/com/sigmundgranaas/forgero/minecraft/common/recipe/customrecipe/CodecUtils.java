package com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe;

import com.mojang.serialization.*;

import java.util.function.Function;
import java.util.stream.Stream;

public class CodecUtils {
	public static <T, R> Codec<R> extendCodec(Codec<T> baseCodec, Function<T, R> toExtendedType, Function<R, T> toBaseType) {
		if (baseCodec instanceof MapCodec.MapCodecCodec) {
			MapCodec<T> baseMapCodec = ((MapCodec.MapCodecCodec<T>) baseCodec).codec();

			MapCodec<R> extendedMapCodec = new MapCodec<>() {
				@Override
				public <T2> Stream<T2> keys(DynamicOps<T2> ops) {
					return baseMapCodec.keys(ops);
				}

				@Override
				public <T2> RecordBuilder<T2> encode(R extendedType, DynamicOps<T2> ops, RecordBuilder<T2> builder) {
					T baseType = toBaseType.apply(extendedType);
					return baseMapCodec.encode(baseType, ops, builder);
				}

				@Override
				public <T2> DataResult<R> decode(DynamicOps<T2> ops, MapLike<T2> map) {
					return baseMapCodec.decode(ops, map).map(toExtendedType);
				}
			};

			return new MapCodec.MapCodecCodec<>(extendedMapCodec);
		} else {
			return baseCodec.xmap(toExtendedType, toBaseType);
		}
	}
}
