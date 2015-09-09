package org.treez.results.javafxchart.svgconverter.converters.parent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.log4j.Logger;
import org.treez.results.javafxchart.svgconverter.SvgNodeProperties;
import org.treez.results.javafxchart.svgconverter.converters.AbstractNodeToSvgConverter;
import org.treez.results.javafxchart.svgconverter.converters.shape.ShapeToSvgConverter;
import org.treez.results.javafxchart.svgconverter.enumerations.SvgStrokeAlignment;
import org.treez.results.javafxchart.svgconverter.enumerations.SvgStrokeLineCap;

import javafx.geometry.Bounds;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;

/**
 * Converts a Region to SVG code
 */
public class RegionToSvgConverter extends AbstractNodeToSvgConverter<Region> {

	/**
	 * Logger for this class
	 */
	private static Logger sysLog = Logger.getLogger(RegionToSvgConverter.class);

	
	//#region METHODS

	/**
	 * Converts a Region to SVG code. The type hierarchy of Region is as follows:
	 *
	 * <pre>
	 *   *** Region (x)
	 *       **** Control
	 *            ***** Label (x)
	 *            ***** ...
	 *       **** ...
	 *
	 *  (The special case of Control should already have been handled.)
	 * </pre>
	 */

	@Override
	public String extendCode(String initialSvgString, Region region) {

		//(data from Node and Parent already have been added)
		//add data from Region
		String warningString = addDataFromRegion(region);
		String svgString = initialSvgString + warningString;

		//crate SVG code
		svgString = svgString + createSvgString();
		svgString = extendWithChildSvgCodeAndEndTag(svgString, region);

		return svgString;
	}

	/**
	 * Extracts SVG properties from the given Region and applies them.
	 * Returns some warnings as string if issues occur during data
	 * extraction.
	 *
	 * @param nodeProperties
	 * @param node
	 */
	private String addDataFromRegion(Region region) {

		String warningString = "";

		//comment
		svgNodeProperties.addComment("=> handled as Region");

		//x & y
		Bounds bounds = region.getBoundsInParent();

		Double x = bounds.getMinX();

		Double y = bounds.getMinY();

		svgNodeProperties.setX(x);
		svgNodeProperties.setY(y);

		/*
		Insets paddingInsets = region.getPadding();
		double leftPadding = paddingInsets.getLeft();
		double topPadding = paddingInsets.getTop();
		
		if (leftPadding != 0) {
		 System.out.println("" + leftPadding);
		 x += leftPadding;
		}
		*/

		//isDefinedByRect
		boolean isDefinedByRect = true;
		Shape shape = region.getShape();
		if (shape != null) {
			isDefinedByRect = false;
		}
		svgNodeProperties.setIsDefinedByRect(isDefinedByRect);

		//geometry
		if (isDefinedByRect) {
			//retrieve geometry directly from region

			//width
			String rectWidth = "" + region.getWidth();
			svgNodeProperties.setRectWidth(rectWidth);

			//height
			String rectHeight = "" + region.getHeight();
			svgNodeProperties.setRectHeight(rectHeight);
		} else {
			//retrieve geometry from shape
			svgNodeProperties = ShapeToSvgConverter.addDataFromShape(svgNodeProperties, shape);
		}

		//fill
		String fillWarningString = addFillDataFromRegion(region);
		warningString += fillWarningString;

		//stroke
		String strokeWarningString = addStrokeDataFromRegion(region);
		warningString += strokeWarningString;

		return warningString;

	}

	private String addFillDataFromRegion(Region region) {

		String warningString = "";

		Background backGround = region.getBackground();
		if (backGround != null) {

			//warning for multiple fills
			List<BackgroundFill> fills = backGround.getFills();
			boolean hasMultipleFills = fills != null & fills.size() > 1;
			if (hasMultipleFills) {
				warningString = warningString + indentation + "<!-- Warning: Multiple fills are not yet implemented. "
						+ "=> Only first fill will be used. -->\n";
			}

			//fill color
			String fillColor = backgroundToColorString(backGround);
			svgNodeProperties.setFill(fillColor);

			//fill radius
			List<Double> fillRadiuses = backgroundToFillRadiuses(backGround);
			svgNodeProperties.setFillRadius(fillRadiuses);
		}

		return warningString;
	}

