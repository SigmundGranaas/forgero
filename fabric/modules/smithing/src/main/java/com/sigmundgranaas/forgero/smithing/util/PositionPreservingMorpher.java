package com.sigmundgranaas.forgero.smithing.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;

/**
 * Position-preserving palette morpher for Minecraft textures.
 * Pure Java, no external deps. Designed to mimic the provided Python implementation.
 *
 * Usage (standalone):
 *   java PositionPreservingMorpher input.png output.png --steps 5 --save-dir morph_steps
 *
 * In a mod: call morph(...) or morphStep(...) from your code.
 */
public class PositionPreservingMorpher {

	private final Random rng = new Random();

	/* =========================== Image IO =========================== */

	public BufferedImage loadImage(String path) throws IOException {
		BufferedImage img = ImageIO.read(new File(path));
		if (img.getType() != BufferedImage.TYPE_INT_ARGB) {
			BufferedImage argb = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = argb.createGraphics();
			g.drawImage(img, 0, 0, null);
			g.dispose();
			return argb;
		}
		return img;
	}

	public void saveImage(BufferedImage img, String path) throws IOException {
		ImageIO.write(img, "PNG", new File(path));
	}

	/* =========================== Color & Palette =========================== */

	private double brightness(int argb) {
		int a = (argb >>> 24) & 0xFF;
		if (a == 0) return -1; // fully transparent => sort first (like Python guard)
		int r = (argb >>> 16) & 0xFF, g = (argb >>> 8) & 0xFF, b = (argb) & 0xFF;
		return 0.299 * r + 0.587 * g + 0.114 * b;
	}

