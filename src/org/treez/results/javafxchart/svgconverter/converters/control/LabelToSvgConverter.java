package org.treez.results.javafxchart.svgconverter.converters.control;

import java.util.List;
import java.util.Objects;

import org.treez.results.javafxchart.svgconverter.converters.AbstractNodeToSvgConverter;
import org.treez.results.javafxchart.svgconverter.converters.NodeToSvgConverter;
import org.treez.results.javafxchart.svgconverter.enumerations.SvgTextAnchor;

import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Converts a Label to SVG code
 */
public class LabelToSvgConverter extends AbstractNodeToSvgConverter<Label> {

	
	//#region CONSTRUCTORS

	//#end region

	//#region METHODS

	/**
	 * Converts a Label to SVG code (without SVG header and end tags)
	 *
	 * @return
	 */
	@Override
	public String extendCode(String initialSvgString, Label label) {

		//comment
		String commentString = createComment(label);

		//label image
		String imageSvgString = createImageSvgStringFromLabel(label);

		//text
		String text = label.getText();

		//background color
		String backgroundFill = determineBackgroundFill(label);
		boolean hasBackground = backgroundFill != null;

		//x & y
		List<Node> childNodes = label.getChildrenUnmodifiable();
		Text textNode = null;
		for (Node childNode : childNodes) {
			boolean isText = childNode instanceof Text;
			if (isText) {
				textNode = (Text) childNode;
				break;
			}
		}

		Objects.requireNonNull(textNode, "Could not retrive Text node from Label.");

		Bounds bounds = label.getBoundsInParent();
		Double xl = bounds.getMinX();
		Double yl = bounds.getMinY();

		Bounds textBounds = textNode.getBoundsInParent();
		Double xt = textBounds.getMinX();
		Double yt = textBounds.getMinY();

		Double x = xl + xt;
		Double yField = yl + yt;

		//Bounds bounds = label.getBoundsInParent();
		//Double x = bounds.getMinX();
		boolean hasImage = !imageSvgString.isEmpty();
		if (hasImage) {
			Node image = label.getGraphic();
			Double xOffset = image.getBoundsInParent().getMaxX();
			x = x + xOffset;
		}
		Double baseLineOffset = label.getBaselineOffset();
		Double y = yField + baseLineOffset;

		//font
		Font font = label.getFont();
		String fontFamily = font.getFamily();
		Double fontSize = font.getSize();

		//font color
		Paint textFill = label.getTextFill();
		String fill = paintToColorString(textFill);

		//text anchor (horizontal alignment)
		SvgTextAnchor textAnchor = determineTextAnchor(label);

		//comment
		String svgString = commentString;

		//<rect> start
		boolean wrapInRect = hasImage || hasBackground;
		if (wrapInRect) {
			svgString = includeRectStartTag(svgString, imageSvgString, backgroundFill, hasBackground, textBounds);
		}

		//<text> start
		svgString = includeTextStartTag(svgString, x, y, fontFamily, fontSize, fill, textAnchor);

		//<text> content
		svgString = svgString + text;

		//<text> end
		svgString = svgString + "</text>\n\n";

		//<rect> end
		if (wrapInRect) {
			decreaseIndentation();
			svgString = includeRectEndTag(svgString);
		}

		return svgString;

	}

	private String includeTextStartTag(
			String initialSvgString,
			Double x,
			Double y,
			String fontFamily,
			Double fontSize,
			String fill,
			SvgTextAnchor textAnchor) {
		//@formatter:off
		String svgString = initialSvgString + indentation + "<text"
				+ " x=\""+ x + "\""
				+ " y=\""+ y + "\""
				+ " font-family=\""+ fontFamily + "\""
				+ " font-size=\""+ fontSize + "\"";
		//@formatter:on

		if (fill != null) {
			svgString = svgString + " fill=\"" + fill + "\"";
		}

		//if (!textAnchor.equals(SvgTextAnchor.LEFT)) {
		svgString = svgString + " text-anchor=\"" + textAnchor + "\"";
		//}

		svgString = svgString + ">";
		return svgString;
	}

	private static String determineBackgroundFill(Label label) {
		String backgroundFill = null;
		Background background = label.getBackground();
		if (background != null) {
			backgroundFill = backgroundToColorString(background);
		}
		return backgroundFill;
	}

	private static SvgTextAnchor determineTextAnchor(Label label) {
		Pos alignment = label.getAlignment();
		HPos horizontalAlignment = alignment.getHpos();

		SvgTextAnchor textAnchor;
		switch (horizontalAlignment) {
		case LEFT:
			textAnchor = SvgTextAnchor.LEFT;
			break;
		case CENTER:
			textAnchor = SvgTextAnchor.MIDDLE;
			break;
		case RIGHT:
			textAnchor = SvgTextAnchor.END;
			break;
		default:
			String message = "The text alignment '" + horizontalAlignment + "' is not known.";
			throw new IllegalStateException(message);
		}
		return textAnchor;
	}

	private String includeRectStartTag(
			String initialSvgString,
			String imageSvgString,
			String backgroundFill,
			boolean hasBackground,
			Bounds bounds) {

		String svgString = initialSvgString + indentation + "<g>\n";
		increaseIndentation();
		Double width = bounds.getWidth();
		Double height = bounds.getHeight();
		String rectString = "<rect width=\"" + width + "\" height=\"" + height + "\"";
		if (hasBackground) {
			rectString = rectString + " fill=\"" + backgroundFill + "\"";
		}
		rectString = rectString + "/>\n\n";
		svgString = svgString + indentation + rectString;

		svgString = svgString + imageSvgString;
		return svgString;
	}

	private String includeRectEndTag(String initialSvgString) {
		String svgString = initialSvgString + indentation + "</g>\n\n";
		return svgString;
	}

	private String createComment(Label label) {
		String className = label.getClass().getName();
		String styleClassComment = createCssClassString(label);
		String commentString = indentation + "<!-- " + className;
		if (!commentString.isEmpty()) {
			commentString = commentString + " | " + styleClassComment;
		}
		commentString = commentString + " | => handled as Label -->\n";

		return commentString;
	}

	private String createImageSvgStringFromLabel(Label label) {
		String imageSvgString = "";
		Node image = label.getGraphic();
		if (image != null) {

			NodeToSvgConverter nodeConverter = new NodeToSvgConverter();
			nodeConverter.setIndentation(indentation);
			nodeConverter.increaseIndentation();
			imageSvgString = nodeConverter.extendCode("", image);

		}
		return imageSvgString;
	}

	//#end region

}
