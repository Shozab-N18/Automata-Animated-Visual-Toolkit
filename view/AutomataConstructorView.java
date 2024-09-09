package view;

import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;

import java.util.Optional;

import components.ConstructionCanvas;
import components.MainCanvas;
import components.serialization.SerializablePoint2D;
import controller.ConstructionController;
import model.*;

/**
 * AutomataConstructorView: View class for the automata construction interface.
 */
public class AutomataConstructorView extends BorderPane {
    private static Label automataValidityLabel;
    private Label automataLabel;
    private Label automataTypeLabel;
    private Label coordinatesLabel;
    
    private ComboBox<String> alphabetComboBox;
    private TextField customAlphabetField;
    
    private static ConstructionController controller;
    private TableView<ObservableList<StringProperty>>  transitionTable;
    private boolean showTable = false;
    
    private static ToggleButton nfaButton;
    private static ToggleButton dfaButton;
    
    private static boolean isDFA = false;
    private ConstructionCanvas canvas;
    
    private Button addStateBtn;
    private Button showTableBtn;
    private Button addTransitionBtn;
    
    private ScrollPane tableScrollPane;
    private VBox controlPanel;
    private HBox alphabetBox;
    private HBox automataTypeBox;
    
    private ScrollPane scrollCanvas;
    private SplitPane splitPane;
    
    // Constructor
    public AutomataConstructorView(Automata automata) {
        getStylesheets().add(
            getClass().getResource("/css/AutomataConstruction.css").toExternalForm()
        );
        
        controller = new ConstructionController(automata);
        transitionTable = (controller.getTransitionTable());
        transitionTable.setEditable(true);
        
        canvas = new ConstructionCanvas(controller);
        
        initialiseComponents();
        layoutComponents();
        initialiseEventHandlers();
    }
    
    public void initialiseComponents() {
        tableScrollPane = new ScrollPane(transitionTable);
        tableScrollPane.setFitToWidth(true);
        
        controlPanel = new VBox(10);
        controlPanel.setId("controlPanel");
        
        automataLabel = new Label("Automata Construction ");
        automataLabel.setId("header"); 
        
        addStateBtn = new Button("Add State");
        showTableBtn = new Button("Show Transition Table");
        addTransitionBtn = new Button("Add Transition");
        
        customAlphabetField = new TextField();
        customAlphabetField.setPromptText("e.g. abc01");
        customAlphabetField.setDisable(true);
        
        alphabetComboBox = new ComboBox<>();
        alphabetComboBox.getItems().addAll("a-b", "a-c", "0-9", "0-1" , "A-Z", "a-z", "Custom", "#/-+{}()*=");
        alphabetComboBox.setValue("a-b");
        
        alphabetBox = new HBox(10);
        
        automataTypeLabel = new Label("Type: NFA");
        automataTypeLabel.setId("automataTypeLabel");
        
        automataValidityLabel = controller.getAutomataValidityLabel();
        automataValidityLabel.setId("automataValidityLabel");
        
        nfaButton = new ToggleButton("NFA");
        dfaButton = new ToggleButton("DFA");
        
        nfaButton.setSelected(true);
        dfaButton.setSelected(false);
        
        automataTypeBox = new HBox(10);
        scrollCanvas = new ScrollPane(canvas);
        splitPane = new SplitPane();
        
        coordinatesLabel = canvas.getCoordinatesLabel();
        coordinatesLabel.setId("coordinatesLabel");
    }
    
    public void layoutComponents() {
        setAlignment(automataLabel, Pos.CENTER);
        setTop(automataLabel);
        
        alphabetBox.getChildren().addAll(alphabetComboBox, customAlphabetField);
        
        automataTypeBox.getChildren().addAll(dfaButton, nfaButton);
        
        Insets margin = new Insets(5, 20, 5, 20);
        
        VBox.setMargin(addStateBtn, new Insets(20, 20, 5, 20));
        VBox.setMargin(addTransitionBtn, margin);  
        VBox.setMargin(tableScrollPane, margin);
        VBox.setMargin(showTableBtn, margin);
        VBox.setMargin(alphabetBox, margin);
        VBox.setMargin(automataTypeLabel, margin);
        VBox.setMargin(automataValidityLabel, margin);
        VBox.setMargin(automataTypeBox, margin);
        
        controlPanel.getChildren().addAll(
            addStateBtn, addTransitionBtn, showTableBtn,
            alphabetBox, automataTypeBox, automataTypeLabel, automataValidityLabel
        );
        
        scrollCanvas.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollCanvas.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        
        splitPane.getItems().addAll(controlPanel, scrollCanvas);
        
        setCenter(splitPane);
        
        BorderPane.setAlignment(coordinatesLabel, Pos.BOTTOM_RIGHT);
        setBottom(coordinatesLabel);
    }
    
