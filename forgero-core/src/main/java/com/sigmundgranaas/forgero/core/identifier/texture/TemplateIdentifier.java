package com.sigmundgranaas.forgero.core.identifier.texture;

public record TemplateIdentifier(String template) implements TextureIdentifier {
	@Override
	public String getFileNameWithExtension() {
		return template;
	}

	@Override
	public String getFileNameWithoutExtension() {
		return template.replace(".png", "");
	}

	@Override
	public String getIdentifier() {
		return getFileNameWithExtension();
	}
}