	private String addStrokeDataFromRegion(Region region) {

		String warningString = "";

		Border border = region.getBorder();
		if (border != null) {

			//stroke colors
			List<String> strokeColors = borderToColorStrings(border);
			svgNodeProperties.setStroke(strokeColors);

			//stroke radiuses
			List<Double> strokeRadiuses = borderToStrokeRadii(border);
			svgNodeProperties.setStrokeRadius(strokeRadiuses);

			//stroke widths
			List<Double> strokeWidths = borderToStrokeWidths(border);
			svgNodeProperties.setStrokeWidth(strokeWidths);

			//stroke opacities
			List<Double> strokeOpacities = borderToStrokeOpacites(border);
			svgNodeProperties.setStrokeOpacities(strokeOpacities);
		}

		return warningString;
	}

	@Override
	protected String createStyleContentString() {

		String styleContent = super.createStyleContentString();

		//fill
		styleContent = addFillStyle(styleContent);

		//stroke
		styleContent = addStrokeStyle(styleContent);

		//stroke width
		styleContent = addStrokeWidthStyle(styleContent);

		//stroke alignment
		styleContent = addStrokeAlignmentStyle(styleContent);

		//stroke line cap
		styleContent = addStrokeLineCapStyle(styleContent);

		//stroke dash array
		styleContent = addStrokeDashArrayStyle(styleContent);
		
		return styleContent;
	}

	private String addFillStyle(String initialStyleContent) {
		String styleContent = initialStyleContent;
		String fill = svgNodeProperties.getFill();
		if (fill != null) {
			styleContent = styleContent + "fill:" + fill + ";";
		}
		return styleContent;
	}
	
	private String addStrokeStyle(String initialStyleContent) {
		String styleContent = initialStyleContent;
		List<String> strokes = svgNodeProperties.getStroke();
		if (strokes != null) {
			boolean hasOneStroke = strokes.size() == 1;
			if (hasOneStroke) {
				styleContent = styleContent + "stroke:" + strokes.get(0) + ";";
			} else {
				//the special case of multiple strokes is handled
				//in other methods (e.g. createRectGeometryString)
			}
		}
		return styleContent;
	}
	
	private String addStrokeWidthStyle(String initialStyleContent) {
		String styleContent = initialStyleContent;
		List<Double> strokeWidths = svgNodeProperties.getStrokeWidth();
		if (strokeWidths != null) {
			boolean hasOneStrokeWidth = strokeWidths.size() == 1;
			if (hasOneStrokeWidth) {
				Double strokeWidth = strokeWidths.get(0);
				if (!strokeWidth.equals(1)) {
					styleContent = styleContent + "stroke-width:" + strokeWidth + ";";
				}
			} else {
				//the special case of multiple strokes is handled
				//in other methods (e.g. createRectGeometryString)
			}
		}
		return styleContent;
	}
	
	private String addStrokeAlignmentStyle(String initialStyleContent) {
		String styleContent = initialStyleContent;
		SvgStrokeAlignment strokeAlignment = svgNodeProperties.getStrokeAlignment();
		if (strokeAlignment != null) {
			if (!strokeAlignment.equals(SvgStrokeAlignment.CENTER)) {
				styleContent = styleContent + "stroke-alignment:" + strokeAlignment + ";";
				String message = "The svg stroke-alignment (JavaFx: StrokeType) is set to '" + strokeAlignment + "'.\n"
						+ "This svg property is relativly new and might not yet be supported by your svg viewer.";
				sysLog.warn(message);
			}
		}
		return styleContent;
	}
		
	private String addStrokeLineCapStyle(String initialStyleContent) {
		String styleContent = initialStyleContent;
		SvgStrokeLineCap strokeLineCap = svgNodeProperties.getStrokeLineCap();
		if (strokeLineCap != null) {
			if (!strokeLineCap.equals(SvgStrokeLineCap.SQUARE)) {
				styleContent = styleContent + "stroke-linecap:" + strokeLineCap + ";";
			}
		}
		return styleContent;
	}
	
