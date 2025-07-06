package com.sigmundgranaas.forgero.smithing.util;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TextureCenteringUtil {
    private static final int GRID_SIZE = 16;
    private static final int GRID_CENTER = 8;
    private static final int ALPHA_THRESHOLD = 1;
    private static final Map<String, int[]> offsetCache = new ConcurrentHashMap<>();

    /**
     * Computes the offset needed to center the non-transparent pixels of a texture in a 16x16 grid.
     * @param image The texture as a BufferedImage.
     * @return An int array [dx, dy] representing the offset from the center.
     */
    public static int[] getTextureCenterOffset(BufferedImage image) {
        int width = Math.min(image.getWidth(), GRID_SIZE);
        int height = Math.min(image.getHeight(), GRID_SIZE);
        int sumX = 0, sumY = 0, count = 0;
        int minX = GRID_SIZE, maxX = -1;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = image.getRGB(x, y);
                int alpha = (pixel >> 24) & 0xff;
                if (alpha >= ALPHA_THRESHOLD) {
                    sumX += x;
                    sumY += y;
                    count++;
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                }
            }
        }
        if (count > 0) {
            int centerX = sumX / count;
            // Outlier-aggressive: bias toward the bounding box edge if far from center
            int boxCenterX = minX + (maxX - minX) / 2;
            float outlierAggression = 0f; // 0 = pure center of mass, 1 = pure bounding box center
            int mixedCenterX = Math.round(centerX * (1 - outlierAggression) + boxCenterX * outlierAggression);
            mixedCenterX = Math.max(0, Math.min(GRID_SIZE - 1, mixedCenterX));
            int dx = GRID_CENTER - mixedCenterX;
            int dy = 0; // y centering is ignored for rendering
            return new int[] {dx, dy};
        }
        return new int[] {0, 0};
    }

    /**
     * Gets the cached offset for a texture, or computes and caches it if not present.
     * @param key A unique key for the texture (e.g., its identifier path).
     * @param image The texture as a BufferedImage.
     * @return The offset [dx, dy].
     */
    public static int[] getCachedOffset(String key, BufferedImage image) {
        return offsetCache.computeIfAbsent(key, k -> getTextureCenterOffset(image));
    }
}
