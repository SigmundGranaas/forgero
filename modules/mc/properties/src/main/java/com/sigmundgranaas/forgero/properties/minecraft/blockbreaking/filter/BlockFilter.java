package com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.filter;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.common.filter.TypedFilter;
import com.sigmundgranaas.forgero.utility.codec.DispatchCodecUtils;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

/**
 * Filters block positions based on various criteria.
 * <p>
 * Used in block breaking operations (e.g., vein mining) to determine which
 * blocks should be included in a multi-block operation.
 * <p>
 * The context is a {@link BlockFilterContext} containing the entity and root position,
 * and the target is the {@link BlockPos} being tested.
 *
 * @see com.sigmundgranaas.forgero.common.filter.Filter
 */
public interface BlockFilter extends TypedFilter<BlockPos, BlockFilterContext> {

	/**
	 * Tests whether the given block position passes this filter.
	 *
	 * @param entity     The entity performing the action
	 * @param currentPos The block position being tested
	 * @param root       The root/origin block position
	 * @return true if the block passes the filter
	 */
	boolean filter(Entity entity, BlockPos currentPos, BlockPos root);

	/**
	 * Implementation of the base Filter interface.
	 * Delegates to the legacy {@link #filter} method.
	 */
	@Override
	default boolean test(BlockFilterContext context, BlockPos target) {
		return filter(context.entity(), target, context.root());
	}

	static Codec<? extends BlockFilter> getCodec(String type) {
		return switch (type) {
			case CanMineFilter.TYPE -> CanMineFilter.CODEC;
			case FilterWrapper.TYPE -> FilterWrapper.CODEC;
			case IsBlockFilter.TYPE -> IsBlockFilter.CODEC;
			case SameBlockFilter.TYPE -> SameBlockFilter.CODEC;
			case SimilarBlockFilter.TYPE -> SimilarBlockFilter.CODEC;
			case BlockTagFilter.TYPE -> BlockTagFilter.CODEC;
			default -> throw new IllegalArgumentException("Unknown BlockFilter type: " + type);
		};
	}

	Codec<BlockFilter> CODEC = DispatchCodecUtils.create(
			BlockFilter::getCodec,
			BlockFilter::type
	);
}
