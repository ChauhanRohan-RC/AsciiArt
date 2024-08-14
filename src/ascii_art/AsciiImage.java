package ascii_art;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import processing.event.KeyEvent;
import processing.opengl.PJOGL;
import util.U;

import java.awt.*;
import java.nio.file.Path;


public class AsciiImage extends PApplet {

    public static final boolean SVG_FILL_OPACITY = true;

    public static final boolean TRANSFORM_COLORS = false;

    public static final Color COLOR_BG = new Color(0, 0, 0, 255);


    private final float @NotNull[] mTempHsb = new float[3];

    private int transformColor(int argb) {
//        Color.RGBtoHSB(U.red255(argb), U.green255(argb), U.blue255(argb), mTempHsb);

//        mTempHsb[1] = 1;
//        mTempHsb[2] = 1 - mTempHsb[2];

//        return Color.HSBtoRGB(mTempHsb[0], mTempHsb[1], mTempHsb[2]);

        return argb;
    }

    private void transformColors(int @NotNull[] colors) {
        if (!TRANSFORM_COLORS)
            return;

        for (int i=0; i < colors.length; i++) {
            colors[i] = transformColor(colors[i]);
        }
    }



    public record TextColors(@NotNull String textSequence, int @NotNull[] colors) {
    }

//    public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(10);


    /**
     * Maps the characters in the given shader sequence to corresponding brightness array
     *
     * @return An array of 256 characters, with index = brightness [0, 255], value = character for that brightness in the shader
     * */
    private static char @NotNull[] createMappedChars(@NotNull CharSequence shaderSequence) {
        final char[] arr = new char[256];
        for (int i = 0; i < arr.length; i++) {
            int index = round(map(i, 0, arr.length, 0, shaderSequence.length()));
            if (index >= shaderSequence.length()) {
                index = shaderSequence.length() - 1;
            }

            arr[i] = shaderSequence.charAt(index);
        }

        return arr;
    }

    public enum Shader {
        SHADE_CHARS("Shade Chars", "        .:░▒▓█"),

        SPECIAL("Special", "       .:-i|=+%O#@"),

        ALPHANUMERIC("Alphanumeric", "           _.,-=+:;cba!?0123456789$W#@Ñ"),

        ALL_LOWERCASE("All Lowercase", "         .`-_':,;^=+/\"|)\\<>)iv%xclrs{*}?!][1taeo7zjun#wfy325p6mqghd4gbk&8$@0"),

        ALL("All", "         .`-_':,;^=+/\"|)\\<>)iv%xclrs{*}I?!][1taeo7zjLunT#JCwfy325Fp6mqSghVd4EgXPGZbYkOA&8U$@KHDBWNMR0Q")
        ;

        @NotNull
        public final String displayName;
        @NotNull
        public final String shaderSequence;

        /**
         * Array of 256 chars, where each index is mapped to a char in the {@link #shaderSequence sequence} with corresponding brightness: From low to high
         *
         * @see AsciiImage#createMappedChars(CharSequence)
         * */
        private volatile char @Nullable[] mMappedChars;

        Shader(@NotNull String displayName, @NotNull String shaderSequence) {
            this.displayName = displayName;
            this.shaderSequence = shaderSequence;
        }

        private char[] getMappedChars() {
            if (mMappedChars == null) {
                synchronized (this) {
                    if (mMappedChars == null) {
                        mMappedChars = createMappedChars(shaderSequence);
                    }
                }
            }

            return mMappedChars;
        }

        public char getCharacterForBrightness255(int brightness /* [0, 255] */) {
            return getMappedChars()[brightness];            // TODO: invert chars
        }

        public char getCharacterForBrightness01(float brightness /* [0, 1] */) {
            return getCharacterForBrightness255(floor(brightness * 255));
        }

        @Override
        public String toString() {
            return displayName;
        }

        /*.....................*/
    }


