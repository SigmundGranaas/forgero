package com.sigmundgranaas.forgero.core.state.identity;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sigmundgranaas.forgero.core.state.State;

public class ModificationRuleBuilder {
	private ModificationRuleRegistry registry = ModificationRuleRegistry.staticRegistry();
	private Condition condition;
	private Function<String, String> transformation = Function.identity();

	public static ModificationRuleBuilder builder() {
		return new ModificationRuleBuilder();
	}

	public ModificationRuleBuilder when(Condition condition) {
		this.condition = condition;
		return this;
	}

	public ModificationRuleBuilder replace(String target, String replacement) {
		this.transformation = this.transformation.andThen(name -> name.replace(target, replacement));
		return this;
	}

	public ModificationRuleBuilder ignore() {
		this.transformation = this.transformation.andThen(name -> "");
		return this;
	}

	public ModificationRuleBuilder remove(String substring) {
		return replace(substring, "");
	}

	public ModificationRuleBuilder replaceElement(String containing, String replacement) {
		this.transformation = this.transformation.andThen(name -> {
			String[] elements = name.split(ELEMENT_SEPARATOR);
			return Arrays.stream(elements)
					.map(element -> element.contains(containing) ? replacement : element)
					.collect(Collectors.joining(ELEMENT_SEPARATOR));
		});
		return this;
	}

	public ModificationRule build() {
		return new ModificationRule() {
			@Override
			public boolean applies(State state) {
				return condition.predicate().test(state);
			}

			@Override
			public Function<String, String> transformation() {
				return transformation;
			}
		};
	}

	public ModificationRule register(String id) {
		ModificationRule rule = build();
		registry.registerRule(id, rule);
		return rule;
	}

	public ModificationRuleBuilder takeElement(int i) {
		this.transformation = this.transformation
				.andThen(name -> {
					var elements = name.split(ELEMENT_SEPARATOR);
					if (elements.length >= i) {
						return elements[0];
					} else {
						return name;
					}
				});

		return this;
	}
}