	private String addStrokeDashArrayStyle(String initialStyleContent) {
		String styleContent = initialStyleContent;
		String strokeDashArray = svgNodeProperties.getStrokeDashArray();
		if (strokeDashArray != null) {
			if (!strokeDashArray.isEmpty()) {
				styleContent = styleContent + "stroke-dasharray:" + strokeDashArray + ";";
			}
		}
		return styleContent;
	}

	@Override
	protected String createTagStartString(String idString, String styleString, String transformString) {

		Objects.requireNonNull(svgNodeProperties, "svg node propeties must not be null.");

		boolean hasChildren = svgNodeProperties.hasChildren();

		boolean isDefinedByRect = svgNodeProperties.isDefinedByRect();

		String pathShape = svgNodeProperties.getPathShape();
		boolean hasPathShape = pathShape != null && !pathShape.isEmpty();

		String startString = "";
		if (hasChildren) {
			//add a group tag as prefix and include the id, style and transform into that group tag
			startString = startString + indentation + "<g" + idString + styleString + transformString + ">\n";
			increaseIndentation();

			//create "base tag" (the id, style and transform are not included here
			//since they are already included in the group tag)
			if (isDefinedByRect) {
				//rects are drown as individual lines in a group
				//to be able to style the lines individually
				//this tag starts a group for the rect lines
				startString = startString + indentation + "<g>\n";
				increaseIndentation();
			} else {
				if (hasPathShape) {
					startString = startString + indentation + "<path";
				} else {
					//something went wrong: do not add corrupted path tag
					startString = startString + indentation;
				}
			}

		} else {
			//create individual tag and directly include id, style and transform
			if (isDefinedByRect) {
				//rects are drown as individual lines in a group
				//to be able to style the lines individually
				//this tag starts a group for the rect lines
				startString = startString + indentation + "<g" + idString + styleString + transformString + ">\n";
				increaseIndentation();
			} else {
				if (hasPathShape) {
					startString = startString + indentation + "<path" + idString + styleString + transformString;
				} else {
					//something went wrong: do not add corrupted path tag
					startString = startString + indentation;
				}
			}
		}

		return startString;
	}

	/**
	 * Creates the geometry string, including the end of the (base) tag. (This does not include the end tag of a maybe
	 * existing parent group.)
	 *
	 * @param svgNodeProperties
	 * @return
	 */
	@Override
	protected String createGeometryString() {

		boolean isDefinedByRect = svgNodeProperties.isDefinedByRect();

		String pathShape = svgNodeProperties.getPathShape();
		boolean hasPathShape = pathShape != null && !pathShape.isEmpty();

		if (isDefinedByRect) {
			//create rect geometry string
			String rectGeometryString = createSvgRectString(svgNodeProperties);
			return rectGeometryString;
		} else {
			if (hasPathShape) {
				//create path geometry string
				String shapeGeometryString = createPathGeometryString(pathShape);
				return shapeGeometryString;
			} else {
				//something went wrong: include a svg comment with a warning
				//the start string also checks for this issue and does not include a
				//start tag. Therefore, the start of the comment tag is included here without issues.
				String warningString = "<!-- warning: empty path shape -->\n";
				return warningString;
			}
		}
	}

