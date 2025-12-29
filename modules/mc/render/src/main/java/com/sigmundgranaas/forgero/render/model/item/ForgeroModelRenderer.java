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
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A specialized renderer that bakes a Forgero Component into an efficient, non-overlapping BakedModel.
 * This combines multiple texture layers into a single composite view before generating geometry.
 * It creates front-facing quads only for the visible pixels of each layer and side-facing quads only for the
 * final silhouette of the model, eliminating Z-fighting and reducing the polygon count for optimal performance.
 */
@SuppressWarnings("deprecation") // Uses SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE which is deprecated but still functional
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

		CompositeModel composite = compositeLayers(textures);
		if (composite == null) {
			return new BasicBakedModel(List.of(), Map.of(), true, false, true, particleSprite, transformation, ModelOverrideList.EMPTY);
		}

		List<ModelElement> elements = new ArrayList<>();
		elements.addAll(generateMainElements(composite));
		elements.addAll(generateSideElements(composite));

		List<BakedQuad> quads = new ArrayList<>();
		for (ModelElement element : elements) {
			for (Map.Entry<Direction, ModelElementFace> entry : element.faces.entrySet()) {
				ModelElementFace face = entry.getValue();
				Sprite sprite = composite.spriteData().get(face.textureId);
				BakedQuad quad = QUAD_FACTORY.bake(element.from, element.to, face, sprite, entry.getKey(), settings, element.rotation, element.shade, modelId);
				quads.add(quad);
			}
		}

		Map<Direction, List<BakedQuad>> faceQuads = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			faceQuads.put(dir, new ArrayList<>());
		}

		return new BasicBakedModel(quads, faceQuads, true, isSideLit, true, particleSprite, this.transformation, ModelOverrideList.EMPTY);
	}

	private record CompositeModel(
			RenderableTexture[][] visibleLayerMap,
			Map<String, Sprite> spriteData,
			int canvasWidth,
			int canvasHeight,
			int globalOffsetX,
			int globalOffsetY,
			int baseFrameWidth,
			int baseFrameHeight
	) {
	}

	@Nullable
	private CompositeModel compositeLayers(List<RenderableTexture> textures) {
		Map<String, Sprite> spriteData = textures.stream()
				.collect(Collectors.toMap(
						RenderableTexture::texture,
						texture -> textureGetter.apply(new SpriteIdentifier(net.minecraft.client.texture.SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(texture.texture()))),
						(a, b) -> b,
						LinkedHashMap::new
				));

		if (spriteData.isEmpty() || spriteData.values().stream().allMatch(s -> s.getContents().getWidth() == 0)) {
			return null;
		}

		// Step 1: Establish the base frame using the texture with the lowest order.
		RenderableTexture baseTexture = textures.stream()
				.min(Comparator.comparingInt(RenderableTexture::order))
				.orElse(textures.get(0));
		Sprite baseSprite = spriteData.get(baseTexture.texture());
		int baseFrameWidth = baseSprite.getContents().getWidth();
		int baseFrameHeight = baseSprite.getContents().getHeight();


		// Step 2: Determine the bounds of the virtual canvas based on all textures and their offsets.
		int minX = 0, minY = 0, maxX = baseFrameWidth, maxY = baseFrameHeight;
		for (RenderableTexture texture : textures) {
			Sprite sprite = spriteData.get(texture.texture());
			if (sprite == null) continue;
			minX = Math.min(minX, texture.offset().x());
			minY = Math.min(minY, texture.offset().y());
			maxX = Math.max(maxX, texture.offset().x() + sprite.getContents().getWidth());
			maxY = Math.max(maxY, texture.offset().y() + sprite.getContents().getHeight());
		}

		int canvasWidth = maxX - minX;
		int canvasHeight = maxY - minY;
		int globalOffsetX = -minX;
		int globalOffsetY = -minY;

		// Step 3: "Paint" textures onto the virtual canvas.
		RenderableTexture[][] visibleLayerMap = new RenderableTexture[canvasHeight][canvasWidth];
		List<RenderableTexture> sortedTextures = textures.stream()
				.sorted(Comparator.comparingInt(RenderableTexture::order).reversed())
				.toList();

		for (RenderableTexture texture : sortedTextures) {
			Sprite sprite = spriteData.get(texture.texture());
			if (sprite == null) continue;
			int spriteWidth = sprite.getContents().getWidth();
			int spriteHeight = sprite.getContents().getHeight();

			for (int y = 0; y < spriteHeight; y++) {
				for (int x = 0; x < spriteWidth; x++) {
					int canvasX = x + texture.offset().x() + globalOffsetX;
					int canvasY = y + texture.offset().y() + globalOffsetY;

					if (canvasX >= 0 && canvasX < canvasWidth && canvasY >= 0 && canvasY < canvasHeight) {
						if (visibleLayerMap[canvasY][canvasX] == null && !isPixelTransparent(sprite.getContents(), x, y)) {
							visibleLayerMap[canvasY][canvasX] = texture;
						}
					}
				}
			}
		}

		return new CompositeModel(visibleLayerMap, spriteData, canvasWidth, canvasHeight, globalOffsetX, globalOffsetY, baseFrameWidth, baseFrameHeight);
	}

	private List<ModelElement> generateMainElements(CompositeModel composite) {
		List<ModelElement> elements = new ArrayList<>();
		boolean[][] visited = new boolean[composite.canvasHeight][composite.canvasWidth];

		for (int y = 0; y < composite.canvasHeight; y++) {
			for (int x = 0; x < composite.canvasWidth; x++) {
				if (visited[y][x] || composite.visibleLayerMap[y][x] == null) {
					continue;
				}

				RenderableTexture currentLayer = composite.visibleLayerMap[y][x];
				int width;
				for (width = 1; x + width < composite.canvasWidth && !visited[y][x + width] && composite.visibleLayerMap[y][x + width] == currentLayer; width++) {
				}

				int height;
				outer:
				for (height = 1; y + height < composite.canvasHeight; height++) {
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

	private ModelElement createMainElementForRect(int canvasX, int canvasY, int w, int h, RenderableTexture layer, CompositeModel composite) {
		float zOffset = 0.001f * layer.order();

		// Geometry is relative to the canvas. Convert to model space (0-16) using the base frame dimensions.
		float modelScaleX = 16.0f / composite.baseFrameWidth;
		float modelScaleY = 16.0f / composite.baseFrameHeight;

		float fromX = (canvasX - composite.globalOffsetX) * modelScaleX;
		float fromY = (canvasY - composite.globalOffsetY) * modelScaleY;
		float toX = fromX + (w * modelScaleX);
		float toY = fromY + (h * modelScaleY);

		// UV coordinates are relative to the original sprite.
		Sprite sprite = composite.spriteData.get(layer.texture());
		float spriteW = sprite.getContents().getWidth();
		float spriteH = sprite.getContents().getHeight();

		float u_from = (canvasX - (layer.offset().x() + composite.globalOffsetX)) / spriteW * 16.0f;
		float v_from = (canvasY - (layer.offset().y() + composite.globalOffsetY)) / spriteH * 16.0f;
		float u_to = u_from + (w / spriteW * 16.0f);
		float v_to = v_from + (h / spriteH * 16.0f);

		Map<Direction, ModelElementFace> faces = new EnumMap<>(Direction.class);
		faces.put(Direction.SOUTH, new ModelElementFace(null, -1, layer.texture(), new ModelElementTexture(new float[]{u_from, v_from, u_to, v_to}, 0)));
		faces.put(Direction.NORTH, new ModelElementFace(null, -1, layer.texture(), new ModelElementTexture(new float[]{u_to, v_from, u_from, v_to}, 0)));

		// Y-inversion for model space
		float finalFromY = 16.0f - toY;
		float finalToY = 16.0f - fromY;

		Vector3f from = new Vector3f(fromX, finalFromY, 7.5f - zOffset);
		Vector3f to = new Vector3f(toX, finalToY, 8.5f - zOffset);

		return new ModelElement(from, to, faces, null, true);
	}

	private List<ModelElement> generateSideElements(CompositeModel composite) {
		List<ModelElement> elements = new ArrayList<>();
		Map<Side, List<Frame>> framesBySide = new EnumMap<>(Side.class);
		for (Side side : Side.values()) {
			framesBySide.put(side, new ArrayList<>());
		}

		for (int y = 0; y < composite.canvasHeight; y++) {
			for (int x = 0; x < composite.canvasWidth; x++) {
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

		float modelScaleX = 16.0f / composite.baseFrameWidth;
		float modelScaleY = 16.0f / composite.baseFrameHeight;

		float fromX, fromY, toX, toY;
		float u1, v1, u2, v2;
		Side side = frame.getSide();
		float min = frame.getMin();
		float max = frame.getMax();
		float level = frame.getLevel();

		switch (side) {
			case UP -> {
				fromX = min - composite.globalOffsetX; toX = max + 1.0f - composite.globalOffsetX; fromY = level - composite.globalOffsetY; toY = level - composite.globalOffsetY;
				u1 = min; u2 = max + 1.0f; v1 = level; v2 = level + 1.0f;
			}
			case DOWN -> {
				fromX = min - composite.globalOffsetX; toX = max + 1.0f - composite.globalOffsetX; fromY = level + 1.0f - composite.globalOffsetY; toY = level + 1.0f - composite.globalOffsetY;
				u1 = min; u2 = max + 1.0f; v1 = level; v2 = level + 1.0f;
			}
			case LEFT -> {
				fromX = level - composite.globalOffsetX; toX = level - composite.globalOffsetX; fromY = min - composite.globalOffsetY; toY = max + 1.0f - composite.globalOffsetY;
				u1 = level; u2 = level + 1.0f; v1 = min; v2 = max + 1.0f;
			}
			default -> { // RIGHT
				fromX = level + 1.0f - composite.globalOffsetX; toX = level + 1.0f - composite.globalOffsetX; fromY = min - composite.globalOffsetY; toY = max + 1.0f - composite.globalOffsetY;
				u1 = level; u2 = level + 1.0f; v1 = min; v2 = max + 1.0f;
			}
		}

		fromX *= modelScaleX; toX *= modelScaleX; fromY *= modelScaleY; toY *= modelScaleY;

		Sprite sprite = composite.spriteData.get(layer.texture());
		float spriteW = sprite.getContents().getWidth();
		float spriteH = sprite.getContents().getHeight();
		float u_from = (u1 - (layer.offset().x() + composite.globalOffsetX)) / spriteW * 16.0f;
		float v_from = (v1 - (layer.offset().y() + composite.globalOffsetY)) / spriteH * 16.0f;
		float u_to = (u2 - (layer.offset().x() + composite.globalOffsetX)) / spriteW * 16.0f;
		float v_to = (v2 - (layer.offset().y() + composite.globalOffsetY)) / spriteH * 16.0f;

		float finalFromY = 16.0f - toY;
		float finalToY = 16.0f - fromY;
		if (finalFromY > finalToY) { float temp = finalFromY; finalFromY = finalToY; finalToY = temp; }

		Map<Direction, ModelElementFace> faces = new EnumMap<>(Direction.class);
		ModelElementTexture texture = new ModelElementTexture(new float[]{u_from, v_from, u_to, v_to}, 0);
		faces.put(side.getDirection(), new ModelElementFace(null, -1, layer.texture(), texture));

		Vector3f from = new Vector3f(fromX, finalFromY, 7.5f - zOffset);
		Vector3f to = new Vector3f(toX, finalToY, 8.5f - zOffset);

		return new ModelElement(from, to, faces, null, true);
	}

	@Nullable
	private RenderableTexture getNeighborLayer(int x, int y, Side side, CompositeModel composite) {
		int nX = x + side.getOffsetX();
		int nY = y + side.getOffsetY();
		if (nX < 0 || nX >= composite.canvasWidth || nY < 0 || nY >= composite.canvasHeight) {
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
