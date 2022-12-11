package com.sigmundgranaas.forgero.resource;

import java.util.Optional;

public interface Validator<T> {
    Optional<T> validate(T res);
}