	@SuppressWarnings({"checkstyle:magicnumber", "checkstyle:cyclomaticcomplexity", 
		"checkstyle:executablestatementcount", "checkstyle:javancss"})
	private String createSvgRectString(SvgNodeProperties svgNodeProperties) {

		String rectSvgString = "";

		String width = svgNodeProperties.getRectWidth();
		String height = svgNodeProperties.getRectHeight();

		List<Double> fillRadius = svgNodeProperties.getFillRadius();
		boolean hasFillRadius = fillRadius != null && !fillRadius.isEmpty();
		boolean hasIndividualFillRadiuses = hasFillRadius && fillRadius.size() > 1;

		List<String> stroke = svgNodeProperties.getStroke();
		boolean hasIndividualStrokes = stroke != null && stroke.size() > 1;
		if (hasIndividualFillRadiuses) {
			//not yet implemented
			rectSvgString = addWarningForIndividualFillRadiuses(rectSvgString);

			String strokeValue = stroke.get(0);
			stroke.clear();
			stroke.add(strokeValue);
		}

		List<Double> strokeWidth = svgNodeProperties.getStrokeWidth();
		boolean hasIndividualStrokeWidth = strokeWidth != null && strokeWidth.size() > 1;

		List<Double> strokeOpacity = svgNodeProperties.getStrokeOpacities();
		boolean hasIndividualStrokeOpacities = strokeOpacity != null && strokeOpacity.size() > 1;

		List<Double> strokeRadius = svgNodeProperties.getStrokeRadius();
		boolean hasStrokeRadius = strokeRadius != null && !strokeRadius.isEmpty();
		boolean hasIndividualStrokeRadii = strokeRadius != null && strokeRadius.size() > 1;
		if (hasIndividualStrokeRadii) {
			//not yet implemented
			rectSvgString = addWarningForIndividualStrokeRadii(rectSvgString);

			Double radius = strokeRadius.get(0);
			strokeRadius.clear();
			strokeRadius.add(radius);
		}

		boolean useIndividualBorders = hasIndividualStrokes || hasIndividualStrokeWidth || hasIndividualStrokeOpacities
				|| hasIndividualStrokeRadii;

		if (useIndividualBorders) {

			if (hasStrokeRadius) {
				//not yet implemented
				rectSvgString = addWarningForStrokeRadius(rectSvgString);
			}

			//create a rect where each border (top, right, bottom, left) can have an individual style
			rectSvgString = createRectWithIndividualBorders(width, height, fillRadius, stroke, 
					strokeWidth, strokeOpacity, rectSvgString);

		} else {
			//create a rect where all borders (top, right, bottom, left) have the same style
			rectSvgString = createRectWithHomogeneousBorder(width, height, fillRadius, hasFillRadius, strokeRadius, 
					hasStrokeRadius, rectSvgString);
		}

		decreaseIndentation();
		rectSvgString = rectSvgString + indentation + "</g>\n\n";
		return rectSvgString;
	}

	

	

	private String addWarningForIndividualFillRadiuses(String initialRectSvgString) {
		String rectSvgString = initialRectSvgString;
		String warnString = "Warning: Individual fill radiuses are not yet implemented. => Using first fill radius.";
		sysLog.warn(warnString);
		rectSvgString += indentation + "<!--" + warnString + "-->\n";
		return rectSvgString;
	}
	
	private String addWarningForIndividualStrokeRadii(String initialRectSvgString) {
		String rectSvgString = initialRectSvgString;
		String warnString = "Warning: Individual stroke radiuses are not yet implemented. => Using first stroke radius.";
		sysLog.warn(warnString);
		rectSvgString += indentation + "<!--" + warnString + "-->\n";
		return rectSvgString;
	}
	
	private String addWarningForStrokeRadius(String initialRectSvgString) {
		String rectSvgString = initialRectSvgString;
		String warnString = "Warning: The stroke radius is not yet implemented for individual stroke styles. "
				+ "=> Using straight strokes.";
		sysLog.warn(warnString);
		rectSvgString += indentation + "<!--" + warnString + "-->\n";
		return rectSvgString;
	}

	@SuppressWarnings("checkstyle:magicnumber")
	private String createRectWithIndividualBorders(
			String width,
			String height,
			List<Double> fillRadius,
			List<String> initialStroke,
			List<Double> initialStrokeWidth,
			List<Double> initialStrokeOpacity,
			String rectGeometryString) {

		String rectSvgString = rectGeometryString;

		List<String> stroke = prepareArray(initialStroke);
		List<Double> strokeWidth = prepareArray(initialStrokeWidth);
		List<Double> strokeOpacity = prepareArray(initialStrokeOpacity);

		//rectangle start for showing fill
		rectSvgString = addRectangleForShowingFill(width, height, fillRadius, rectSvgString);

		//individual border lines to apply individual border styles
		String topStroke = stroke.get(0);
		Double topStrokeWidth = strokeWidth.get(0);
		Double topOpacity = strokeOpacity.get(0);
		String topLine = createSvgLineString("top", "0", height, width, height, topStroke, topStrokeWidth, topOpacity);
		rectSvgString += indentation + topLine;

		String rightStroke = stroke.get(1);
		Double rightStrokeWidth = strokeWidth.get(1);
		Double rightOpacity = strokeOpacity.get(1);
		String rightLine = createSvgLineString("right", width, height, width, "0", rightStroke, rightStrokeWidth,
				rightOpacity);
		rectSvgString += indentation + rightLine;

		String bottomStroke = stroke.get(2);
		Double bottomStrokeWidth = strokeWidth.get(2);
		Double bottomOpacity = strokeOpacity.get(2);
		String bottomLine = createSvgLineString("bottom", width, "0", "0", "0", bottomStroke, bottomStrokeWidth,
				bottomOpacity);
		rectSvgString += indentation + bottomLine;

		String leftStroke = stroke.get(3);
		Double leftStrokeWidth = strokeWidth.get(3);
		Double leftOpacity = strokeOpacity.get(3);
		String leftLine = createSvgLineString("left", "0", "0", "0", height, leftStroke, leftStrokeWidth, leftOpacity);
		rectSvgString += indentation + leftLine;
		
		return rectSvgString;
	}

