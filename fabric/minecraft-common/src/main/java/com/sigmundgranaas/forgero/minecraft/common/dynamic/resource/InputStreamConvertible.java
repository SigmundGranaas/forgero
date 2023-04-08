package com.sigmundgranaas.forgero.minecraft.common.dynamic.resource;

import java.io.InputStream;

@FunctionalInterface
public interface InputStreamConvertible {
	InputStream asInputStream();
}
