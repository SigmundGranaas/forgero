package com.sigmundgranaas.forgero.item.forgerotool.model;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.item.forgerotool.Constants;
import com.sigmundgranaas.forgero.item.forgerotool.material.ForgeroToolMaterial;
import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartItem;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class ToolTextureManager {
    public static final Logger LOGGER = LogManager.getLogger(Forgero.MOD_NAMESPACE);
    private static ToolTextureManager manager = null;
    private final HashMap<String, MaterialColourPalette> materialPalettes = new HashMap<>();
    private final HashMap<ToolPartModelTypes, BaseTexture> templateTextures = new HashMap<>();

    public static ToolTextureManager getInstance() {
        if (manager == null) {
            manager = new ToolTextureManager();
        }
        return manager;
    }

    /**
     * A method which will check if a resource is prepared, and create the texture if it does not exist.
     * This method will not do anything if it finds an existing texture, or if the operation fails.
     *
     * @param id          Identifier for the requested texture
     * @param getResource A method for accessing the potential resource dependency
     * @throws IOException If a resource is not found, an IOException will be thrown.
     */
    public void createTextureDependencies(Identifier id, BiConsumer_WithExceptions<Identifier, Resource> getResource) throws IOException, URISyntaxException {
        if (resourceExists(id)) {
            return;
        }


        Optional<BaseTexture> templateTextureResult = getOrCreateTemplateTexture(id);

        Optional<MaterialColourPalette> paletteResult = getOrCreateMaterialPalette(id, getResource);

        if (templateTextureResult.isEmpty() || paletteResult.isEmpty()) {
            return;
        }

        BufferedImage recolouredTexture = templateTextureResult.get().createRecolouredImage(paletteResult.get());

        try {
            writeTextureToFile(id, recolouredTexture);
        } catch (Exception e) {
            LOGGER.warn(e);
            return;
        }
    }

    private void writeTextureToFile(Identifier id, BufferedImage recolouredTexture) throws IOException, URISyntaxException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("assets/forgero");
        ImageIO.setUseCache(false);
        File targetFile = new File(resource.toURI() + "/" + getFileNameFromId(id));
        targetFile.getParentFile().mkdirs();
        if (targetFile.createNewFile()) {
            ImageIO.write(recolouredTexture, "PNG", targetFile);
        }
    }


    private Optional<MaterialColourPalette> getOrCreateMaterialPalette(Identifier id, BiConsumer_WithExceptions<Identifier, Resource> getResource) {
        String toolPartName = getResourceNameFromID(id);
        Optional<String> materialResult = ForgeroToolPartItem.getMaterialFromFileName(toolPartName);
        if (materialResult.isEmpty()) {
            return Optional.empty();
        }
        String material = materialResult.get();
        if (materialPalettes.containsKey(material)) {
            return Optional.of(materialPalettes.get(material));
        }

        List<Identifier> materialDependencyIdentifiers = getMaterialDependencyFromMaterial(material);

        try {
            List<BufferedImage> materialTextureImages = new ArrayList<>();

            for (Identifier identifier : materialDependencyIdentifiers) {
                Resource materialTexture = getResource.apply(identifier);
                BufferedInputStream materialTextureStream = new BufferedInputStream(materialTexture.getInputStream());
                BufferedImage materialTextureImage = ImageIO.read(materialTextureStream);
                materialTextureImages.add(materialTextureImage);
            }
            MaterialColourPalette palette = MaterialColourPalette.createColourPalette(materialTextureImages);
            materialPalettes.put(material, palette);
            writePaletteAsImage(palette.getColourValues(), new Identifier("forgero:textures/item/" + materialResult.get().toString().toLowerCase(Locale.ROOT) + ".png"));
            return Optional.of(palette);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private void writePaletteAsImage(int[] palette, Identifier id) {
        try {
            BufferedImage paletteImage = new BufferedImage(palette.length, 1, BufferedImage.TYPE_INT_ARGB);
            for (int x = 0; x < palette.length; x++) {
                paletteImage.setRGB(x, 0, palette[x]);
            }
            writeTextureToFile(id, paletteImage);
        } catch (Exception e) {
            LOGGER.warn(e);
        }
    }

    private List<Identifier> getMaterialDependencyFromMaterial(String material) {
        return ForgeroToolMaterial.getMaterialRepresentations(material);
    }

    private Optional<BaseTexture> getOrCreateTemplateTexture(Identifier id) throws URISyntaxException {
        String fileName = getResourceNameFromID(id);
        Optional<ToolPartModelTypes> toolPartResult = ForgeroToolPartItem.getToolPartModelTypeFromFileName(fileName);

        if (toolPartResult.isEmpty()) {
            return Optional.empty();
        }
        ToolPartModelTypes toolPart = toolPartResult.get();

        if (templateTextures.containsKey(toolPart)) {
            return Optional.of(templateTextures.get(toolPart));
        }

        Optional<BaseTexture> BaseTextureResult = createBaseTexture(toolPart);

        if (BaseTextureResult.isEmpty()) {
            return Optional.empty();
        }
        BaseTexture baseTexture = BaseTextureResult.get();
        templateTextures.put(toolPart, baseTexture);

        writePaletteAsImage(baseTexture.getGreyScaleValues(), new Identifier("forgero:textures/item/" + toolPart.toString().toLowerCase() + "_template.png"));
        return Optional.of(baseTexture);
    }

    private Optional<BaseTexture> createBaseTexture(ToolPartModelTypes toolPart) throws URISyntaxException {
        String textureName = getTextureNameFromToolPart(toolPart);
        String baseTextureName = textureName + Constants.BASE_IDENTIFIER;
        String baseTextureFullPath = "config/templates/toolparts/" + baseTextureName + ".png";

        File targetFile;

        try {
            targetFile = getFileFromResource(baseTextureFullPath);
        } catch (Exception e) {
            return Optional.empty();
        }


        if (!targetFile.exists()) {
            return Optional.empty();
        }

        BufferedImage templateImage;
        try {
            BufferedInputStream imageStream = new BufferedInputStream(new FileInputStream(targetFile));
            templateImage = ImageIO.read(imageStream);

        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
        if (templateImage == null) {
            return Optional.empty();
        }

        BaseTexture baseTexture = BaseTexture.createBaseTexture(templateImage);
        templateTextures.put(toolPart, baseTexture);
        return Optional.of(baseTexture);
    }

    private String getTextureNameFromToolPart(ToolPartModelTypes toolPart) {
        return toolPart.toFileName();
    }

    private String getResourceNameFromID(Identifier id) {
        String texturePath = id.getPath();
        String[] elements = texturePath.split("/");
        String fileName = elements[elements.length - 1];
        fileName = fileName.replace(".png", "");
        return fileName;
    }

    private boolean resourceExists(Identifier id) {
        String baseTextureFullPath = "assets/forgero/textures/item/";
        String fileName = getResourceNameFromID(id);
        try {
            File targetFile = getFileFromResource(baseTextureFullPath + fileName + ".png");
            return targetFile.exists();
        } catch (Exception e) {
            return false;
        }
    }

    private String getFileNameFromId(Identifier id) {
        return id.getPath();
    }

    private File getFileFromResource(String fileName) throws URISyntaxException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return new File(resource.toURI());
        }
    }

    @FunctionalInterface
    public interface BiConsumer_WithExceptions<Identifier, Resource> {
        Resource apply(Identifier id) throws IOException;
    }
}