package com.sigmundgranaas.forgero.core.identifier;

import net.minecraft.util.Identifier;

public interface ForgeroIdentifierFactory {
    ForgeroIdentifierFactory INSTANCE = ForgeroIdentifierFactoryImpl.getInstance();

    ForgeroIdentifier createForgeroIdentifier(Identifier identifier);

    ForgeroIdentifier createForgeroIdentifier(String identifier);

    ForgeroMaterialIdentifier createForgeroMaterialIdentifier(String identififer);
}
