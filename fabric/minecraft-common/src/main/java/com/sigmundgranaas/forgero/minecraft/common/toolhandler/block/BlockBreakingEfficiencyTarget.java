package com.sigmundgranaas.forgero.minecraft.common.toolhandler.block;

import java.util.Collections;
import java.util.Set;

import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.property.TargetTypes;

import net.minecraft.block.BlockState;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public record BlockBreakingEfficiencyTarget(BlockState state) implements Target {
	@Override
	public Set<TargetTypes> getTypes() {
		return Set.of(TargetTypes.BLOCK);
	}

	@Override
	public Set<String> getTags() {
		return Collections.emptySet();
	}

	@Override
	public boolean isApplicable(Set<String> tag, TargetTypes type) {
		if (type == TargetTypes.BLOCK) {
			for (String stringTag : tag) {
				if (state.isIn(TagKey.of(Registry.BLOCK_KEY, new Identifier(stringTag)))) {
					return true;
				} else if (Registry.BLOCK.getId(state.getBlock()).toString().equals(stringTag)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getTags().hashCode()
				+ getTypes().hashCode()
				+ state.toString().hashCode();
	}
}
