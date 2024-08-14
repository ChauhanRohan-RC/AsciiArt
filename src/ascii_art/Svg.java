package ascii_art;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.StringJoiner;

import util.U;

public class Svg {

    private static int sTextSpanId;

    private static void resetTextSpanId() {
        sTextSpanId = 0;
    }

    private static int nextTextSpanId() {
        return ++sTextSpanId;
    }

    @NotNull
    private static Element createNewTextSpan(@NotNull Document doc) {
        final Element tspan = doc.createElement("tspan");
        tspan.setAttribute("id", "tspan" + nextTextSpanId());
        return tspan;
    }

    @NotNull
    private static Element createLineTextSpan(@NotNull Document doc, float x, float y) {
        final Element line = createNewTextSpan(doc);
        line.setAttribute("x", String.valueOf(x));
        line.setAttribute("y", String.valueOf(y));
        line.setAttribute("sodipodi:role", "line");
        return line;
    }

    @NotNull
    private static Element createTextSpan(@NotNull Document doc, String content, int argb, boolean withFillOpacity) {
        final Element span = createNewTextSpan(doc);
        span.setAttribute("fill", U.hex(argb));

        if (withFillOpacity) {
            span.setAttribute("fill-opacity", String.valueOf(U.alpha01(argb)));
        }

        span.appendChild(doc.createTextNode(content));
        return span;
    }

    @NotNull
    private static String createStylesString(@Nullable Map<String, String> styleOptions) {
        if (styleOptions == null || styleOptions.isEmpty())
            return "";

        final StringJoiner sj = new StringJoiner(";");

        for (Map.Entry<String, String> e: styleOptions.entrySet()) {
            sj.add(e.getKey() + ":" + e.getValue());
        }

        return sj.toString();
    }

    /**
     * Creates a styled svg file with the given text and color associated with each character in the text
     *
     * <p>
     *     colors array should map each character in the given sequence to its color, <strong>EXCLUDING NEW LINE '\n' CHARACTERS</strong> i.e.
     *     {@code colors.length == textSequence.length() - number of '\n' in the textSequence}
     * </p>
     *
     * @param seq the character sequence
     * @param colors array of colors (argb's) for each character in the given sequence, <strong>EXCLUDING NEW LINE '\n' CHARACTERS</strong>
     * @param fontSizePx font size in pixels
     * @param lineHeightEm line height relative to font size (em units)
     * @param width width of the svg in units (pt, px, mm, in), for ex. "210mm", "8.5in"
     * @param height height of the svg in units (pt, px, mm, in), for ex. "297mm", "11in"
     * @param textX x position of textbox in pixels
     * @param textY y position of textbox in pixels
     * @param fontFamily font family
     * @param withFillOpacity true to include alpha channel of colors
     * @param pretty true to use indentation in output xml
     * @param styleOptions Style options, like "stroke-width", "letter-spacing", "word-spacing", "font-style", "font-variant", "font-weight", "font-stretch"
     * */
    public static String createSvg(@NotNull CharSequence seq, int @NotNull[] colors, float fontSizePx, float lineHeightEm, @NotNull String fontFamily, float textX, float textY, String width, String height, boolean withFillOpacity, boolean pretty, @Nullable Map<String, String> styleOptions) throws ParserConfigurationException, TransformerException {
        // 1.Create a Document
        final DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        final Document doc = docBuilder.newDocument();

        // root svg
        final Element svg = doc.createElement("svg");
        svg.setAttribute("version", "1.1");
        svg.setAttribute("xmlns", "http://www.w3.org/2000/svg");
        svg.setAttribute("xmlns:sodipodi", "http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd");
        svg.setAttribute("width", width);
        svg.setAttribute("height", height);
        doc.appendChild(svg);

        // root text
        final Element text = doc.createElement("text");
        text.setAttribute("id", "text1");
        text.setAttribute("x", String.valueOf(textX));
        text.setAttribute("y", String.valueOf(textY));
        text.setAttribute("font-family", fontFamily);
        text.setAttribute("font-size", fontSizePx + "px");
        text.setAttribute("xml:space", "preserve");

        final Map<String, String> styleOps = new HashMap<>();
        styleOps.put("font-family", fontFamily);
        styleOps.put("font-size", fontSizePx + "px");
        styleOps.put("line-height", lineHeightEm + "em");
        if (!(styleOptions == null || styleOptions.isEmpty())) {
            styleOps.putAll(styleOptions);
        }

        text.setAttribute("style", createStylesString(styleOps));

        // for each line
        resetTextSpanId();

        float lineY = textY;
        Element line = createLineTextSpan(doc, textX, lineY);   // First line
        final LinkedList<Element> lines = new LinkedList<>();
        lines.addLast(line);

        int j = 0;
        for (int i=0; i < seq.length(); i++) {
            char c = seq.charAt(i);
            if (c == '\n') {
                lineY += fontSizePx * lineHeightEm;
                line = createLineTextSpan(doc, textX, lineY);       // next line
                lines.addLast(line);
            } else {
                line.appendChild(createTextSpan(doc, String.valueOf(c), colors[j], withFillOpacity));
                j++;
            }
        }

        // add all the lines
        for (Element _line: lines) {
            text.appendChild(_line);
        }

        // add text to svg
        svg.appendChild(text);

        // Create a new Transformer instance
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, pretty? "yes": "no");

        // Create a new StreamResult to the output stream you want to use.
        final StringWriter stringWriter = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(stringWriter));
        return stringWriter.toString();
    }

    @NotNull
    public static String createSvg(@NotNull CharSequence seq, int @NotNull[] colors, boolean withFillOpacity, @Nullable Map<String, String> styleOptions) throws ParserConfigurationException, TransformerException {
        final Map<String, String> styles = new HashMap<>();
        styles.put("font-style", "normal");
        styles.put("font-variant", "normal");
        styles.put("font-weight", "normal");
        styles.put("font-stretch", "normal");
        styles.put("stroke-width", "0.26458");
        styles.put("letter-spacing", "-2px");
//        styles.put("word-spacing", );

        if (!(styleOptions == null || styleOptions.isEmpty())) {
            styles.putAll(styleOptions);
        }

        // TODO: test svg
//        return createSvg(seq, colors, 10, 0.7f, "Consolas", 50, 50, "210mm", "297mm", true, true, styles);
        return createSvg(seq, colors, 10, 1.25f, "Consolas", 50, 50, "210mm", "297mm", withFillOpacity, true, styles);
    }

    public static void main(String[] args) {
//        try {
//            System.out.println(createSvg("ABC\nDE", new int[]{ 0xFFE90000, 0xFF00D700, 0xFFE90000, 0xFF00D700, 0xFFE90000 }));
//        } catch (Exception ignored) {
//        }


    }

}
