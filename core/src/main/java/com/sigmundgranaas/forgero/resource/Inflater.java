package com.sigmundgranaas.forgero.resource;

import java.util.List;

@FunctionalInterface
public interface Inflater<T> {
    List<T> inflate(T res);
}