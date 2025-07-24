package com.sigmundgranaas.forgero.render.model.item;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.model.api.RenderableTexture;
import com.sigmundgranaas.forgero.model.resolution.api.item.ItemModelResolver;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BakedQuadFactory;
import net.minecraft.client.render.model.BasicBakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.json.ModelElement;
import net.minecraft.client.render.model.json.ModelElementFace;
import net.minecraft.client.render.model.json.ModelElementTexture;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**

 A specialized renderer that bakes a Forgero Component into an efficient, non-overlapping BakedModel.
 <p>

 This combines multiple texture layers into a single composite view before generating geometry.

 It creates front-facing quads only for the visible pixels of each layer and side-facing quads only for the

 final silhouette of the model, eliminating Z-fighting and reducing the polygon count for optimal performance.
 */
public class ForgeroModelRenderer {
	private static final BakedQuadFactory QUAD_FACTORY = new BakedQuadFactory();
	private final Function<SpriteIdentifier, Sprite> textureGetter;
	private final ModelBakeSettings settings;
	private final ItemModelResolver resolver;
	private final Identifier modelId;
	private final ModelTransformation transformation;

	public ForgeroModelRenderer(Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings settings, ItemModelResolver resolver, Identifier modelId, ModelTransformation transformation) {
		this.textureGetter = textureGetter;
		this.settings = settings;
		this.resolver = resolver;
		this.modelId = modelId;
		this.transformation = transformation;
	}

	@Nullable
	public BakedModel bake(Component component, boolean isSideLit, Sprite particleSprite) {
		List<RenderableTexture> textures = resolver.resolve(component).orElse(Collections.emptyList());
		if (textures.isEmpty()) {
			return null;
		}


		// Step 1: Composite all layers into a single, flattened representation.
		CompositeModel composite = compositeLayers(textures);
		if (composite == null) {
			return new BasicBakedModel(List.of(), Map.of(), true, false, true, particleSprite, transformation, ModelOverrideList.EMPTY);
		}

		// Step 2: Generate the minimal required geometry from the composite model.
		List<ModelElement> elements = new ArrayList<>();
		elements.addAll(generateMainElements(composite)); // Front and back faces
		elements.addAll(generateSideElements(composite)); // Depth and edge faces

		// Step 3: Bake the generated model elements into vanilla BakedQuads.
		List<BakedQuad> quads = new ArrayList<>();
		for (ModelElement element : elements) {
			for (Map.Entry<Direction, ModelElementFace> entry : element.faces.entrySet()) {
				ModelElementFace face = entry.getValue();
				// Use the textureId string on the face to look up the correct sprite from our map.
				Sprite sprite = composite.spriteData().get(face.textureId);

				BakedQuad quad = QUAD_FACTORY.bake(element.from, element.to, face, sprite, entry.getKey(), settings, element.rotation, element.shade, modelId);
				quads.add(quad);
			}
		}

		// Step 4: Construct the final BakedModel.
		Map<Direction, List<BakedQuad>> faceQuads = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			faceQuads.put(dir, new ArrayList<>());
		}

