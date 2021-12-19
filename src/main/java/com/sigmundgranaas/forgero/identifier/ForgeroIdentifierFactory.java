package com.sigmundgranaas.forgero.identifier;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public interface ForgeroIdentifierFactory {
    ForgeroIdentifierFactory INSTANCE = ForgeroIdentifierFactoryImpl.getInstance();

    ForgeroIdentifier createForgeroIdentifier(Identifier identifier);

    ForgeroIdentifier createForgeroIdentifier(String identifier);

    ForgeroIdentifier createForgeroIdentifier(Item item);
}
