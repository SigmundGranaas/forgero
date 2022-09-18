package com.sigmundgranaas.forgero.state;

public interface Container<T extends State> {
    Default<T> getDefault();
}
