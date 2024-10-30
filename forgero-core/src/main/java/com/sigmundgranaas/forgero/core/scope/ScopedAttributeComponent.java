package com.sigmundgranaas.forgero.core.scope;

import com.sigmundgranaas.forgero.core.attribute.AttributeComponent;
import com.sigmundgranaas.forgero.core.attribute.computation.Computation;

public record ScopedAttributeComponent(AttributeComponent attributeComponent, ScopeEvaluator evaluator) implements AttributeComponent {

	@Override
	public String type() {
		return attributeComponent.type();
	}

	@Override
	public Computation computation() {
		return attributeComponent.computation();
	}

	ScopeEvaluator.EvaluationResult evaluate(Scope scope){
		return evaluator().evaluate(scope);
	}

	boolean matches(Scope scope){
		return evaluator().matches(scope);
	}
}
