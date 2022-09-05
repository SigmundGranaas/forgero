package com.sigmundgranaas.forgerocore.state;

public interface Container<T extends State> {
    Default<T> getDefault();
}
