package com.sigmundgranaas.forgero.core.resource;

import com.sigmundgranaas.forgero.core.data.ForgeroDataResource;

import java.nio.file.Path;
import java.util.Optional;

public interface PojoFileLoader<T extends ForgeroDataResource> {
    Optional<T> loadFile(Path path);
}
