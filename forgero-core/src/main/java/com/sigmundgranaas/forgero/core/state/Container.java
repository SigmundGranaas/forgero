package com.sigmundgranaas.forgero.core.state;

public interface Container<T extends State> {
    Default<T> getDefault();
}
