package com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.selector;

import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import lombok.Getter;
import lombok.experimental.Accessors;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;

import java.util.Set;



/**
 * BlockSelector that only selects the root block.
 * <p>Example JSON configuration:
 * This configuration will instant mine all block that are in the tag forgero:vein_mining_ores
 * <pre>
 * {
 * 	 "type": "minecraft:block_breaking",
 * 	 "selector": {
 * 	 "type": "forgero:single"
 *          },
 * 	 "speed": "forgero:instant",
 * 	 "predicate": {
 * 	 "type": "minecraft:block",
 * 	 "tag": "forgero:vein_mining_ores"
 *     },
 * 	 "title": "feature.forgero.vein_mining.title",
 * 	 "description": "feature.forgero.ore_vein_mining.description"
 *  }
 * </pre>
 * </p>
 */
@Getter
@Accessors(fluent = true)
public class SingleSelector implements BlockSelector {
	public static final SingleSelector DEFAULT = new SingleSelector();
	public static final String TYPE = "forgero:single";
	public static final JsonBuilder<SingleSelector> BUILDER = HandlerBuilder.fromObject(SingleSelector.class, (json) -> new SingleSelector());


	/**
	 * Selects a single block
	 *
	 * @param rootPos origin position of the selection.
	 * @return A set containing the root position.
	 */
	@NotNull
	@Override
	public Set<BlockPos> select(BlockPos rootPos, Entity source) {
		return Set.of(rootPos);
	}
}
