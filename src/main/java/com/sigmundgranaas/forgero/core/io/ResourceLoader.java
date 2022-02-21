package com.sigmundgranaas.forgero.core.io;

import java.io.IOException;
import java.io.InputStream;

public interface ResourceLoader {
    InputStream loadResource(String resource) throws IOException;
}
