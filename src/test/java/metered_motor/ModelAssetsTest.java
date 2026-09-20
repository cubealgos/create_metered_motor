package metered_motor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

/**
 * The metered motor's model assets, checked against the files themselves rather than by loading
 * Minecraft (MOTOR-REQ-013): every model the blockstate JSON names exists, every model the item
 * definition's {@code minecraft:select} cases name exists (MM-15, TRADE-REQ-005), every model's
 * texture references resolve to either a PNG in this mod's namespace or the {@code create:}
 * namespace, and the six tier textures {@code tools/recolour.py} writes are 16x16.
 */
final class ModelAssetsTest {
    private static final Path ASSETS = Path.of("src/main/resources/assets/metered_motor");
    private static final Path BLOCKSTATE = ASSETS.resolve("blockstates/metered_motor.json");
    private static final Path ITEM_DEFINITION = ASSETS.resolve("items/metered_motor.json");
    private static final Path MODELS = ASSETS.resolve("models");
    private static final Path TEXTURES = ASSETS.resolve("textures");
    private static final Pattern MODEL_REF = Pattern.compile("\"model\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern TEXTURES_BLOCK = Pattern.compile("\"textures\"\\s*:\\s*\\{([^}]*)}");
    private static final Pattern RESOURCE_LOCATION = Pattern.compile("\"([a-z0-9_.-]+:[a-z0-9_/.-]+)\"");

    @Test
    void everyBlockstateModelExists() throws IOException {
        String blockstate = Files.readString(BLOCKSTATE);
        List<String> missing = new ArrayList<>();
        Matcher m = MODEL_REF.matcher(blockstate);
        while (m.find()) {
            Path modelFile = modelFileFor(m.group(1));
            if (!Files.isRegularFile(modelFile)) {
                missing.add(m.group(1) + " -> " + modelFile);
            }
        }
        assertTrue(missing.isEmpty(), "every blockstate model exists: " + missing);
    }

    @Test
    void everyItemDefinitionModelExists() throws IOException {
        String itemDefinition = Files.readString(ITEM_DEFINITION);
        List<String> found = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        Matcher m = MODEL_REF.matcher(itemDefinition);
        while (m.find()) {
            found.add(m.group(1));
            Path modelFile = modelFileFor(m.group(1));
            if (!Files.isRegularFile(modelFile)) {
                missing.add(m.group(1) + " -> " + modelFile);
            }
        }
        assertTrue(missing.isEmpty(), "every item definition model exists: " + missing);
        for (String tier : List.of("i", "ii", "iii")) {
            String reference = "metered_motor:block/metered_motor/item_" + tier;
            assertTrue(found.contains(reference), "item definition references " + reference + ": " + found);
        }
    }

    @Test
    void everyModelsTextureReferenceResolves() throws IOException {
        List<String> missing = new ArrayList<>();
        for (Path modelJson : modelFiles()) {
            String text = Files.readString(modelJson);
            Matcher texturesBlock = TEXTURES_BLOCK.matcher(text);
            if (!texturesBlock.find()) {
                continue;
            }
            Matcher refs = RESOURCE_LOCATION.matcher(texturesBlock.group(1));
            while (refs.find()) {
                String ref = refs.group(1);
                if (ref.startsWith("create:")) {
                    continue; // Create Fly's own texture, referenced by namespace, not copied.
                }
                Path png = textureFileFor(ref);
                if (!Files.isRegularFile(png)) {
                    missing.add(modelJson + ": " + ref + " -> " + png);
                }
            }
        }
        assertTrue(missing.isEmpty(), "every local texture reference resolves to a PNG: " + missing);
    }

    @Test
    void theSixRecolouredTexturesAre16x16() throws IOException {
        for (String tier : List.of("i", "ii", "iii")) {
            for (String prefix : List.of("motor_", "casing_")) {
                Path png = TEXTURES.resolve("block/" + prefix + tier + ".png");
                assertTrue(Files.isRegularFile(png), png + " exists");
                var image = ImageIO.read(png.toFile());
                assertEquals(16, image.getWidth(), png + " width");
                assertEquals(16, image.getHeight(), png + " height");
            }
        }
    }

    /** {@code "metered_motor:block/metered_motor/block_i"} -> {@code models/block/metered_motor/block_i.json}. */
    private static Path modelFileFor(String reference) {
        String path = reference.substring(reference.indexOf(':') + 1);
        return MODELS.resolve(path + ".json");
    }

    /** {@code "metered_motor:block/motor_i"} -> {@code textures/block/motor_i.png}. */
    private static Path textureFileFor(String reference) {
        String path = reference.substring(reference.indexOf(':') + 1);
        return TEXTURES.resolve(path + ".png");
    }

    private static List<Path> modelFiles() throws IOException {
        try (Stream<Path> walk = Files.walk(MODELS)) {
            return walk.filter(p -> p.toString().endsWith(".json")).toList();
        }
    }
}
