package com.sigmundgranaas.forgero.core.resource;

import java.io.InputStream;
import java.util.Collection;

public interface InputStreamProvider {
    Collection<InputStream> getStreams(ForgeroResourceType type);
}
