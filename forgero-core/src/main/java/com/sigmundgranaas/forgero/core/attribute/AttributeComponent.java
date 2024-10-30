package com.sigmundgranaas.forgero.core.attribute;

import com.sigmundgranaas.forgero.core.attribute.computation.Addition;
import com.sigmundgranaas.forgero.core.attribute.computation.Computation;

public interface AttributeComponent {
	String type();
	Computation computation();

	static AttributeComponent add(String type, long val){
		return new BasicAttributeComponent(new Addition(val), type);
	}
}
