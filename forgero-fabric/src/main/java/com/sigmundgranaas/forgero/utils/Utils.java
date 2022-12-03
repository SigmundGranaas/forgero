package com.sigmundgranaas.forgero.utils;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.ForgeroInitializer;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {

    public static String createModelJson(String path, String parent) {
        String[] segments = path.split("/");
        path = segments[segments.length - 1];
        return "{\n" +
                "  \"parent\": \"" + parent + "\",\n" +
                "  \"textures\": {\n" +
                "    \"layer0\": \"" + ForgeroInitializer.MOD_NAMESPACE + ":item/" + path + "\"\n" +
                "  }\n" +
                "}";
    }

    @SafeVarargs
    public static <T> ImmutableList<T> concatImmutable(Collection<T>... lists) {
        return Stream.of(lists).flatMap(Collection::stream).collect(ImmutableList.toImmutableList());
    }

    @SafeVarargs
    public static <T> List<T> concat(Collection<T>... lists) {
        return Stream.of(lists).flatMap(Collection::stream).collect(Collectors.toList());
    }


}
