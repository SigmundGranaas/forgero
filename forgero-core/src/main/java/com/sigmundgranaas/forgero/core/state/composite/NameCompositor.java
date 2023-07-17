package com.sigmundgranaas.forgero.core.state.composite;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;
import static com.sigmundgranaas.forgero.core.type.Type.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;

public class NameCompositor {
	public String compositeName(List<State> ingredients) {
		return ingredients.stream()
				.sorted(Comparator.comparingInt(this::sorter))
				.map(this::mapper)
				.flatMap(Optional::stream)
				.collect(Collectors.joining(ELEMENT_SEPARATOR));
	}

	private int sorter(State ingredient) {
		if (ingredient.test(MATERIAL, MatchContext.of())) {
			return 1;
		} else if (ingredient.test(SCHEMATIC, MatchContext.of())) {
			return 2;
		}
		return 0;
	}

	private Optional<String> mapper(State ingredient) {
		if (ingredient.test(TOOL_PART_HEAD, MatchContext.of()) || ingredient.test(ARROW_HEAD, MatchContext.of())) {
			return Optional.of(ingredient.name().replace("_head", ""));
		}
		if (ingredient.test(BOW_LIMB)) {
			return Optional.of(ingredient.name().replace("_limb", ""));
		}
		if (ingredient.test(SWORD_BLADE, MatchContext.of())) {
			return Optional.of(ingredient.name().replace("_blade", ""));
		} else if (ingredient.test(HANDLE, MatchContext.of())) {
			return Optional.empty();
		} else if (ingredient.test(SCHEMATIC, MatchContext.of())) {
			var elements = ingredient.name().split(ELEMENT_SEPARATOR);
			if (elements.length == 2) {
				return Optional.of(elements[0]);
			}
		}
		return Optional.of(ingredient.name());
	}
}
