package ascii_art;

import org.apache.commons.math3.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class R {

    public static final boolean FROZEN = false;         // TODO: set true before packaging

    // Dir structure
    public static final Path DIR_MAIN = (FROZEN? Path.of("app") : Path.of("")).toAbsolutePath();
    public static final Path DIR_RES = DIR_MAIN.resolve("res");
    public static final Path DIR_FONT = DIR_RES.resolve("font");
    public static final Path DIR_IMAGE = DIR_RES.resolve("image");
    public static final Path DIR_IMAGE_ASCII_ART = DIR_IMAGE.resolve("ascii-art");

    // Fonts
    public static final Path FONT_PD_SANS_REGULAR = DIR_FONT.resolve("product_sans_regular.ttf");
    public static final Path FONT_PD_SANS_MEDIUM = DIR_FONT.resolve("product_sans_medium.ttf");

    // .................................... ASCII ART ..........................................

    public static final String ASCII_ART_TITLE = "Ascii Art";

    @Nullable
    public static final Path ASCII_ART_ICON = null /*DIR_IMAGE.resolve("ascii_art_icon.png")*/;

    public static final String IMAGE_ASCII_ART_INPUT_FILE_NAME_TOKEN_SUFFIX = "ascii-in";

    @Nullable
    public static final Path IMAGE_ASCII_ART_INPUT = findFileWithNameTokenSuffix(DIR_IMAGE_ASCII_ART, IMAGE_ASCII_ART_INPUT_FILE_NAME_TOKEN_SUFFIX);


    /* ...................................  Utility functions  ................................ */

    @NotNull
    public static Pair<String, String> splitNameExt(@NotNull String fullName, boolean withDot) {
        final int i = fullName.lastIndexOf('.');

        if (i != 1) {
            return new Pair<>(fullName.substring(0, i), fullName.substring(withDot? i: i + 1));
        }

        return new Pair<>(fullName, "");
    }

    @NotNull
    public static String getName(@NotNull String fullName) {
        final int i = fullName.lastIndexOf('.');
        return i != -1? fullName.substring(0, i): fullName;
    }


    @Nullable
    public static Path findFileWithNameTokenSuffix(@NotNull Path dir, @NotNull String fileNameTokenSuffix) {
        // Finding any file in IMG folder with name == IMAGE_ASCII_ART_INPUT_FILE_NAME

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, Files::isRegularFile)) {
            for (Path p: stream) {
                final String name = getName(p.getFileName().toString());
                if (name.endsWith(fileNameTokenSuffix)) {
                    return p;
                }
            }
        } catch (Exception exc) {
        }

        return null;
    }

}