    // TODO
//    public static TextColors toAscii(int src_width, int src_height, int[] src_pixels, boolean out_coloured) {
////        float asp = (float) width / height, src_asp = (float) src.width / src.height;
////        float nw, nh;
////        if (asp == src_asp) {
////            nw = width; nh = height;
////        } else if (asp < src_asp) {
////            nw = width; nh = (nw / src_asp);
////        } else {
////            nh = height; nw = (nh * src_asp);
////        }
////
////        translate((width - nw) / 2f, (height - nh) / 2f);
////        scale(nw / src.width, nh / src.height);
////
////        textSize(fontSize);
//
//        int index = 0;
////        src.loadPixels();
//
//        final StringBuilder sb = new StringBuilder(src_pixels.length + 2);
//        final int[] out_colours = new int[src_pixels.length];
//
//        // for each row
//        for (int y = 0; y < src_height; y++) {
//
//            // for each column
//            for (int x = 0; x < src_width; x++) {
//                final int pixelColor = src_pixels[index];
//                final int brightness = computeBrightness255(pixelColor);
//
//                if (out_coloured) {
//                    out_colours[index] = pixelColor;
//                } else {
//                    out_colours[index] = U.withAlpha(Color.WHITE.getRGB(), U.alpha255(pixelColor));
//                }
//
//                final char c = mShader.getCharacterForBrightness255(brightness);
//                if (sb != null) {
//                    sb.append(c);
//                }
//
//                text(c, 0, 0);
//
//                // Move to the next pixel
//                index++;
//
//                // Move over for next character
//                translate(1, 0);
//            }
//
//            popMatrix();
//
//            // Move down for next line
//            translate(0,  1);
//            if (sb != null) {
//                sb.append('\n');
//            }
//        }
//    }


    public static final float SRC_RES_SCALE_MIN = 0.1f;
    public static final float SRC_RES_SCALE_MAX = 2f;
    public static final float SRC_RES_SCALE_DEFAULT = 1f;
    public static final int DEFAULT_SRC_RES_SCALE_PIXEL_TOLERANCE = 0;

    public static final Shader DEFAULT_SHADER = Shader.ALL;
    public static final boolean DEFAULT_COLOUR_ENABLED = true;
    public static final boolean DEFAULT_HUD_ENABLED = true;
    public static final float DEFAULT_FONT_SIZE = 1.0f;
//    public static final float DEFAULT_LINE_SPACING_EM = 1.5f;
//    public static final float DEFAULT_CHARACTER_SPACING_EM = 1.5f;

//    private static final Dimension WINDOW_SIZE = new Dimension(round(U.SCREEN_SIZE.width * 0.8f), round(U.SCREEN_SIZE.height * 0.8f));


    @NotNull
    private final Path mInputImgPath;
    private int _w, _h;
    private KeyEvent mKeyEvent;

    private PImage o_src;
    private PImage src;

    @NotNull
    private Shader mShader;
    private boolean mColoured = DEFAULT_COLOUR_ENABLED;
    private boolean mHudEnabled = DEFAULT_HUD_ENABLED;

//    PFont font;
    private float fontSize = DEFAULT_FONT_SIZE;
//    private float lineSpacingEm = DEFAULT_LINE_SPACING_EM;
//    private float charSpacingEm = DEFAULT_CHARACTER_SPACING_EM;
    private float mSrcResScale = SRC_RES_SCALE_DEFAULT;


    public AsciiImage(@NotNull Path inputImagePath, @Nullable Shader shader) {
        mInputImgPath = inputImagePath;
        mShader = shader != null? shader: DEFAULT_SHADER;
    }

    @Override
    public void settings() {
        size(round(displayWidth * 0.6f), round(displayHeight * 0.6f), JAVA2D);
        smooth(4);

        if (R.ASCII_ART_ICON != null) {
            PJOGL.setIcon(R.ASCII_ART_ICON.toString());       // icon
        }

        _w = width;
        _h = height;
    }


    private void setSrcScaleInternal(float srcResScale, int pixelsTolerance) {
        final int newW = (int) (o_src.width * srcResScale);
        final int newH = (int) (o_src.height * srcResScale);

        if (abs(src.width - newW) > pixelsTolerance && abs(src.height - newH) > pixelsTolerance) {
            mSrcResScale = srcResScale;
            src = o_src.copy();
            src.resize(newW, newH);
//            resizeSrcInBackground(newW, newH);
            // TODO: changed
        }
    }



    private void setSrcScale(float srcResScale, int pixelsTolerance) {
        srcResScale = constrain(srcResScale, SRC_RES_SCALE_MIN, SRC_RES_SCALE_MAX);
        if (mSrcResScale == srcResScale)
            return;

        setSrcScaleInternal(srcResScale, pixelsTolerance);
    }

    private void setSrcScale(float srcResScale) {
        setSrcScale(srcResScale, DEFAULT_SRC_RES_SCALE_PIXEL_TOLERANCE);
    }


    public void setup() {
        surface.setResizable(true);
        surface.setTitle(R.ASCII_ART_TITLE);
        surface.setLocation((displayWidth - width) / 2, (displayHeight - height) / 2);

//        src = loadImage("deadpool.jpg");
//
////        src.resize(200, 200);
////        src.resize(width, height);

        o_src = loadImage(mInputImgPath.toString());
        o_src.loadPixels();
        transformColors(o_src.pixels);

        src = o_src.copy();
        setSrcScaleInternal(mSrcResScale, DEFAULT_SRC_RES_SCALE_PIXEL_TOLERANCE);

        textFont(new PFont(PFont.findFont("Consolas"), true));

//        printArray(Capture.list());

//        src = new Capture(this, "pipeline:autovideosrc");
//        src.resize(20, 20);
//        src.start();

//        font = loadFont("UniversLTStd-Light-48.vlw");
    }