	public List<Integer> extractSortedPalette(BufferedImage img) {
		Set<Integer> set = new HashSet<>();
		int w = img.getWidth(), h = img.getHeight();
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int c = img.getRGB(x, y);
				if (((c >>> 24) & 0xFF) > 0) set.add(c);
			}
		}
		List<Integer> palette = new ArrayList<>(set);
		palette.sort(Comparator.comparingDouble(this::brightness)); // dark → light
		return palette;
	}

	public int[][] createColorIndexMap(BufferedImage img, List<Integer> palette) {
		int h = img.getHeight(), w = img.getWidth();
		int[][] indexMap = new int[h][w];
		Arrays.stream(indexMap).forEach(row -> Arrays.fill(row, -1));
		// Build lookup map for palette
		Map<Integer, Integer> idxByColor = new HashMap<>();
		for (int i = 0; i < palette.size(); i++) idxByColor.put(palette.get(i), i);

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int c = img.getRGB(x, x < w ? x : w - 1); // safeguard
				c = img.getRGB(x, y);
				Integer idx = idxByColor.get(c);
				if (idx != null) indexMap[y][x] = idx;
			}
		}
		return indexMap;
	}

	/* =========================== Masks & Edges =========================== */

	public boolean[][] alphaMask(BufferedImage img) {
		int h = img.getHeight(), w = img.getWidth();
		boolean[][] mask = new boolean[h][w];
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				mask[y][x] = ((img.getRGB(x, y) >>> 24) & 0xFF) > 0;
			}
		}
		return mask;
	}

	// Binary erosion with 3x3 ones (8-neighborhood). Pixel remains true if all neighbors (including itself) are true.
	public boolean[][] binaryErosion(boolean[][] mask) {
		int h = mask.length, w = mask[0].length;
		boolean[][] out = new boolean[h][w];
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				boolean keep = true;
				for (int dy = -1; dy <= 1 && keep; dy++) {
					for (int dx = -1; dx <= 1 && keep; dx++) {
						int yy = y + dy, xx = x + dx;
						if (yy < 0 || yy >= h || xx < 0 || xx >= w || !mask[yy][xx]) {
							keep = false;
						}
					}
				}
				out[y][x] = keep;
			}
		}
		return out;
	}

	public boolean[][] edgePixels(boolean[][] mask) {
		boolean any = false;
		int h = mask.length, w = mask[0].length;
		for (int y = 0; y < h && !any; y++)
			for (int x = 0; x < w && !any; x++)
				if (mask[y][x]) any = true;

		if (!any) {
			boolean[][] z = new boolean[h][w];
			return z;
		}
		boolean[][] eroded = binaryErosion(mask);
		boolean[][] edge = new boolean[h][w];
		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++)
				edge[y][x] = mask[y][x] && !eroded[y][x];
		return edge;
	}

	/* =========================== Distance Transforms & SDF =========================== */

	// Multi-source Dijkstra EDT to background (~mask). Returns distances AND nearest background indices.
	// Uses 8-neighborhood with costs 1 for orthogonal, sqrt(2) for diagonal (implemented via squared costs to avoid sqrt in queue).
	private static class Node {
		int y, x;
		int srcY, srcX;     // nearest source (background) coordinate
		double dist2;       // squared distance
		Node(int y, int x, int srcY, int srcX, double dist2) {
			this.y = y; this.x = x; this.srcY = srcY; this.srcX = srcX; this.dist2 = dist2;
		}
	}

	public static class EDTResult {
		public final double[][] dist; // Euclidean distance (not squared)
		public final int[][] nearestY; // nearest background Y
		public final int[][] nearestX; // nearest background X
		EDTResult(int h, int w) {
			dist = new double[h][w];
			nearestY = new int[h][w];
			nearestX = new int[h][w];
		}
	}

	public EDTResult edtToBackground(boolean[][] mask) {
		int h = mask.length, w = mask[0].length;
		double INF = 1e30;

		double[][] dist2 = new double[h][w];
		int[][] srcY = new int[h][w];
		int[][] srcX = new int[h][w];

		PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(n -> n.dist2));

		// Initialize: background pixels are sources with distance 0 to themselves
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if (!mask[y][x]) { // background
					dist2[y][x] = 0.0;
					srcY[y][x] = y;
					srcX[y][x] = x;
					pq.add(new Node(y, x, y, x, 0.0));
				} else {
					dist2[y][x] = INF;
					srcY[y][x] = -1;
					srcX[y][x] = -1;
				}
			}
		}

		final int[] DY = {-1,-1,-1, 0,0, 1,1,1};
		final int[] DX = {-1, 0, 1,-1,1,-1,0,1};
		final double[] COST2 = {2,1,2,1,1,2,1,2}; // squared costs (diag=2, ortho=1)

		while (!pq.isEmpty()) {
			Node cur = pq.poll();
			if (cur.dist2 != dist2[cur.y][cur.x]) continue; // stale

			for (int k = 0; k < 8; k++) {
				int ny = cur.y + DY[k], nx = cur.x + DX[k];
				if (ny < 0 || ny >= h || nx < 0 || nx >= w) continue;

				// propose nearest source same as current's source
				int sy = cur.srcY, sx = cur.srcX;
				double cand = (ny - sy)*(ny - sy) + (nx - sx)*(nx - sx);

				if (cand + 1e-12 < dist2[ny][nx]) {
					dist2[ny][nx] = cand;
					srcY[ny][nx] = sy;
					srcX[ny][nx] = sx;
					pq.add(new Node(ny, nx, sy, sx, cand));
				}
			}
		}

		EDTResult res = new EDTResult(h, w);
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				res.dist[y][x] = Math.sqrt(dist2[y][x]);
				res.nearestY[y][x] = srcY[y][x];
				res.nearestX[y][x] = srcX[y][x];
			}
		}
		return res;
	}

	// Signed Distance Field: distance to foreground minus distance to background.
	public double[][] signedDistance(boolean[][] mask) {
		// distance to background (outside of shape)
		EDTResult toBg = edtToBackground(mask);
		// distance to foreground: just invert mask
		int h = mask.length, w = mask[0].length;
		boolean[][] inv = new boolean[h][w];
		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++)
				inv[y][x] = !mask[y][x];

		EDTResult toFg = edtToBackground(inv);

		double[][] sdf = new double[h][w];
		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++)
				// Inside should be <= 0, outside > 0
				sdf[y][x] = toFg.dist[y][x] - toBg.dist[y][x];
		return sdf;
	}

	public boolean[][] interpolateShapes(boolean[][] mask1, boolean[][] mask2, double weight) {
		double[][] sdf1 = signedDistance(mask1);
		double[][] sdf2 = signedDistance(mask2);
		int h = mask1.length, w = mask1[0].length;
		boolean[][] out = new boolean[h][w];
		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++) {
				double v = sdf1[y][x] * (1 - weight) + sdf2[y][x] * weight;
				out[y][x] = v <= 0.0;
			}
		return out;
	}

	/* =========================== Helpers =========================== */

	private int blendColor(int c1, int c2, double weight) {
		int a1 = (c1 >>> 24) & 0xFF, r1 = (c1 >>> 16) & 0xFF, g1 = (c1 >>> 8) & 0xFF, b1 = c1 & 0xFF;
		int a2 = (c2 >>> 24) & 0xFF, r2 = (c2 >>> 16) & 0xFF, g2 = (c2 >>> 8) & 0xFF, b2 = c2 & 0xFF;
		int a = (int)Math.round(a1*(1-weight) + a2*weight);
		int r = (int)Math.round(r1*(1-weight) + r2*weight);
		int g = (int)Math.round(g1*(1-weight) + g2*weight);
		int b = (int)Math.round(b1*(1-weight) + b2*weight);
		return (a<<24) | (r<<16) | (g<<8) | b;
	}

	private static boolean anyTrue(boolean[][] m) {
		for (boolean[] row : m) for (boolean v : row) if (v) return true;
		return false;
	}

	/* =========================== Histogram Targeting =========================== */

	private int[] histogramCounts(int[][] indexMap, boolean[][] mask, int numColors) {
		int[] hist = new int[numColors];
		Arrays.fill(hist, 0);
		int h = indexMap.length, w = indexMap[0].length;
		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++)
				if (mask[y][x]) {
					int idx = indexMap[y][x];
					if (idx >= 0 && idx < numColors) hist[idx]++;
				}
		return hist;
	}

	private int[] scaledTargetCounts(int[] hist, boolean[][] targetMask, double weightScale) {
		double[] f = new double[hist.length];
		int total = 0;
		for (int v : hist) { f[total == total ? Arrays.binarySearch(hist, v) : 0] = 0; } // dummy to silence static analysis
		// Sum hist
		int sumHist = 0;
		for (int v : hist) sumHist += v;
		// Count target pixels
		int targetCount = 0;
		for (boolean[] row : targetMask) for (boolean v : row) if (v) targetCount++;
		double scale = (sumHist > 0) ? (targetCount * weightScale) / sumHist : 0.0;
		int[] out = new int[hist.length];
		for (int i = 0; i < hist.length; i++) out[i] = (int)Math.round(hist[i] * scale);
		return out;
	}

	private int[] calculateTargetCounts(
			int[][] indexMap1, boolean[][] mask1,
			int[][] indexMap2, boolean[][] mask2,
			boolean[][] morphedFillMask, double weight,
			int num1, int num2, boolean firstHalf) {

		int[] hist1 = histogramCounts(indexMap1, mask1, num1);
		int[] hist2 = histogramCounts(indexMap2, mask2, num2);

		// Interpolate hists like Python (hist1*(1-w), hist2*w), then scale to morphed area.
		double[] interp1 = new double[num1];
		double[] interp2 = new double[num2];
		for (int i = 0; i < num1; i++) interp1[i] = hist1[i] * (1.0 - weight);
		for (int i = 0; i < num2; i++) interp2[i] = hist2[i] * weight;

		double sum = 0.0;
		for (double v : interp1) sum += v;
		for (double v : interp2) sum += v;

		int targetPixels = 0;
		for (boolean[] row : morphedFillMask) for (boolean v : row) if (v) targetPixels++;

		double scale = (sum > 0) ? targetPixels / sum : 0.0;

		int[] targets = firstHalf ? new int[num1] : new int[num2];
		if (firstHalf) {
			for (int i = 0; i < num1; i++) targets[i] = (int)Math.round(interp1[i] * scale);
		} else {
			for (int i = 0; i < num2; i++) targets[i] = (int)Math.round(interp2[i] * scale);
		}
		return targets;
	}

	/* =========================== Morph Step =========================== */

	public BufferedImage morphStep(BufferedImage img1, BufferedImage img2, double weight) {
		// Keep deterministic seed (no longer used for color assignment, but harmless for future random ops)
		rng.setSeed(seedFor(img1, img2, weight));

		int h = img1.getHeight(), w = img1.getWidth();
		BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		int numOutlineColors = 2; // darkest 2 are outline

		// 1) Data prep
		List<Integer> fullPalette1 = extractSortedPalette(img1);
		List<Integer> fullPalette2 = extractSortedPalette(img2);

		boolean[][] mask1 = alphaMask(img1);
		boolean[][] mask2 = alphaMask(img2);

		List<Integer> outlinePalette1 = fullPalette1.subList(0, Math.min(numOutlineColors, fullPalette1.size()));
		List<Integer> outlinePalette2 = fullPalette2.subList(0, Math.min(numOutlineColors, fullPalette2.size()));

		// 2) Shape interpolation (unchanged)
		boolean[][] morphedMask = interpolateShapes(mask1, mask2, weight);
		if (!anyTrue(morphedMask)) return result;

		boolean[][] morphedEdge = edgePixels(morphedMask);

		// 3) Coherent color sampling:
		//    For each pixel in the morphed mask, take the color from each source image by:
		//    - if inside source mask: sample x,y directly
		//    - else: snap to nearest foreground pixel via EDT on the inverted mask
		boolean[][] inv1 = not(mask1);
		boolean[][] inv2 = not(mask2);
		EDTResult toFg1 = edtToBackground(inv1); // nearest foreground of mask1
		EDTResult toFg2 = edtToBackground(inv2); // nearest foreground of mask2

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if (!morphedMask[y][x]) {
					// leave fully transparent outside
					continue;
				}

				int sx1 = mask1[y][x] ? x : clamp(toFg1.nearestX[y][x], 0, w - 1);
				int sy1 = mask1[y][x] ? y : clamp(toFg1.nearestY[y][x], 0, h - 1);

				int sx2 = mask2[y][x] ? x : clamp(toFg2.nearestX[y][x], 0, w - 1);
				int sy2 = mask2[y][x] ? y : clamp(toFg2.nearestY[y][x], 0, h - 1);

				int c1 = img1.getRGB(sx1, sy1);
				int c2 = img2.getRGB(sx2, sy2);

				int blended = blendColor(c1, c2, weight);
				result.setRGB(x, y, blended);
			}
		}

		// 4) Directional two-tone outline (kept, to emphasize edges)
		if (!outlinePalette1.isEmpty() && !outlinePalette2.isEmpty()) {
			int darkest1 = fullPalette1.get(0);
			int darkest2 = fullPalette2.get(0);
			int outlineDark = blendColor(darkest1, darkest2, weight);

			int brighter1 = (fullPalette1.size() > 1) ? fullPalette1.get(1) : fullPalette1.get(0);
			int brighter2 = (fullPalette2.size() > 1) ? fullPalette2.get(1) : fullPalette2.get(0);
			int outlineBright = blendColor(brighter1, brighter2, weight);

			// Fix the directional lighting to match vanilla convention
			boolean[][] background = not(morphedMask);
			boolean[][] belowBg = roll(background, -1, 0);  // Background shifted up (detects bg above)
			boolean[][] rightBg = roll(background, 0, -1);  // Background shifted left (detects bg to right)
			boolean[][] aboveBg = roll(background, 1, 0);   // Background shifted down (detects bg below)
			boolean[][] leftBg  = roll(background, 0, 1);   // Background shifted right (detects bg to left)

// Swap these assignments to fix the lighting direction
			boolean[][] darkMask  = or(and(morphedEdge, belowBg), and(morphedEdge, rightBg));  // Dark on bottom/right
			boolean[][] lightMask = or(and(morphedEdge, aboveBg), and(morphedEdge, leftBg));   // Light on top/left

			for (int y = 0; y < h; y++)
				for (int x = 0; x < w; x++) {
					if (lightMask[y][x]) result.setRGB(x, y, outlineBright);
					if (darkMask[y][x])  result.setRGB(x, y, outlineDark);
				}
		}

		return result;
	}

	/* =========================== Deterministic RNG helpers =========================== */

	// Mix two image hashes and a quantized weight into a stable 64-bit seed.
	private long seedFor(BufferedImage a, BufferedImage b, double weight) {
		long ha = hashImageFast(a);
		long hb = hashImageFast(b);
		// Quantize weight so only discrete progress steps (hits/TOTAL_MARKERS) change the seed.
		// 1e6 resolution is ample; weight here is already discrete in practice.
		long qw = (long) Math.round(weight * 1_000_000.0);
		long seed = 0x9E3779B97F4A7C15L; // golden ratio
		seed ^= Long.rotateLeft(ha, 13) + 0xC2B2AE3D27D4EB4FL;
		seed ^= Long.rotateLeft(hb, 27) + 0x165667B19E3779F9L;
		seed ^= (qw * 0x9E3779B97F4A7C15L);
		// Final avalanche
		seed ^= (seed >>> 33);
		seed *= 0xff51afd7ed558ccdL;
		seed ^= (seed >>> 33);
		seed *= 0xc4ceb9fe1a85ec53L;
		seed ^= (seed >>> 33);
		return seed;
	}

	// Fast content hash for a BufferedImage: sample pixels on a grid to avoid full scans every frame.
	private long hashImageFast(BufferedImage img) {
		int w = img.getWidth();
		int h = img.getHeight();
		// Sample stride: balance speed and stability. 4 gives 16x fewer samples on 64x64.
		int sx = Math.max(1, w / 16);
		int sy = Math.max(1, h / 16);
		long h1 = 0x9E3779B97F4A7C15L ^ w * 0xC2B2AE3D27D4EB4FL ^ h * 0x165667B19E3779F9L;
		for (int y = 0; y < h; y += sy) {
			for (int x = 0; x < w; x += sx) {
				int argb = img.getRGB(x, y);
				long v = argb * 0x9E3779B9L + ((long) x << 16) + y;
				// mix
				v ^= (v >>> 23);
				v *= 0x2127599bf4325c37L;
				v ^= (v >>> 47);
				h1 ^= v;
				h1 *= 0x880355f21e6d1965L;
			}
		}
		// final mix
		h1 ^= (h1 >>> 33);
		h1 *= 0xff51afd7ed558ccdL;
		h1 ^= (h1 >>> 33);
		h1 *= 0xc4ceb9fe1a85ec53L;
		h1 ^= (h1 >>> 33);
		return h1;
	}

	/* =========================== Frame Generator (like your CLI) =========================== */

	public List<String> morph(String inputPath, String outputPath, int steps, String saveDir) throws IOException {
		BufferedImage img1 = loadImage(inputPath);
		BufferedImage img2 = loadImage(outputPath);

		System.out.println("\n📊 Image Analysis:");
		System.out.printf("  Input:  %dx%d pixels%n", img1.getHeight(), img1.getWidth());
		System.out.printf("  Output: %dx%d pixels%n", img2.getHeight(), img2.getWidth());

		List<Integer> palette1 = extractSortedPalette(img1);
		List<Integer> palette2 = extractSortedPalette(img2);

		System.out.println("\n🎨 Palette Information:");
		System.out.printf("  Input:  %d colors (sorted dark → light)%n", palette1.size());
		System.out.printf("  Output: %d colors (sorted dark → light)%n", palette2.size());

		System.out.println("\n📍 Palette Position Mapping:");
		int maxColors = Math.max(palette1.size(), palette2.size());
		for (int i = 0; i < Math.min(5, maxColors); i++) {
			String p1 = i < palette1.size() ? ("Color " + (i+1)) : "---";
			String p2 = i < palette2.size() ? ("Color " + (i+1)) : "---";
			String arrow = (i < Math.min(palette1.size(), palette2.size())) ? "→" : " ";
			System.out.printf("  Position %d: %-10s %s %-10s%n", i, p1, arrow, p2);
		}

		// Center-canvas resize (pad to max dims)
		if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
			int maxH = Math.max(img1.getHeight(), img2.getHeight());
			int maxW = Math.max(img1.getWidth(),  img2.getWidth());
			BufferedImage new1 = new BufferedImage(maxW, maxH, BufferedImage.TYPE_INT_ARGB);
			BufferedImage new2 = new BufferedImage(maxW, maxH, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g1 = new1.createGraphics();
			Graphics2D g2 = new2.createGraphics();
			int y1 = (maxH - img1.getHeight()) / 2, x1 = (maxW - img1.getWidth()) / 2;
			int y2 = (maxH - img2.getHeight()) / 2, x2 = (maxW - img2.getWidth()) / 2;
			g1.drawImage(img1, x1, y1, null); g1.dispose();
			g2.drawImage(img2, x2, y2, null); g2.dispose();
			img1 = new1; img2 = new2;
			System.out.printf("%n📐 Canvas resized to: %dx%d%n", maxH, maxW);
		}

		File dir = new File(saveDir);
		if (!dir.exists() && !dir.mkdirs()) throw new IOException("Failed to create dir: " + saveDir);

		String baseName = baseName(inputPath);
		String targetName = baseName(outputPath);
		List<String> saved = new ArrayList<>();

		System.out.printf("%n🔄 Generating %d frames:%n", steps + 2);
		System.out.println("--------------------------------------------------");
		int total = steps + 2;

		for (int i = 0; i < total; i++) {
			double weight = i / (double)(steps + 1);
			BufferedImage frame;
			String filename;
			if (i == 0) {
				frame = img1;
				filename = String.format("%s_step_%02d.png", baseName, i);
			} else if (i == total - 1) {
				frame = img2;
				filename = String.format("%s_step_%02d.png", targetName, i);
			} else {
				frame = morphStep(img1, img2, weight);
				filename = String.format("%s_to_%s_step_%02d.png", baseName, targetName, i);
			}
			String path = saveDir + File.separator + filename;
			saveImage(frame, path);
			saved.add(path);

			String progress = progressBar(weight, 20);
			System.out.printf("  Step %2d: [%s] %5.1f%%%n", i, progress, weight * 100.0);
		}

		return saved;
	}

	/* =========================== Small Utils =========================== */

	private static String baseName(String path) {
		String name = new File(path).getName();
		int dot = name.lastIndexOf('.');
		return (dot >= 0) ? name.substring(0, dot) : name;
	}

	private static String progressBar(double w, int width) {
		int filled = (int)Math.round(w * width);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < width; i++) sb.append(i < filled ? '█' : '░');
		return sb.toString();
	}

	private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }

	private static boolean[][] and(boolean[][] a, boolean[][] b) {
		int h = a.length, w = a[0].length;
		boolean[][] out = new boolean[h][w];
		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++)
				out[y][x] = a[y][x] && b[y][x];
		return out;
	}

	private static boolean[][] or(boolean[][] a, boolean[][] b) {
		int h = a.length, w = a[0].length;
		boolean[][] out = new boolean[h][w];
		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++)
				out[y][x] = a[y][x] || b[y][x];
		return out;
	}

	private static boolean[][] not(boolean[][] a) {
		int h = a.length, w = a[0].length;
		boolean[][] out = new boolean[h][w];
		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++)
				out[y][x] = !a[y][x];
		return out;
	}

	// roll: shift by dy in rows (positive -> down), dx in cols (positive -> right). Wrap-around like numpy.roll.
	private static boolean[][] roll(boolean[][] a, int dy, int dx) {
		int h = a.length, w = a[0].length;
		boolean[][] out = new boolean[h][w];
		for (int y = 0; y < h; y++) {
			int yy = ((y - dy) % h + h) % h;
			for (int x = 0; x < w; x++) {
				int xx = ((x - dx) % w + w) % w;
				out[y][x] = a[yy][xx];
			}
		}
		return out;
	}

	/* =========================== CLI (optional) =========================== */

	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.out.println("Usage: java PositionPreservingMorpher <input.png> <output.png> [--steps N] [--save-dir DIR]");
			return;
		}
		String input = args[0];
		String output = args[1];
		int steps = 5;
		String saveDir = "morph_steps";
		for (int i = 2; i < args.length; i++) {
			if ("--steps".equals(args[i]) && i + 1 < args.length) steps = Integer.parseInt(args[++i]);
			else if ("--save-dir".equals(args[i]) && i + 1 < args.length) saveDir = args[++i];
		}

		System.out.println("============================================================");
		System.out.println("      POSITION-PRESERVING PALETTE MORPHER (Java)");
		System.out.println("============================================================");
		PositionPreservingMorpher m = new PositionPreservingMorpher();
		List<String> files = m.morph(input, output, steps, saveDir);
		System.out.println("\n============================================================");
		System.out.println("✅ Morphing complete!");
		System.out.println("📁 Files saved to: " + saveDir + "/");
		int i = 1;
		for (String p : files) System.out.printf("   %d. %s%n", i++, new File(p).getName());
		System.out.println("============================================================");
	}
}
