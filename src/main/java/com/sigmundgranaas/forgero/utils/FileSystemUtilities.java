package com.sigmundgranaas.forgero.utils;

import com.sigmundgranaas.forgero.Forgero;

import java.io.File;

public class FileSystemUtilities {
    /**
     * Method for finding the resource folder path.
     * Will return "" if it is unable to find resource folder
     *
     * @return the path of the resource folder
     */
    public static String getResourcePath() {
        ClassLoader classLoader = ResourceUtils.class.getClassLoader();
        try {
            return classLoader.getResource("").getPath();
        } catch (NullPointerException e) {
            Forgero.LOGGER.warn("Unable to find resource directory");
            Forgero.LOGGER.warn(e);
            return "";
        }
    }

    /**
     * Attempts to find the path to the current resource directory
     *
     * @param resourcePath
     * @return
     */
    public static boolean resourceExists(String resourcePath) {
        File targetFile = new File(resourcePath);
        return targetFile.exists();
    }
}
