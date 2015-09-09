package org.treez.results.javafxchart.svgconverter;

import org.apache.log4j.Logger;
import org.treez.results.javafxchart.svgconverter.converters.NodeToSvgConverter;

import javafx.scene.Node;

/**
 * Converts a JavaFx node to an SVG string
 */
public final class JavaFxNodeToSvgConverter {

	/**
	 * Logger for this class
	 */
	@SuppressWarnings("unused")
	private static Logger sysLog = Logger.getLogger(JavaFxNodeToSvgConverter.class);

	
	//#region CONSTRUCTORS

	/**
	 * Private Constructor to prevent construction
	 */
	private JavaFxNodeToSvgConverter() {}

	//#end region

	//#region METHODS

	/**
	 * Converts a JavaFx Node to an SVG String
	 *
	 * @param node
	 * @return
	 */
	public static String nodeToSvg(Node node) {

		String svgString = createSvgHeader();

		NodeToSvgConverter nodeConverter = new NodeToSvgConverter();
		String initialIndentation = "    ";
		nodeConverter.setIndentation(initialIndentation);

		svgString = nodeConverter.extendCode(svgString, node);

		String endString = "</svg>";
		svgString = svgString + endString;

		return svgString;

	}

	/**
	 * Creates the SVG header
	 *
	 * @return
	 */
	private static String createSvgHeader() {

		String svgHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" + "<svg\n"
				+ "    xmlns:svg=\"http://www.w3.org/2000/svg\"\n" + "    xmlns=\"http://www.w3.org/2000/svg\"\n"
				+ ">\n";
		return svgHeader;
	}


	//#end region

	//#region ACCESSORS

	//#end region

}
