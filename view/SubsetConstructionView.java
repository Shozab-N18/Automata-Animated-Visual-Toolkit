package view;

import java.util.Map;

import javafx.scene.control.TableView;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import model.State;
import model.Automata;
import components.SubsetConstructionCanvas;
import components.SpecialSymbols;
import components.serialization.SerializablePoint2D;
import controller.SubsetConstructionController;
import app.App; 

/*
 * SubsetConstructionView: View class for the subset construction algorithm interface.
 */
public class SubsetConstructionView extends BorderPane {
    private static Automata nfaAutomata;
    private static Automata dfaAutomata;
    
    private static SubsetConstructionController nfacontroller;
    private static SubsetConstructionController dfacontroller;
    
    private SubsetConstructionCanvas<SubsetConstructionController> nfaCanvas;
    private SubsetConstructionCanvas<SubsetConstructionController> dfaCanvas;
    private static TableView<ObservableList<StringProperty>> nfaTransitionTable;
    private static TableView<ObservableList<StringProperty>> dfaTransitionTable;
    private static Button convertButton;
    private static Button useDFAButton;
    private static Button tryConversionButton;
    private static Button clearDFAButton;
    
    private static VBox nfaDataWrapper;
    private static VBox dfaDataWrapper;
    
    // Constructor
    public SubsetConstructionView(Map<State, SerializablePoint2D> states) {
        this.getStylesheets().add(
            getClass().getResource("/css/SubsetConstruction.css").toExternalForm()
        );
        nfaAutomata = new Automata(states);
        dfaAutomata = new Automata();
        
        nfaAutomata.setAsNFA();
        dfaAutomata.setAsDFA();
        
        initialiseComponents();
        layoutComponents();
        initialiseEventHandlers();
    }
    
    private void initialiseComponents() {
        nfacontroller = new SubsetConstructionController(nfaAutomata);
        dfacontroller = new SubsetConstructionController(dfaAutomata);
        dfacontroller.getAutomata().setAsDFA();
        
        nfaCanvas = new SubsetConstructionCanvas<>(nfacontroller);
        dfaCanvas = new SubsetConstructionCanvas<>(dfacontroller);
        
        nfaTransitionTable = nfacontroller.getTransitionTable();
        dfaTransitionTable = dfacontroller.getTransitionTable();
        
        convertButton = new Button("Convert to DFA");
        useDFAButton = new Button("Use DFA");
        tryConversionButton = new Button("Attempt Conversion");
        clearDFAButton = new Button("Clear DFA");
        
        nfaDataWrapper = new VBox(20);
        dfaDataWrapper = new VBox(20);
        nfaDataWrapper.setId("dataWrapper");
        dfaDataWrapper.setId("dataWrapper");
    }
    
    private void layoutComponents() {
        Label automataLabel = new Label("Subset Constuction Algorithm");
        automataLabel.setId("header");
        setTop(automataLabel);
        setAlignment(automataLabel, Pos.CENTER);
        
        Label nfaLabel = new Label("NFA (N)");
        nfaLabel.setFont(Font.font(18));
        
        Label dfaLabel = new Label("DFA (D)");
        dfaLabel.setFont(Font.font(18));
        
        convertButton.setDisable(nfacontroller.getAutomata().isValid());
        
        ScrollPane nfaScrollPane = new ScrollPane(nfaCanvas);
        nfaScrollPane.setPrefHeight(500);
        nfaScrollPane.setPrefWidth(1000);
        nfaScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        nfaScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        ScrollPane dfaScrollPane = new ScrollPane(dfaCanvas);
        dfaScrollPane.setPrefHeight(500);
        dfaScrollPane.setPrefWidth(1000);
        dfaScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        dfaScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        nfaTransitionTable.setPrefHeight(300);
        nfaTransitionTable.setPrefWidth(500);
        
        dfaTransitionTable.setPrefHeight(300);
        dfaTransitionTable.setPrefWidth(500);
        
        updateAutomatonDataView();
        
        VBox nfaBox = new VBox(20);
        nfaBox.setAlignment(Pos.CENTER);
        nfaBox.getChildren().addAll(nfaLabel, nfaScrollPane, nfaDataWrapper);
        
        VBox dfaBox = new VBox(20);
        dfaBox.setAlignment(Pos.CENTER);
        dfaBox.getChildren().addAll(dfaLabel, dfaScrollPane, dfaDataWrapper);
        
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(convertButton, useDFAButton, tryConversionButton, clearDFAButton);
        
        Insets margin = new Insets(10, 0, 0, 0);
        HBox.setMargin(convertButton, margin);
        HBox.setMargin(useDFAButton, margin);
        HBox.setMargin(tryConversionButton, margin);
        HBox.setMargin(clearDFAButton, margin);
        
        HBox automataBox = new HBox(20);
        automataBox.setAlignment(Pos.CENTER);
        automataBox.getChildren().addAll(nfaBox, dfaBox);
        
        setPadding(new Insets(20));
        setCenter(automataBox);
        setBottom(buttonBox);
    }

