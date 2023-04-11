package com.sigmundgranaas.drp.impl.resource;

import java.io.InputStream;

@FunctionalInterface
public interface InputStreamConvertible {
	InputStream asInputStream();
}