    private int mSaveFrameId = 1;


    private void saveCurrentFrame(boolean saveText, boolean saveSvg) {
        final String saveFileName = R.getName(mInputImgPath.getFileName().toString()).replace(R.IMAGE_ASCII_ART_INPUT_FILE_NAME_TOKEN_SUFFIX, "") + " shader-" + mShader.displayName + "-frame-" + mSaveFrameId;

        final TextColors textColors = updateDrawing(saveText || saveSvg);
        final String frameFIle = saveFileName + ".png";
        saveFrame(frameFIle);
        println("Frame saved to '" + frameFIle + "'");

        if (textColors != null) {
            if (saveText) {
                final String textFile = saveFileName + ".txt";
                saveStrings(textFile, new String[] { textColors.textSequence() });
                println("Shader text saved to '" + textFile + "'");
            }

            if (saveSvg) {
                final String svgFile = saveFileName + ".svg";

                try {
                    final String svgStr = Svg.createSvg(textColors.textSequence(), textColors.colors(), SVG_FILL_OPACITY, null);
                    saveStrings(svgFile, new String[] { svgStr });
                    println("Svg saved to '" + svgFile + "'");
                } catch (Exception exc) {
                    System.err.println("\nFailed to create SVG: " + exc.getMessage());
                    exc.printStackTrace();
                }
            }
        }

        mSaveFrameId++;
    }


//    public void captureEvent(Capture c) {
//        c.read();
//    }


    private static int computeBrightness255(int argb) {
        return round(U.perceivedLuminance255(argb) * U.alpha01(argb));
    }

    @Nullable
    public TextColors updateDrawing(boolean returnTextAndColors) {
        background(COLOR_BG.getRGB());

        pushMatrix();

//        final float scaleX = (float) width / src.width;
//        final float scaleY = (float) height / src.height;
//        final float scale = min(scaleX, scaleY) * fontSize;
//
//        final float scaledW = src.width * scale;
//        final float scaledH = src.height * scale;
//        scale(scale);
//        translate((width - scaledW) / 2, (height - scaledH) / 2);       // center


//        textFont(font, fontSize);


        // FIT_CENTER
        float asp = (float) width / height, src_asp = (float) src.width / src.height;
        float nw, nh;
        if (asp == src_asp) {
            nw = width; nh = height;
        } else if (asp < src_asp) {
            nw = width; nh = (nw / src_asp);
        } else {
            nh = height; nw = (nh * src_asp);
        }

        translate((width - nw) / 2f, (height - nh) / 2f);
        scale(nw / src.width, nh / src.height);

        textSize(fontSize);

        int index = 0;
        src.loadPixels();

        final StringBuilder sb = returnTextAndColors? new StringBuilder(src.pixels.length + 2): null;

        // for each row
        for (int y = 0; y < src.height; y++) {

            pushMatrix();
            // for each column
            for (int x = 0; x < src.width; x++) {
                final int pixelColor = src.pixels[index];
                final int brightness = computeBrightness255(pixelColor);

                if (mColoured) {
                    fill(pixelColor);
                } else {
                    fill(U.withAlpha(Color.WHITE.getRGB(), U.alpha255(pixelColor)));
                }

                final char c = mShader.getCharacterForBrightness255(brightness);
                if (sb != null) {
                    sb.append(c);
                }

                text(c, 0, 0);

                // Move to the next pixel
                index++;

                // Move over for next character
                translate(1, 0);
            }

            popMatrix();

            // Move down for next line
            translate(0,  1);
            if (sb != null) {
                sb.append('\n');
            }
        }

        popMatrix();

        if (mHudEnabled) {
            final String status = String.format("Scale  {Up | Down} : %.2fx\nFont SIze  {Ctrl-[Up | Down]} : %.2f\nShader  {S} : %s\nColour Mode  {C} : %s", mSrcResScale, fontSize, mShader.displayName, mColoured? "RGB": "Mono");

            pushMatrix();
            pushStyle();

            textSize(13);
            fill(255);
            translate(10, height - (textAscent() + textDescent()) * 4 - 10);
            text(status, 0, 0);

            popStyle();
            popMatrix();

//          // FIT_CENTER
//            float hnw, hnh;
//            float factor = 0.25f;
//
//            if (asp == src_asp) {
//                hnw = width * factor; hnh = height * factor;
//            } else if (asp < src_asp) {
//                hnw = width * factor; hnh = (hnw / src_asp);
//            } else {
//                hnh = height * factor; hnw = (hnh * src_asp);
//            }
//
//            translate(0, height - hnh);
//            stroke(255);
//            fill(0);
//            strokeWeight(1);
//            rect(0, 0, hnw, hnh, 2, 2, 2, 2);
//            image(src, 2, 2, hnw - 4, hnh - 4);
        }

        return sb != null? new TextColors(sb.toString(), src.pixels): null;
    }


//    private void drawGrays() {
//        background(0);
//
//        float w = width / 256f;
//        noStroke();
//
//        for (int i=0; i < 256; i++) {
//            fill(color(i));
//            rect(i * w, 0, w, height);
//        }
//    }


