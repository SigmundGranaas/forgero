package com.sigmundgranaas.forgero.item.forgerotool.identifier;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public interface ForgeroIdentifierFactory {

    ForgeroIdentifier createForgeroIdentifier(Identifier identifier);

    ForgeroIdentifier createForgeroIdentifier(String identifier);

    ForgeroIdentifier createForgeroIdentifier(Item item);
}
