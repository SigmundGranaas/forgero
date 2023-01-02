package com.sigmundgranaas.forgero.core.resource;

import java.util.Optional;

public interface Validator<T> {
    Optional<T> validate(T res);
}
