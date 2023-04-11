package com.sigmundgranaas.drp.impl.resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public interface JsonData extends InputStreamConvertible {
	JsonObject asJson();

	default InputStream asInputStream() {
		return new ByteArrayInputStream(new Gson().toJson(asJson()).getBytes());
	}
}