	private String addRectangleForShowingFill(String width, String height, List<Double> fillRadius,
			String initialRectSvgString) {
		String rectSvgString = initialRectSvgString;
		String rectString = "<rect width=\"" + width + "\" height=\"" + height + "\"";		
		rectString = addFillRadius(fillRadius, rectString);
		rectString = rectString + "/>\n";
		rectSvgString += indentation + rectString;
		return rectSvgString;
	}

	private String addFillRadius(List<Double> fillRadius, String initialRectString) {
		String rectString = initialRectString;
		boolean hasFillRadius = fillRadius != null && fillRadius.size() > 0;
		if (hasFillRadius) {
			Double radius = fillRadius.get(0);
			rectString = rectString + " rx=\"" + radius + "\" ry=\"" + radius + "\"";
		}
		return rectString;
	}

	private <T> List<T> prepareArray(List<T> initialValueList) {
		List<T> valueList = initialValueList;
		if (valueList == null) {
			valueList = Arrays.asList(null, null, null, null);
		} else if (valueList.size() == 1) {
			T firstValue = valueList.get(0);
			valueList.add(firstValue);
			valueList.add(firstValue);
			valueList.add(firstValue);
		}
		return valueList;
	}

	private String createRectWithHomogeneousBorder(
			String width,
			String height,
			List<Double> fillRadius,
			boolean hasFillRadius,
			List<Double> strokeRadius,
			boolean hasStrokeRadius,
			String rectGeometryString) {

		String rectSvgString = rectGeometryString;

		String fillRadiusString = "";
		if (hasFillRadius) {
			Double r = fillRadius.get(0);
			fillRadiusString = " rx=\"" + r + "\" ry=\"" + r + "\"";
		}

		String rectString = "<rect width=\"" + width + "\" height=\"" + height + "\"" + fillRadiusString + "/>\n";
		rectSvgString += indentation + rectString;

		if (hasStrokeRadius) {
			//add extra rect with transparent fill to show the border
			Double r = strokeRadius.get(0);
			String strokeRadiusString = "fill=\"transparent\" rx=\"" + r + "\" ry=\"" + r + "\"";

			String extraRectString = "<rect width=\"" + width + "\" height=\"" + height + "\"" + strokeRadiusString
					+ "/>\n";
			rectSvgString += indentation + extraRectString;
		}
		return rectSvgString;
	}

	private static String createPathGeometryString(String pathShape) {
		String shapeGeometryString = " d=\"" + pathShape + "\"/>\n\n";
		return shapeGeometryString;
	}

	@SuppressWarnings("checkstyle:parameternumber")
	private static String createSvgLineString(
			String id,
			String x1,
			String y1,
			String x2,
			String y2,
			String stroke,
			Double strokeWidth,
			Double opacity) {

		String styleString = "style =\"";

		if (stroke != null) {
			styleString += "stroke:" + stroke + ";";
		}

		if (strokeWidth != null) {
			styleString += "stroke-width:" + strokeWidth + ";";
		}

		if (opacity != null) {
			styleString += "opacity:" + opacity + ";";
		}

		styleString += "\"";

		//avoid empty styles
		if (styleString.equals("style=\"\"")) {
			styleString = "";
		}

		String lineSvgString = "<line id=\"" + id + "\" x1=\"" + x1 + "\" y1=\"" + y1 + "\"  x2=\"" + x2 + "\" y2=\""
				+ y2 + "\" " + styleString + "/>\n";
		return lineSvgString;
	}

