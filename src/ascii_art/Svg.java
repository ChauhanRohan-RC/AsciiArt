package ascii_art;

import org.jetbrains.annotations.NotNull;
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
import java.util.LinkedList;

import util.U;

public class Svg {

    @NotNull
    private static Element createLineTextSpan(@NotNull Document doc, float x, float y) {
        final Element line = doc.createElement("tspan");
        line.setAttribute("x", String.valueOf(x));
        line.setAttribute("y", String.valueOf(y));
        return line;
    }

    @NotNull
    private static Element createTextSpan(@NotNull Document doc, String content, int argb, boolean withFillOpacity) {
        final Element span = doc.createElement("tspan");
        span.setAttribute("fill", U.hex(argb));

        if (withFillOpacity) {
            span.setAttribute("fill-opacity", String.valueOf(U.alpha01(argb)));
        }

        span.appendChild(doc.createTextNode(content));
        return span;
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
     * @param lineHeightRel line height relative to font size
     * @param strokeWidthRel stroke width relative to font size
     * @param width width of the svg, for ex. "210mm", "8.5in"
     * @param height height of the svg, for ex. "297mm", "11in"
     * @param textX x position of textbox in pixels
     * @param textY y position of textbox in pixels
     * @param fontFamily font family
     * @param withFillOpacity true to include alpha channel of colors
     * @param pretty true to use indentation in output xml
     * */
    public static String createSvg(@NotNull CharSequence seq, int @NotNull[] colors, float fontSizePx, float lineHeightRel, float strokeWidthRel, @NotNull String fontFamily, float textX, float textY, String width, String height, boolean withFillOpacity, boolean pretty) throws ParserConfigurationException, TransformerException {
        // 1.Create a Document
        final DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        final Document doc = docBuilder.newDocument();

        // root svg
        final Element svg = doc.createElement("svg");
        svg.setAttribute("version", "1.1");
        svg.setAttribute("xmlns", "http://www.w3.org/2000/svg");
        svg.setAttribute("width", width);
        svg.setAttribute("height", height);
        doc.appendChild(svg);

        // root text
        final Element text = doc.createElement("text");
        text.setAttribute("x", String.valueOf(textX));
        text.setAttribute("y", String.valueOf(textY));
        text.setAttribute("font-size", fontSizePx + "px");
        text.setAttribute("stroke-width", String.valueOf(strokeWidthRel));
        text.setAttribute("font-family", fontFamily);
        text.setAttribute("style", "line-height:" + lineHeightRel);
        text.setAttribute("xml:space", "preserve");

        // for each line
        float lineY = textY;
        Element line = createLineTextSpan(doc, textX, lineY);   // First line
        final LinkedList<Element> lines = new LinkedList<>();
        lines.addLast(line);

        int j = 0;
        for (int i=0; i < seq.length(); i++) {
            char c = seq.charAt(i);
            if (c == '\n') {
                lineY += fontSizePx * lineHeightRel;
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
    public static String createSvg(@NotNull CharSequence seq, int @NotNull[] colors) throws ParserConfigurationException, TransformerException {
        return createSvg(seq, colors, 10, 1.25f, 0.26458f, "Consolas", 50, 50, "210mm", "297mm", true, true);
    }

    public static void main(String[] args) {
//        try {
//            System.out.println(createSvg("ABC\nDE", new int[]{ 0xFFE90000, 0xFF00D700, 0xFFE90000, 0xFF00D700, 0xFFE90000 }));
//        } catch (Exception ignored) {
//        }
    }

}