    public void initialiseEventHandlers() {
        convertButton.setOnAction(e -> {
            dfaAutomata.setAsDFA();
            nfacontroller.convertToDFA();
            dfaAutomata.setStates(nfacontroller.getDFAStates());
            dfacontroller.updateStates(dfaAutomata.getStates());
            updateAutomatonDataView();
            useDFAButton.setDisable(dfacontroller.getAutomata().getStates().isEmpty());
        });
        
        // Toggle between the NFA or DFA automata in the entire application
        useDFAButton.setOnAction(e -> {
            if (useDFAButton.getText().equals("Use DFA") && !dfaAutomata.getStates().isEmpty()) {
                App.setAppStates(dfaAutomata);
                AutomataConstructorView.setAsDFA(); 
                
                useDFAButton.setText("Use NFA");
            } else if (useDFAButton.getText().equals("Use NFA")) {
                App.setAppStates(nfaAutomata);
                AutomataConstructorView.setAsNFA();
                
                useDFAButton.setText("Use DFA");
            }
        });
        
        tryConversionButton.setOnAction(e -> {
            if (!AutomataConstructorView.isDFA()) {
                nfacontroller.convertToDFA();
                dfaAutomata.setStates(nfacontroller.getDFAStates());
                dfacontroller.attemptConversion(dfaAutomata.getStates(), nfaAutomata.getStates());
                updateAutomatonDataView();
            }
        });
        
        clearDFAButton.setOnAction(e -> {
            dfaAutomata.getStates().clear();
            dfacontroller.updateStates(dfaAutomata.getStates());
            dfacontroller.updateTransitionTable();
            // AutomataConstructorView.clearStates();
            updateAutomatonDataView();
            useDFAButton.setText("Use NFA");
            useDFAButton.setDisable(false);
        });
    }
    
    // Update the data view of the automata for both the NFA and DFA
    public static void updateAutomatonDataView() {
        nfaDataWrapper.getChildren().clear();
        dfaDataWrapper.getChildren().clear();
        
        HBox nfaData = new HBox(50);
        nfaData.setAlignment(Pos.CENTER);
        nfaData.getChildren().addAll(
            new Label("Q: "  + nfacontroller.getStringAllStates()),
            new Label(SpecialSymbols.ALPHABET + ": "  + nfacontroller.getStringAlphabet()), 
            new Label("q0: " + nfacontroller.getStringStartingState()),
            new Label("F: "  + nfacontroller.getStringAllAcceptingStates()),
            new Label(SpecialSymbols.TRANSITION + ": ")
        );
        
        HBox dfaData = new HBox(50);
        dfaData.setAlignment(Pos.CENTER);
        dfaData.getChildren().addAll(
            new Label("Q: " + dfacontroller.getStringAllStates()),
            new Label(SpecialSymbols.ALPHABET + ": " + dfacontroller.getStringAlphabet()),
            new Label("q0: " + dfacontroller.getStringStartingState()),
            new Label("F: " + dfacontroller.getStringAllAcceptingStates()),
            new Label(SpecialSymbols.TRANSITION + ": ")
        );
        
        nfaDataWrapper.setAlignment(Pos.CENTER);
        nfaDataWrapper.getChildren().addAll(nfaData, nfaTransitionTable);
        
        dfaDataWrapper.setAlignment(Pos.CENTER);
        dfaDataWrapper.getChildren().addAll(dfaData, dfaTransitionTable);
    }

    /**
     * Update the corresponding automata based on the automata passed in.
     * @param automata: The automata to update the canvas with.
     */
    public static void updateCorrespondingAutomata(Automata automata) {
        Map<State, SerializablePoint2D> states = automata.getStates();
        if (automata.isDFA()) {
            dfaAutomata.setStates(states);
            dfacontroller.updateStates(states);
        } else {
            nfaAutomata.setStates(states);
            nfacontroller.updateStates(states);
        }
        
        updateAutomatonDataView();
    }
    
    /*
     * Update the alphabet of both the NFA and DFA automata. 
     * Used when the alphabet is changed in the automata constructor.
     */
    public static void updateAlphabetView(String alphabet) {
        dfacontroller.setAlphabet(alphabet);
        nfacontroller.setAlphabet(alphabet);
    }

    public static void disableConversionButtons() {
        convertButton.setDisable(true);
        useDFAButton.setDisable(true);
        tryConversionButton.setDisable(true);
    }

    public static void enableConversionButtons() {
        convertButton.setDisable(false);
        useDFAButton.setDisable(false);
        tryConversionButton.setDisable(false);
    }
}