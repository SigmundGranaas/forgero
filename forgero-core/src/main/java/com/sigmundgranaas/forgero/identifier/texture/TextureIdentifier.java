package com.sigmundgranaas.forgero.identifier.texture;

public interface TextureIdentifier {
    String GENERATED_IDENTIFIER = "generated_";

    String getFileNameWithExtension();

    String getFileNameWithoutExtension();

    String getIdentifier();
}