	/**
	 * Extracts the background radiuses
	 *
	 * @param backGround
	 * @return
	 */
	private static List<Double> backgroundToFillRadiuses(Background backGround) {
		Objects.requireNonNull(backGround, "Background must not be null");
		List<BackgroundFill> fills = backGround.getFills();

		BackgroundFill backgroundFill = fills.get(0);
		CornerRadii cornerRadii = backgroundFill.getRadii();
		List<Double> radii = new ArrayList<>();

		Double firstRadius = cornerRadii.getTopLeftHorizontalRadius();
		radii.add(firstRadius);
		//topLeftVertical is ignored

		Double secondRadius = cornerRadii.getTopRightHorizontalRadius();
		radii.add(secondRadius);
		//topRightVertical is ignored

		Double thirdRadius = cornerRadii.getBottomRightHorizontalRadius();
		radii.add(thirdRadius);
		//bottomRightVertical is ignored

		Double fourthRadius = cornerRadii.getBottomLeftHorizontalRadius();
		radii.add(fourthRadius);
		//bottomLeftVertical is ignored

		//check for equal values and default value and maybe condense to a single value
		Set<Double> radiiSet = new HashSet<Double>(radii);
		boolean hasEqualRadii = radiiSet.size() == 1;
		if (hasEqualRadii) {
			Double radius = radii.get(0);
			boolean isDefaultRadius = radius.equals(0.0);
			if (isDefaultRadius) {
				return null;
			} else {
				radii.clear();
				radii.add(radius);
			}
		}

		return radii;
	}

	/**
	 * Extracts color strings from the given border.
	 *
	 * @param border
	 * @return
	 */
	private static List<String> borderToColorStrings(Border border) {
		Objects.requireNonNull(border, "Border must not be null.");

		List<String> strokeColors = new ArrayList<>();
		List<BorderStroke> strokes = border.getStrokes();
		for (BorderStroke borderStroke : strokes) {
			Paint topStroke = borderStroke.getTopStroke();
			String topStrokeColor = paintToColorString(topStroke);
			strokeColors.add(topStrokeColor);

			Paint rightStroke = borderStroke.getRightStroke();
			String rightStrokeColor = paintToColorString(rightStroke);
			strokeColors.add(rightStrokeColor);

			Paint bottomStroke = borderStroke.getBottomStroke();
			String bottomStrokeColor = paintToColorString(bottomStroke);
			strokeColors.add(bottomStrokeColor);

			Paint leftStroke = borderStroke.getLeftStroke();
			String leftStrokeColor = paintToColorString(leftStroke);
			strokeColors.add(leftStrokeColor);
		}

		//check for equal values and default value and maybe condense to a single value
		Set<String> colorSet = new HashSet<String>(strokeColors);
		boolean hasEqualColor = colorSet.size() == 1;
		if (hasEqualColor) {
			String color = strokeColors.get(0);
			boolean hasDefaultColor = color.equals("#FFFFFF");
			if (hasDefaultColor) {
				return null;
			} else {
				strokeColors.clear();
				strokeColors.add(color);
			}
		}

		return strokeColors;

	}

	/**
	 * Extracts radii from the given border.
	 *
	 * @param border
	 * @return
	 */
	private static List<Double> borderToStrokeRadii(Border border) {
		Objects.requireNonNull(border, "Border must not be null.");

		List<Double> strokeRadii = new ArrayList<>();

		List<BorderStroke> strokes = border.getStrokes();
		BorderStroke borderStroke = strokes.get(0);
		CornerRadii radii = borderStroke.getRadii();

		double topLeftHorizontalRadius = radii.getTopLeftHorizontalRadius();
		strokeRadii.add(topLeftHorizontalRadius);
		//double topLeftVerticalRadius = radii.getTopLeftVerticalRadius();
		//strokeRadii.add(topLeftVerticalRadius);

		double topRightHorizontalRadius = radii.getTopRightHorizontalRadius();
		strokeRadii.add(topRightHorizontalRadius);
		//double topRightVerticalRadius = radii.getTopRightVerticalRadius();
		//strokeRadii.add(topLeftHorizontalRadius);

		double bottomRightHorizontalRadius = radii.getBottomRightHorizontalRadius();
		strokeRadii.add(bottomRightHorizontalRadius);
		//double bottomRightVerticalRadius = radii.getBottomRightVerticalRadius();
		//strokeRadii.add(bottomRightVerticalRadius);

		double bottomLeftHorizontalRadius = radii.getBottomLeftHorizontalRadius();
		strokeRadii.add(bottomLeftHorizontalRadius);
		//double bottomLeftVerticalRadius = radii.getBottomLeftVerticalRadius();
		//strokeRadii.add(bottomLeftVerticalRadius);

		//check for equal values and default value and maybe condense to a single value
		Set<Double> radiiSet = new HashSet<Double>(strokeRadii);
		boolean hasEqualRadii = radiiSet.size() == 1;
		if (hasEqualRadii) {
			Double radius = strokeRadii.get(0);
			boolean isDefaultRadius = radius.equals(0.0);
			if (isDefaultRadius) {
				return null;
			} else {
				strokeRadii.clear();
				strokeRadii.add(radius);
			}
		}

		return strokeRadii;

	}

