package org.treez.results.javafxchart.svgconverter;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * Demonstrates the conversion of a JavaFx Node to SVG code
 * 
 * @author Stefan Eidelloth
 *
 */
@SuppressWarnings("checkstyle:uncommentedmain")
public class JavaFxNodeToSvgConverterDemo extends Application {

	/**
	 * Logger for this class
	 */
	private static Logger sysLog = Logger.getLogger(JavaFxNodeToSvgConverterDemo.class);
	
	
	//#region ATTRIBUTES
		
	/**
	 * True: use a Chart example, False: use a custom example
	 */
	private static final Boolean USE_CHART_EXAMPLE = true;
	
	//#end region
	
	//#region METHODS
		
	/**
	 * Main
	 * @param args
	 */	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	@SuppressWarnings("checkstyle:regexpsinglelinejava")
	public void start(Stage stage) {
		
		initializeLog4jLogging();
				
		stage.setTitle("JavaFxNodeToSvg Example");

		//create example node that will be converted
		Parent rootNode;
		if (USE_CHART_EXAMPLE) {	
			rootNode = createChartExample();
		} else {
			rootNode = createCustomExample();
		}

		//show scene
		showScene(stage, rootNode);
	
		//convert rootNode to SVG code
		String svgString = JavaFxNodeToSvgConverter.nodeToSvg(rootNode);

		//write SVG code to console
		System.out.println(svgString);
		
		//write SVG code to text file
		String outputFolderPath = getURIPathToFolder("output");		
		String outputFilePath = outputFolderPath + "/output.svg";
		
		File outputFile = new File(outputFilePath);
		try {
			FileUtils.writeStringToFile(outputFile, svgString);
		} catch (IOException exception) {
			sysLog.error("Could not write text file", exception);

		}

	}
	
	/**
	 * Creates a JavaFx node example that contains a Chart
	 * @return
	 */
	@SuppressWarnings("checkstyle:magicnumber")
	private Parent createChartExample() {
		
		Parent rootNode;
		
		//define axes
		final NumberAxis xAxis = new NumberAxis();
		xAxis.setLabel("Number of Month");
		xAxis.setAnimated(false);
		
		final NumberAxis yAxis = new NumberAxis();
		yAxis.setLabel("Stock");
		yAxis.setAnimated(false);

		//creating line chart
		LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis);
		lineChart.setTitle("Stock Monitoring, 2010");
		lineChart.setAnimated(false);
		
		//define a series
		XYChart.Series<Number, Number> series = new XYChart.Series<>();
		series.setName("My portfolio");		
		
		//populate the series with data
		series.getData().add(new XYChart.Data<Number, Number>(1.0, 23.0));
		series.getData().add(new XYChart.Data<Number, Number>(12.0, 44.0));
		
		//add series to line chart
		lineChart.getData().add(series);

		rootNode = lineChart;
		return rootNode;
	}
	

	/**
	 * Creates a custom JavaFx node example
	 * @return
	 */
	@SuppressWarnings("checkstyle:magicnumber")
	private Parent createCustomExample() {
		
		Parent node;
		
		StackPane nodePane = new StackPane();
		nodePane.setId("node");
		nodePane.setStyle("-fx-background-color: white;");

		StackPane pane = createColoredStackPane();		
		List<Node> paneChildren = pane.getChildren();
		nodePane.getChildren().add(pane);
		
		Group group = createGroup();
		List<Node> groupChildren = group.getChildren();
		paneChildren.add(group);

		Rectangle smallRectangle = new Rectangle(10, 10);
		smallRectangle.setId("smallrectangle");
		smallRectangle.setFill(Color.BLUE);
		smallRectangle.resizeRelocate(0, 0, 1, 1);		

		paneChildren.add(smallRectangle);
		
		Line line = new Line(0, 0, 50, 50);
		groupChildren.add(line);

		Label label = new Label("hello");		
		groupChildren.add(label);

		node = nodePane;
		return node;
	}

	private StackPane createColoredStackPane() {
		StackPane pane = new StackPane();		
		pane.setId("pane");
		
		final int paneSize = 300;
		pane.setMaxWidth(paneSize);
		pane.setMaxHeight(paneSize);
		
		String style = "-fx-background-color:#FF0000;";
		pane.setStyle(style);
		return pane;
	}
	

	@SuppressWarnings("checkstyle:magicnumber")
	private Group createGroup() {
		Group group = new Group();
		group.setId("Group");
		group.setTranslateX(100.0);		
		group.setTranslateY(100.0);
		return group;
	}
	
	/**
	 * Shows a JavaFx Scene containing the given root node
	 * 
	 * @param stage
	 * @param rootNode
	 */
	private void showScene(Stage stage, Parent rootNode) {
		final int sceneSize = 600;
		Scene scene = new Scene(rootNode, sceneSize, sceneSize);
		stage.setScene(scene);
		stage.show();
	}

	/**
	 * Returns the URI for the given relative path in the project folder
	 * @param relativePath
	 * @return
	 */
	public static String getURIPathToFolder(String relativePath){
		URL binUrl = JavaFxNodeToSvgConverterDemo.class.getClassLoader().getResource(".");
		try {
			URI binUri = binUrl.toURI();
			URI wantedUri = binUri.resolve("../" + relativePath);	
			File file = new File(wantedUri);
			String wantedFilePath = file.getAbsolutePath();
			return wantedFilePath;
		} catch (URISyntaxException exception) {
			throw new IllegalStateException("Could not find path '" + relativePath + "'");
		}
	}
	
	/**
	 * Initializes the log4j logging
	 */
	public static void initializeLog4jLogging() {
		URL binUrl = JavaFxNodeToSvgConverterDemo.class.getClassLoader().getResource(".");
		try {
			URI binUri = binUrl.toURI();
			URI log4jUri = binUri.resolve("../META-INF/log4j.properties");
			try {
				URL log4jUrl = log4jUri.toURL();
				PropertyConfigurator.configure(log4jUrl);
			} catch (MalformedURLException e) {
				throw new IllegalStateException("Could not initialize logging");
			}

		} catch (URISyntaxException e) {
			throw new IllegalStateException("Could not initialize logging");
		}

	}
	
	//#end region

}
