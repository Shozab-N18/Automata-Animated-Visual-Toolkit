package view;


import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.collections.*;
import javafx.beans.property.*;
import javafx.scene.layout.HBox;

import java.text.DecimalFormat;
import java.util.regex.Pattern;

import model.*;
import components.*;
import controller.SimulatorController;

import de.jensd.fx.glyphs.GlyphsDude;   
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;

/*
 * AutomataSimulatorView: View class for the automata simulator interface.
 */
public class AutomataSimulatorView extends BorderPane {
    private SimulatorController controller;
    private SimulatorCanvas canvas;

    private VBox controlPanel;   
    private Label automataLabel;
    private Label inputWordLabel;
    private static TextField inputWordField;
    private Button simulateBtn;
    private Button findAcceptingPathBtn;
    private Button simulateRandomPathBtn;
    
    private Slider slider;
    private Label valueLabel;
    private TableView<ObservableList<StringProperty>> simulationLog;
    private Button clearLogButton;
    
    private ScrollPane canvasScrollPane;
    private SplitPane centerSplitPane;
    
    private Button playButton;
    private Button pauseButton;
    private Button nextButton;
    private Button previousButton;
    private Button stopButton;
    
    private HBox simulationControls;

    private static InputTapeUI inputTapeUI;
    
    // Constructor
    public AutomataSimulatorView(Automata automata) {
        this.getStylesheets().add(
            getClass().getResource("/css/AutomatonSimulation.css").toExternalForm()
        );
        
        controller = new SimulatorController(automata);
        canvas = new SimulatorCanvas(controller);
        
        initialiseComponents();
        layoutComponents();
        initialiseEventHandlers();
    }
    
    private void initialiseComponents() {
        automataLabel = new Label("Automata Simulator");
        controlPanel = new VBox(10);
        inputWordLabel = new Label("Enter input word:");
        inputWordField = new TextField();
        simulateBtn = new Button("Simulate");
        findAcceptingPathBtn = new Button("Find accepting path");
        simulateRandomPathBtn = new Button("Simulate random path");
        
        slider = new Slider(0.01, 2, 0.5);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(4);
        slider.setBlockIncrement(0.1);
        
        valueLabel = new Label("Seconds per step: " + formatValue(slider.getValue()));
        simulationLog = controller.getSimulationLog();
        clearLogButton = new Button("Clear Log");
        centerSplitPane = new SplitPane();
        
        inputTapeUI = new InputTapeUI();
        
        playButton = GlyphsDude.createIconButton(FontAwesomeIcon.PLAY);
        pauseButton = GlyphsDude.createIconButton(FontAwesomeIcon.PAUSE);
        nextButton = GlyphsDude.createIconButton(FontAwesomeIcon.ARROW_CIRCLE_RIGHT);
        previousButton = GlyphsDude.createIconButton(FontAwesomeIcon.ARROW_CIRCLE_LEFT);
        stopButton = GlyphsDude.createIconButton(FontAwesomeIcon.STOP);
        
        simulationControls = new HBox(10);
        
        controlPanel.getStyleClass().add("controlPanel");
        automataLabel.setId("header");
        inputWordField.getStyleClass().add("text-field");
    }
    
    private void layoutComponents() {
        setAlignment(automataLabel, Pos.CENTER);
        setTop(automataLabel);
        
        Insets margin = new Insets(5, 20, 5, 20);
        
        VBox.setMargin(inputWordLabel, new Insets(20, 20, 5, 20));
        VBox.setMargin(inputWordField, margin);
        VBox.setMargin(simulateBtn, margin);
        VBox.setMargin(slider, margin);
        VBox.setMargin(valueLabel, margin);
        VBox.setMargin(simulationLog, margin);
        VBox.setMargin(clearLogButton, margin);
        VBox.setMargin(simulationControls, margin);
        VBox.setMargin(findAcceptingPathBtn, margin);
        VBox.setMargin(simulateRandomPathBtn, margin);
        
        clearLogButton.setAlignment(Pos.CENTER);
        
        simulationControls.getChildren().addAll(playButton, pauseButton, previousButton, nextButton, stopButton);
        
        controlPanel.getChildren().addAll(
            inputWordLabel, inputWordField, simulateBtn, findAcceptingPathBtn, simulateRandomPathBtn, slider, 
            valueLabel, simulationLog, clearLogButton, simulationControls
        );
        
        canvasScrollPane = new ScrollPane(canvas);
        canvasScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        canvasScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        
        centerSplitPane.getItems().addAll(controlPanel, canvasScrollPane);
        
        inputTapeUI.initialiseInputTapeView(inputWordField);
        
        ScrollPane tapeScrollPane = new ScrollPane(inputTapeUI);
        tapeScrollPane.setFitToHeight(true);
        tapeScrollPane.setFitToWidth(true); 
        
        SplitPane bottomSplitPane = new SplitPane();
        bottomSplitPane.setOrientation(Orientation.VERTICAL);
        bottomSplitPane.getItems().addAll(centerSplitPane, tapeScrollPane);
        bottomSplitPane.setDividerPositions(0.8);
        
        setCenter(bottomSplitPane);
    }
    