	/**
	 * Extracts stroke widths from the given border.
	 *
	 * @param border
	 * @return
	 */
	private static List<Double> borderToStrokeWidths(Border border) {
		Objects.requireNonNull(border, "Border must not be null.");

		List<Double> strokeWidths = new ArrayList<>();

		List<BorderStroke> strokes = border.getStrokes();
		BorderStroke borderStroke = strokes.get(0);

		BorderWidths borderWidths = borderStroke.getWidths();

		double topWidth = borderWidths.getTop();
		strokeWidths.add(topWidth);

		double rightWidth = borderWidths.getRight();
		strokeWidths.add(rightWidth);

		double bottomWidth = borderWidths.getBottom();
		strokeWidths.add(bottomWidth);

		double leftWidth = borderWidths.getLeft();
		strokeWidths.add(leftWidth);

		//check for equal values and default value and maybe condense to a single value
		Set<Double> widthsSet = new HashSet<Double>(strokeWidths);
		boolean hasEqualWidths = widthsSet.size() == 1;
		if (hasEqualWidths) {
			Double width = strokeWidths.get(0);
			boolean isDefaultWidth = width.equals(1.0);
			if (isDefaultWidth) {
				return null;
			} else {
				strokeWidths.clear();
				strokeWidths.add(width);
			}
		}

		return strokeWidths;

	}

	/**
	 * Extracts the opacities from the given border.
	 *
	 * @param border
	 * @return
	 */
	private static List<Double> borderToStrokeOpacites(Border border) {
		Objects.requireNonNull(border, "Border must not be null.");

		List<Double> strokeOpacities = new ArrayList<>();
		List<BorderStroke> strokes = border.getStrokes();
		BorderStroke borderStroke = strokes.get(0);

		Paint topStroke = borderStroke.getTopStroke();
		double topOpacity = paintToOpacity(topStroke);
		strokeOpacities.add(topOpacity);

		Paint rightStroke = borderStroke.getRightStroke();
		double rightOpacity = paintToOpacity(rightStroke);
		strokeOpacities.add(rightOpacity);

		Paint bottomStroke = borderStroke.getBottomStroke();
		double bottomOpacity = paintToOpacity(bottomStroke);
		strokeOpacities.add(bottomOpacity);

		Paint leftStroke = borderStroke.getLeftStroke();
		double leftOpacity = paintToOpacity(leftStroke);
		strokeOpacities.add(leftOpacity);

		//check for equal values and default value and maybe condense to a single value
		Set<Double> opactitySet = new HashSet<Double>(strokeOpacities);
		boolean hasEqualWidths = opactitySet.size() == 1;
		if (hasEqualWidths) {
			Double opacity = strokeOpacities.get(0);
			boolean isDefaultOpacity = opacity.equals(1.0);
			if (isDefaultOpacity) {
				return null;
			} else {
				strokeOpacities.clear();
				strokeOpacities.add(opacity);
			}
		}

		return strokeOpacities;

	}

	private static double paintToOpacity(Paint topStroke) {
		Color topStrokeColor = (Color) topStroke;
		double topOpacity = topStrokeColor.getOpacity();
		return topOpacity;
	}

	//#end region

	
}
