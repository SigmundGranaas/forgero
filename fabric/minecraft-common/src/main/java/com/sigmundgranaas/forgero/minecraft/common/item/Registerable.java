package com.sigmundgranaas.forgero.minecraft.common.item;

@FunctionalInterface
public interface Registerable<T>{
	  void register(GenericRegistry<T> registry);
}
