package com.sigmundgranaas.forgero.smithing.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Position-preserving palette morpher for Minecraft textures.
 */
public class PositionPreservingMorpher {

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

	/* =========================== Morph Step =========================== */

	public BufferedImage morphStep(BufferedImage img1, BufferedImage img2, double weight) {
		if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
			BufferedImage[] padded = centerPadToSameSize(img1, img2);
			img1 = padded[0];
			img2 = padded[1];
		}

		weight = Math.max(0.0, Math.min(1.0, weight));

		int h = img1.getHeight(), w = img1.getWidth();
		BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

		boolean[][] mask1 = alphaMask(img1);
		boolean[][] mask2 = alphaMask(img2);

		boolean[][] morphedMask = interpolateShapes(mask1, mask2, weight);
		if (!anyTrue(morphedMask)) return result;

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

		// Create the edge masks from existing outline detection logic
		boolean[][] darkMask = new boolean[h][w];
		boolean[][] lightMask = new boolean[h][w];

// Extract edge detection logic to populate the masks
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				// Check if this pixel is on a dark edge (shadow side)
				darkMask[y][x] = isOnDarkEdge(x, y, morphedMask, w, h);
				// Check if this pixel is on a light edge (highlight side)
				lightMask[y][x] = isOnLightEdge(x, y, morphedMask, w, h);
			}
		}

// Sample actual edge colors from the original images instead of forcing darkest/brightest
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if (darkMask[y][x]) {
					// Sample the actual color at this edge position from both images
					int sx1 = mask1[y][x] ? x : clamp(toFg1.nearestX[y][x], 0, w - 1);
					int sy1 = mask1[y][x] ? y : clamp(toFg1.nearestY[y][x], 0, h - 1);
					int sx2 = mask2[y][x] ? x : clamp(toFg2.nearestX[y][x], 0, w - 1);
					int sy2 = mask2[y][x] ? y : clamp(toFg2.nearestY[y][x], 0, h - 1);

					int actualColor1 = img1.getRGB(sx1, sy1);
					int actualColor2 = img2.getRGB(sx2, sy2);

					// Ensure colors are opaque before darkening
					if (((actualColor1 >>> 24) & 0xFF) > 0 && ((actualColor2 >>> 24) & 0xFF) > 0) {
						result.setRGB(x, y, blendColor(actualColor1, actualColor2, weight));
					}
				} else if (lightMask[y][x]) {
					// Sample actual colors and lighten slightly for highlight effect
					int sx1 = mask1[y][x] ? x : clamp(toFg1.nearestX[y][x], 0, w - 1);
					int sy1 = mask1[y][x] ? y : clamp(toFg1.nearestY[y][x], 0, h - 1);
					int sx2 = mask2[y][x] ? x : clamp(toFg2.nearestX[y][x], 0, w - 1);
					int sy2 = mask2[y][x] ? y : clamp(toFg2.nearestY[y][x], 0, h - 1);

					int actualColor1 = img1.getRGB(sx1, sy1);
					int actualColor2 = img2.getRGB(sx2, sy2);

					// Ensure colors are opaque before lightening
					if (((actualColor1 >>> 24) & 0xFF) > 0 && ((actualColor2 >>> 24) & 0xFF) > 0) {
						result.setRGB(x, y, blendColor(actualColor1, actualColor2, weight));
					}
				}
			}
		}

		return result;
	}

	private boolean isOnDarkEdge(int x, int y, boolean[][] morphedMask, int w, int h) {
		// Check if this pixel is on an edge where we want dark outline
		// This typically means it's a foreground pixel adjacent to background
		if (!morphedMask[y][x]) return false; // Only apply to foreground pixels

		// Check 4-connected neighbors for background pixels
		boolean hasBackgroundNeighbor = false;
		int[] dx = {-1, 1, 0, 0};
		int[] dy = {0, 0, -1, 1};

		for (int i = 0; i < 4; i++) {
			int nx = x + dx[i];
			int ny = y + dy[i];
			if (nx >= 0 && nx < w && ny >= 0 && ny < h) {
				if (!morphedMask[ny][nx]) {
					hasBackgroundNeighbor = true;
					break;
				}
			} else {
				// Edge of image counts as background
				hasBackgroundNeighbor = true;
				break;
			}
		}

		return hasBackgroundNeighbor;
	}

	private boolean isOnLightEdge(int x, int y, boolean[][] morphedMask, int w, int h) {
		// More conservative light edge detection - only apply to very specific inner edges
		if (!morphedMask[y][x]) return false; // Only apply to foreground pixels

		// Check 8-connected neighbors for inner edge detection
		int foregroundNeighbors = 0;
		int totalNeighbors = 0;

		for (int dy = -1; dy <= 1; dy++) {
			for (int dx = -1; dx <= 1; dx++) {
				if (dx == 0 && dy == 0) continue; // Skip center pixel

				int nx = x + dx;
				int ny = y + dy;

				if (nx >= 0 && nx < w && ny >= 0 && ny < h) {
					totalNeighbors++;
					if (morphedMask[ny][nx]) {
						foregroundNeighbors++;
					}
				}
			}
		}

		// Much more conservative: only very specific inner edges, and reduce frequency
		return totalNeighbors > 0 && foregroundNeighbors >= totalNeighbors * 0.8 && foregroundNeighbors < totalNeighbors;
	}

	/* =========================== Small Utils =========================== */

	private static BufferedImage[] centerPadToSameSize(BufferedImage a, BufferedImage b) {
		int width = Math.max(a.getWidth(), b.getWidth());
		int height = Math.max(a.getHeight(), b.getHeight());
		BufferedImage paddedA = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		BufferedImage paddedB = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		Graphics2D graphicsA = paddedA.createGraphics();
		Graphics2D graphicsB = paddedB.createGraphics();

		graphicsA.drawImage(a, (width - a.getWidth()) / 2, (height - a.getHeight()) / 2, null);
		graphicsB.drawImage(b, (width - b.getWidth()) / 2, (height - b.getHeight()) / 2, null);
		graphicsA.dispose();
		graphicsB.dispose();

		return new BufferedImage[]{paddedA, paddedB};
	}

	private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }

	private static boolean[][] not(boolean[][] a) {
		int h = a.length, w = a[0].length;
		boolean[][] out = new boolean[h][w];
		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++)
				out[y][x] = !a[y][x];
		return out;
	}
}