		return new BasicBakedModel(quads, faceQuads, true, isSideLit, true, particleSprite, this.transformation, ModelOverrideList.EMPTY);

	}

	/**

	 A record to hold the results of the compositing process.

	 @param visibleLayerMap A 2D array indicating which texture layer is visible at each pixel.

	 @param spriteData A map from a texture path String to its corresponding Sprite object for easy lookup.

	 @param width The width of the composite model in pixels.

	 @param height The height of the composite model in pixels.
	 */
	private record CompositeModel(
			RenderableTexture[][] visibleLayerMap,
			Map<String, Sprite> spriteData,
			int width,
			int height
	) {
	}

	/**

	 Composites multiple texture layers into a single 2D representation.

	 It iterates through layers from top to bottom, "painting" pixels onto a grid.

	 The first opaque pixel found for a coordinate determines the visible layer at that point.

	 @param textures The list of texture layers to composite.

	 @return A CompositeModel containing the flattened view, or null if no valid sprites are found.
	 */
	@Nullable
	private CompositeModel compositeLayers(List<RenderableTexture> textures) {
		// The key is now the texture path string, for lookup using ModelElementFace.textureId
		Map<String, Sprite> spriteData = textures.stream()
				.collect(Collectors.toMap(
						RenderableTexture::texture, // Use the string path as the key
						texture -> textureGetter.apply(new SpriteIdentifier(net.minecraft.client.texture.SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(texture.texture()))),
						(a, b) -> b, // In case of duplicate texture paths, keep the latter
						LinkedHashMap::new
				));

		// We need to map the texture object to its sprite for the compositing part
		Map<RenderableTexture, Sprite> tempSpriteMap = textures.stream()
				.collect(Collectors.toMap(t -> t, t -> spriteData.get(t.texture()), (a, b) -> b));

		Sprite firstSprite = spriteData.values().stream().findFirst().orElse(null);
		if (firstSprite == null || firstSprite.getContents().getWidth() == 0 || firstSprite.getContents().getHeight() == 0) {
			return null;
		}

		int width = firstSprite.getContents().getWidth();
		int height = firstSprite.getContents().getHeight();

		RenderableTexture[][] visibleLayerMap = new RenderableTexture[height][width];
		List<RenderableTexture> sortedTextures = textures.stream()
				.sorted(Comparator.comparingInt(RenderableTexture::order).reversed())
				.toList();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				for (RenderableTexture texture : sortedTextures) {
					Sprite sprite = tempSpriteMap.get(texture);
					if (sprite != null && !isPixelTransparent(sprite.getContents(), x, y)) {
						visibleLayerMap[y][x] = texture;
						break; // Move to the next pixel once the top-most layer is found
					}
				}
			}
		}
		return new CompositeModel(visibleLayerMap, spriteData, width, height);
	}

	/**

	 Generates the main front and back faces for the composite model.

	 It uses a greedy meshing algorithm to combine adjacent pixels of the same layer into

	 the largest possible rectangular quads, minimizing the total polygon count.

	 @param composite The composite model data.

	 @return A list of ModelElements representing the main faces.
	 */
	private List<ModelElement> generateMainElements(CompositeModel composite) {
		List<ModelElement> elements = new ArrayList<>();
		boolean[][] visited = new boolean[composite.height][composite.width];

		for (int y = 0; y < composite.height; y++) {
			for (int x = 0; x < composite.width; x++) {
				if (visited[y][x] || composite.visibleLayerMap[y][x] == null) {
					continue;
				}

				RenderableTexture currentLayer = composite.visibleLayerMap[y][x];

				int width;
				for (width = 1; x + width < composite.width && !visited[y][x + width] && composite.visibleLayerMap[y][x + width] == currentLayer; width++) {
				}

				int height;
				outer:
				for (height = 1; y + height < composite.height; height++) {
					for (int k = 0; k < width; k++) {
						if (visited[y + height][x + k] || composite.visibleLayerMap[y + height][x + k] != currentLayer) {
							break outer;
						}
					}
				}

				elements.add(createMainElementForRect(x, y, width, height, currentLayer, composite));

				for (int h = 0; h < height; h++) {
					for (int w = 0; w < width; w++) {
						visited[y + h][x + w] = true;
					}
				}
			}
		}
		return elements;
	}

	/**

	 Creates a single front-and-back ModelElement for a given rectangular area.
	 */
	private ModelElement createMainElementForRect(int x, int y, int w, int h, RenderableTexture layer, CompositeModel composite) {
		float zOffset = 0.001f * layer.order();
		Vector2f customOffset = layer.offset() != null ? new Vector2f(layer.offset().x(), layer.offset().y()) : new Vector2f(0, 0);

		float modelScaleX = 16.0f / composite.width;
		float modelScaleY = 16.0f / composite.height;

		float fromX = x * modelScaleX;
		float fromY = 16.0f - (y + h) * modelScaleY;
		float toX = (x + w) * modelScaleX;
		float toY = 16.0f - y * modelScaleY;

		float[] uv = {x * modelScaleX, y * modelScaleY, (x + w) * modelScaleX, (y + h) * modelScaleY};
		float[] uvFlipped = {(x + w) * modelScaleX, y * modelScaleY, x * modelScaleX, (y + h) * modelScaleY};

		Map<Direction, ModelElementFace> faces = new EnumMap<>(Direction.class);
		// Pass the layer's texture path string as the textureId. This is the key change.
		faces.put(Direction.SOUTH, new ModelElementFace(null, -1, layer.texture(), new ModelElementTexture(uv, 0)));
		faces.put(Direction.NORTH, new ModelElementFace(null, -1, layer.texture(), new ModelElementTexture(uvFlipped, 0)));

		Vector3f from = new Vector3f(fromX + customOffset.x(), fromY - customOffset.y(), 7.5f - zOffset);
		Vector3f to = new Vector3f(toX + customOffset.x(), toY - customOffset.y(), 8.5f - zOffset);

		return new ModelElement(from, to, faces, null, true);
	}

	/**

	 Generates side quads for the model's final silhouette.
	 */
	private List<ModelElement> generateSideElements(CompositeModel composite) {
		List<ModelElement> elements = new ArrayList<>();
		Map<Side, List<Frame>> framesBySide = new EnumMap<>(Side.class);
		for (Side side : Side.values()) {
			framesBySide.put(side, new ArrayList<>());
		}

		for (int y = 0; y < composite.height; y++) {
			for (int x = 0; x < composite.width; x++) {
				RenderableTexture currentLayer = composite.visibleLayerMap[y][x];
				if (currentLayer == null) continue;

				for (Side side : Side.values()) {
					RenderableTexture neighborLayer = getNeighborLayer(x, y, side, composite);
					if (currentLayer != neighborLayer) {
						extendOrAddFrame(framesBySide.get(side), side, x, y, currentLayer);
					}
				}
			}

		}

		for (List<Frame> frameList : framesBySide.values()) {
			for (Frame frame : frameList) {
				elements.add(createModelElementFromFrame(frame, composite));
			}
		}
		return elements;
	}

	private void extendOrAddFrame(List<Frame> frames, Side side, int x, int y, RenderableTexture layer) {
		int pos = side.isVertical() ? x : y;
		int level = side.isVertical() ? y : x;

		for (Frame currentFrame : frames) {
			if (currentFrame.getSide() == side && currentFrame.getLevel() == level && currentFrame.getLayer() == layer && currentFrame.canExpand(pos)) {
				currentFrame.expand(pos);
				return;
			}
		}
		frames.add(new Frame(side, pos, level, layer));
	}

	private ModelElement createModelElementFromFrame(Frame frame, CompositeModel composite) {
		RenderableTexture layer = frame.getLayer();
		float zOffset = 0.001f * layer.order();
		Vector2f customOffset = layer.offset() != null ? new Vector2f(layer.offset().x(), layer.offset().y()) : new Vector2f(0, 0);

		float modelScaleX = 16.0f / composite.width;
		float modelScaleY = 16.0f / composite.height;

		float fromX, fromY, toX, toY;
		float u1, v1, u2, v2;
		Side side = frame.getSide();
		float min = frame.getMin();
		float max = frame.getMax();
		float level = frame.getLevel();

		switch (side) {
			case UP -> {
				fromX = min; toX = max + 1.0f; fromY = level; toY = level;
				u1 = min; u2 = max + 1.0f; v1 = level; v2 = level + 1.0f;
			}
			case DOWN -> {
				fromX = min; toX = max + 1.0f; fromY = level + 1.0f; toY = level + 1.0f;
				u1 = min; u2 = max + 1.0f; v1 = level; v2 = level + 1.0f;
			}
			case LEFT -> {
				fromX = level; toX = level; fromY = min; toY = max + 1.0f;
				u1 = level; u2 = level + 1.0f; v1 = min; v2 = max + 1.0f;
			}
			default -> { // RIGHT
				fromX = level + 1.0f; toX = level + 1.0f; fromY = min; toY = max + 1.0f;
				u1 = level; u2 = level + 1.0f; v1 = min; v2 = max + 1.0f;
			}
		}

		fromX *= modelScaleX; toX *= modelScaleX; fromY *= modelScaleY; toY *= modelScaleY;
		u1 *= modelScaleX; u2 *= modelScaleX; v1 *= modelScaleY; v2 *= modelScaleY;

		fromY = 16.0f - fromY;
		toY = 16.0f - toY;
		if (fromY > toY) { float temp = fromY; fromY = toY; toY = temp; }

		Map<Direction, ModelElementFace> faces = new EnumMap<>(Direction.class);
		// Here too, we pass the layer's texture path string as the textureId.
		ModelElementTexture texture = new ModelElementTexture(new float[]{u1, v1, u2, v2}, 0);
		faces.put(side.getDirection(), new ModelElementFace(null, -1, layer.texture(), texture));

		Vector3f from = new Vector3f(fromX + customOffset.x(), fromY - customOffset.y(), 7.5f - zOffset);
		Vector3f to = new Vector3f(toX + customOffset.x(), toY - customOffset.y(), 8.5f - zOffset);

		return new ModelElement(from, to, faces, null, true);
	}

	@Nullable
	private RenderableTexture getNeighborLayer(int x, int y, Side side, CompositeModel composite) {
		int nX = x + side.getOffsetX();
		int nY = y + side.getOffsetY();
		if (nX < 0 || nX >= composite.width || nY < 0 || nY >= composite.height) {
			return null;
		}
		return composite.visibleLayerMap[nY][nX];
	}

	private boolean isPixelTransparent(net.minecraft.client.texture.SpriteContents contents, int x, int y) {
		if (x < 0 || y < 0 || x >= contents.getWidth() || y >= contents.getHeight()) {
			return true;
		}
		return contents.isPixelTransparent(0, x, y);
	}

	private enum Side {
		UP(Direction.UP, 0, -1),
		DOWN(Direction.DOWN, 0, 1),
		LEFT(Direction.WEST, -1, 0),
		RIGHT(Direction.EAST, 1, 0);

		private final Direction direction;
		private final int offsetX;
		private final int offsetY;

		Side(Direction direction, int offsetX, int offsetY) {
			this.direction = direction;
			this.offsetX = offsetX;
			this.offsetY = offsetY;
		}

		public Direction getDirection() { return this.direction; }
		public int getOffsetX() { return this.offsetX; }
		public int getOffsetY() { return this.offsetY; }
		public boolean isVertical() { return this == DOWN || this == UP; }

	}

	private static class Frame {
		private final Side side;
		private final RenderableTexture layer;
		private final int level;
		private int min;
		private int max;


		public Frame(Side side, int value, int level, RenderableTexture layer) {
			this.side = side;
			this.min = value;
			this.max = value;
			this.level = level;
			this.layer = layer;
		}

		public void expand(int newValue) {
			if (newValue < this.min) this.min = newValue;
			else if (newValue > this.max) this.max = newValue;
		}

		public boolean canExpand(int value) {
			return value == this.max + 1 || value == this.min - 1;
		}

		public Side getSide() { return this.side; }
		public RenderableTexture getLayer() { return this.layer; }
		public int getMin() { return this.min; }
		public int getMax() { return this.max; }
		public int getLevel() { return this.level; }
	}
}