    public void initialiseEventHandlers() {
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            valueLabel.setText("Seconds per step: " + formatValue(newValue.doubleValue()));
            controller.setDelay(newValue.doubleValue());
        });
        
        simulateBtn.setOnAction(e -> {
            simulateBtn.setDisable(true);
            inputWordField.setDisable(true);
            controller.setOnSimulationFinished(() -> {
                simulateBtn.setDisable(false);
                inputWordField.setDisable(false);
            });
            
            String inputWord = inputWordField.getText();
            controller.simulate(inputWord);
            simulationLog.setItems(controller.getSimulationLog().getItems());
        });
        
        centerSplitPane.widthProperty().addListener(e -> {
            double pixelValue = 240;
            centerSplitPane.setDividerPositions(pixelValue / centerSplitPane.getWidth(), 1.0 - (pixelValue / centerSplitPane.getWidth()));
        });
        
        inputWordField.textProperty().addListener((observable, oldValue, newValue) -> {
            StringBuilder alphabetPatternBuilder = new StringBuilder("[");
            for (String character : controller.getAutomata().getAlphabet()) {
                alphabetPatternBuilder.append(Pattern.quote(character));
            }
            alphabetPatternBuilder.append("]*");
            
            String alphabetPattern = alphabetPatternBuilder.toString();
            
            // If the new value is not a valid input word, revert to the old value
            if (!newValue.matches(alphabetPattern)) {
                inputWordField.setText(oldValue);
            } else { 
                inputTapeUI.clearTapeView();
                inputTapeUI.initialiseInputTapeView(inputWordField);
            }
        });
        
        this.setOnKeyPressed(event -> {
            if (event.isControlDown()) { 
                KeyCode keyCode = event.getCode();
                if (keyCode == KeyCode.LEFT) {
                    controller.previous();
                } else if (keyCode == KeyCode.RIGHT) { 
                    controller.next();
                }
            }
        });
        
        this.requestFocus();
        
        findAcceptingPathBtn.setOnAction(e -> controller.findAcceptingPath(inputWordField.getText()));
        simulateRandomPathBtn.setOnAction(e -> controller.simulateRandomPath());
        clearLogButton.setOnAction(e -> controller.clearSimulationLog());
        
        playButton.setOnAction(e -> controller.play());
        pauseButton.setOnAction(e -> controller.pause());
        nextButton.setOnAction(e -> controller.next());
        previousButton.setOnAction(e -> controller.previous());
        stopButton.setOnAction(e -> controller.stop());
    }
    
    /*
     * updateAutomata: Update the automaton being simulated.
     */
    public void updateAutomata(Automata automata) {
        controller.updateAutomata(automata);
    }
    
    // Format a double value to two decimal places
    private String formatValue(double value) {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(value);
    }
    
    /**
     * Update the input tape view. Used by SimulatorController to update the input tape view.
     * @param inputTape
     */
    public static void updateTapeView(InputTape inputTape) {
        inputTapeUI.updateTapeView(inputTape);
    }
    
    /**
     * Show the tape view for a rejecting input. Used by SimulatorController to update the tape view
     * for a rejecting input.
     * @param inputTape 
     */
    public static void showRejectingTapeView(InputTape inputTape) {
        inputTapeUI.showRejectingTapeView(inputTape);
    }
    
    /* 
     * Clear the input word field. Used by AutomataConstructionView to clear the input 
     * word field when the automaton's alphabet is changed.
     */
    public static void clearInputWordField() {
        inputWordField.setText("");
    }
}