    private void onResized(int width, int height) {
        // todo: rescale may be
    }

    private void preDraw() {
        if (_w != width || _h != height) {
            _w = width;
            _h = height;
            onResized(width, height);
        }

        /* Handle Keys [Continuous] */
        if (keyPressed && mKeyEvent != null) {
            onContinuousKeyPressed(mKeyEvent);
        }
    }


    public void draw() {
        preDraw();

        updateDrawing(false);

        postDraw();
    }


    private void postDraw() {


    }


    private void onColouredChanged(boolean coloured) {
        println("COLOUR MODE: " + (coloured? "RGB": "MONO"));
    }

    public boolean isColoured() {
        return mColoured;
    }

    private void setColoured(boolean coloured) {
        if (mColoured == coloured)
            return;

        mColoured = coloured;
        onColouredChanged(coloured);
    }

    private void toggleColoured() {
        setColoured(!mColoured);
    }



    private void onHudEnabledChanged(boolean hudEnabled) {
        println("HUD " + (hudEnabled? " Enabled": "Disabled"));
    }

    public boolean isHudEnabled() {
        return mHudEnabled;
    }

    private void setHudEnabled(boolean hudEnabled) {
        if (mHudEnabled == hudEnabled)
            return;

        mHudEnabled = hudEnabled;
        onHudEnabledChanged(hudEnabled);
    }

    private void toggleHudEnabled() {
        setHudEnabled(!mHudEnabled);
    }


    private void onShaderChanged(Shader old, @NotNull Shader _new) {
        println("Shader Changed: " + old + " -> " + _new);
    }

    @NotNull
    public Shader getShader() {
        return mShader;
    }

    public void setShader(@NotNull Shader shader) {
        if (mShader == shader)
            return;

        final Shader old = mShader;
        mShader = shader;
        onShaderChanged(old, shader);
    }

    public void nextShader() {
        setShader(U.cycleEnum(Shader.class, mShader));
    }


    @Override
    public void keyPressed(KeyEvent event) {
        super.keyPressed(event);
        mKeyEvent = event;

        final int keycode = event.getKeyCode();

        switch (keycode) {
            case java.awt.event.KeyEvent.VK_S -> {
                if (event.isControlDown()) {
                    saveCurrentFrame(true, !event.isShiftDown());
                } else {
                    nextShader();
                }
            }

            case java.awt.event.KeyEvent.VK_C -> toggleColoured();
            case java.awt.event.KeyEvent.VK_H -> toggleHudEnabled();

            case java.awt.event.KeyEvent.VK_UP -> {
                if (event.isControlDown()) {
                    fontSize *= 1.1f;
                } else {
                    setSrcScale(mSrcResScale * 1.1f);
                }
            }

            case java.awt.event.KeyEvent.VK_DOWN -> {
                if (event.isControlDown()) {
                    fontSize *= 0.9f;
                } else {
                    setSrcScale(mSrcResScale * 0.9f);
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent event) {
        super.keyReleased(event);
        if (mKeyEvent != null && mKeyEvent.getKeyCode() == event.getKeyCode()) {
            mKeyEvent = null;
        }
    }

    private void onContinuousKeyPressed(@Nullable KeyEvent event) {
        if (event == null)
            return;

//        final int code = event.getKeyCode();
//        switch (code) {
//
//        }
    }

    public static void main(String[] args) {
        final Path input_img = R.IMAGE_ASCII_ART_INPUT;
        if (input_img == null) {
            System.err.println("No input image file detected!!\n Create an image file with name ending with '" + R.IMAGE_ASCII_ART_INPUT_FILE_NAME_TOKEN_SUFFIX + "' in folder: " + R.DIR_IMAGE_ASCII_ART + "\n For example: 'sample-" + R.IMAGE_ASCII_ART_INPUT_FILE_NAME_TOKEN_SUFFIX + ".png'");
            return;
        }

        final PApplet main = new AsciiImage(input_img, null);
        runSketch(concat(new String[] { main.getClass().getName() }, args), main);
    }
}


