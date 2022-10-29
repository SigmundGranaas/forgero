package com.sigmundgranaas.forgero.property.attribute;

import com.sigmundgranaas.forgero.property.PropertyContainer;

public abstract class AbstractAttribute {
    protected PropertyContainer container;

    public AbstractAttribute(PropertyContainer container) {
        this.container = container;
    }

    public abstract AttributeResult apply();
}