    public void initialiseEventHandlers() {
        alphabetComboBox.setOnAction(e -> {
            String selectedOption = alphabetComboBox.getValue();
            customAlphabetField.setDisable(!selectedOption.equals("Custom"));
            updateAlphabet();
        });
        
        customAlphabetField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateAlphabet();
        });
        
        addStateBtn.setOnAction(e -> {
            controller.addState(new SerializablePoint2D());
            MainCanvas.drawAllCanvases();
        });
        
        addTransitionBtn.setOnAction(e -> {
            TextInputDialog addTransitionDialog = new TextInputDialog();
            addTransitionDialog.setTitle("Add Transition");
            addTransitionDialog.setHeaderText(null);
            addTransitionDialog.setContentText("Enter source state name:");
            
            Optional<String> sourceStateResult = addTransitionDialog.showAndWait();
            
            if (sourceStateResult.isPresent() && !sourceStateResult.get().trim().isEmpty()) {
                String sourceStateName = sourceStateResult.get();
                
                addTransitionDialog.getEditor().clear();
                addTransitionDialog.setContentText("Enter target state name:");
                
                Optional<String> targetStateResult = addTransitionDialog.showAndWait();
                
                if (targetStateResult.isPresent() && !targetStateResult.get().trim().isEmpty()) {
                    String targetStateName = targetStateResult.get();
                    
                    addTransitionDialog.getEditor().clear();
                    addTransitionDialog.setContentText("Enter transition symbol:");
                    
                    Optional<String> transitionSymbolResult = addTransitionDialog.showAndWait();
                    
                    if (transitionSymbolResult.isPresent() && !transitionSymbolResult.get().trim().isEmpty()) {
                        String transitionSymbol = transitionSymbolResult.get();
                        
                        State sourceState = controller.getStateByName(sourceStateName);
                        State targetState = controller.getStateByName(targetStateName);
                        
                        if (sourceState != null && targetState != null) {
                            controller.addTransition(sourceState, targetState, transitionSymbol);
                            MainCanvas.drawAllCanvases();
                        } 
                    }
                }
            }
        });
        
        showTableBtn.setOnAction(e -> {
            showTable = !showTable;
            
            if (showTable) {
                controlPanel.getChildren().add(tableScrollPane);
                showTableBtn.setText("Hide Transition Table");
            } else {
                controlPanel.getChildren().remove(tableScrollPane);
                showTableBtn.setText("Show Transition Table");
            }
        });
        
        nfaButton.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                controller.getAutomata().setAsNFA();
                isDFA = controller.getAutomata().isDFA();
                SubsetConstructionView.enableConversionButtons();
                automataTypeLabel.setText("Type: NFA");
                dfaButton.setSelected(false);
                controller.updateAutomataValidityLabel(controller.getAutomata().isValid());
            }
        });
        
        dfaButton.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                controller.getAutomata().setAsDFA();
                isDFA = controller.getAutomata().isDFA();
                SubsetConstructionView.disableConversionButtons();
                automataTypeLabel.setText("Type: DFA");
                nfaButton.setSelected(false);
                controller.updateAutomataValidityLabel(controller.getAutomata().isValid());
            }
        });
        
        splitPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            double pixelValue = 240;
            splitPane.setDividerPositions(pixelValue / splitPane.getWidth(), 1.0 - (pixelValue / splitPane.getWidth()));
        });
    }
    
    /*
     * Updates the automata in the controller.
     */
    public void updateAutomata(Automata automata) {
        controller.updateAutomata(automata);
        controller.updateAutomataValidityLabel(controller.getAutomata().isValid());
    }
    
    public static void setAsDFA() {
        dfaButton.setSelected(true);
        nfaButton.setSelected(false);
    }
    
    public static void setAsNFA() {
        dfaButton.setSelected(false);
        nfaButton.setSelected(true);
    }
    
    private void updateAlphabet() {
        String selectedOption = alphabetComboBox.getValue();
        String alphabetInput;
        
        if (selectedOption.equals("Custom")) {
            alphabetInput = customAlphabetField.getText();
        } else {
            switch (selectedOption) {
                case "a-z":
                    alphabetInput = "abcdefghijklmnopqrstuvwxyz";
                    break;
                case "0-9":
                    alphabetInput = "0123456789";
                    break;
                case "A-Z":
                    alphabetInput = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
                    break;
                case "0-1":
                    alphabetInput = "01";
                    break;
                case "#/-+{}()*=":
                    alphabetInput = "#/-+{}()*=";
                    break;
                case "a-c":
                    alphabetInput = "abc";
                    break;
                case "ab":
                    alphabetInput = "ab";
                    break;
                default:
                    alphabetInput = "ab";
            }
        }
        controller.setAlphabet(alphabetInput);
        AutomataSimulatorView.clearInputWordField();
    }

    public static boolean isDFA() {
        return isDFA;
    }

    public static void clearStates() {
        controller.getAutomata().getStates().clear();
        controller.updateTransitionTable();
    }
}