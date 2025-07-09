package com.sigmundgranaas.forgero.data;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;

public class Utils {
	public static OpenIdentifier id(String id) {
		return CodecConstants.IDENTIFIER_FACTORY.of(id);
	}
}
