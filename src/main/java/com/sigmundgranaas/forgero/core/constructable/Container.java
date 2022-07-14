package com.sigmundgranaas.forgero.core.constructable;

/**
 * A container which is used to represent many similar variations of an element
 *
 * Example: A pickaxe container can be used to represent many kinds of pickaxes. Containers are unique.
 *
 * @param <T> The type this container represents
 */
public interface Container<T> extends Unique {
    T getDefault();

    default T getDynamic(Convertable<T> converter){
       return converter.convert().orElse(getDefault());
    }
}
