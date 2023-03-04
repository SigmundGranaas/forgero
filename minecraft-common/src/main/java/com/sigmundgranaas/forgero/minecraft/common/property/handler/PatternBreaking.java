package com.sigmundgranaas.forgero.minecraft.common.property.handler;

import com.sigmundgranaas.forgero.core.property.ActivePropertyType;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.property.active.ActiveProperty;
import com.sigmundgranaas.forgero.core.property.active.BreakingDirection;
import com.sigmundgranaas.forgero.core.resource.data.PropertyPojo;

import net.minecraft.block.BlockState;

import java.util.function.Function;
import java.util.function.Predicate;

public class PatternBreaking implements ActiveProperty {
	public static Predicate<PropertyPojo.Active> predicate = (active) -> active.type == ActivePropertyType.BLOCK_BREAKING_PATTERN;
	public static Function<PropertyPojo.Active, ActiveProperty> factory = (active) -> new PatternBreaking(active.pattern, active.direction == null ? BreakingDirection.ANY : active.direction);

	private final String[] pattern;
	private final BreakingDirection direction;

	public PatternBreaking(String[] pattern, BreakingDirection direction) {
		this.pattern = pattern;
		this.direction = direction;
	}

	@Override
	public String type() {
		return "PATTERN_MINING";
	}

	@Override
	public float applyAttribute(Target target, float currentAttribute) {
		return currentAttribute;
	}

	@Override
	public boolean applyCondition(Target target) {
		return true;
	}

	@Override
	public ActivePropertyType getActiveType() {
		return ActivePropertyType.BLOCK_BREAKING_PATTERN;
	}

	public boolean checkBlock(BlockState state) {
		return true;
	}

	public String[] getPattern() {
		return pattern;
	}

	public BreakingDirection getDirection() {
		return direction;
	}
}
