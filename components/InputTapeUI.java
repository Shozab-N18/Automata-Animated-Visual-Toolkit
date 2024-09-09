package components;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.InputTape;

/**
 * InputTapeUI class is responsible for displaying the input tape on the GUI during the simulation with variable colours/styles.
 */
public class InputTapeUI extends GridPane {
    private static final String TAPE_HEAD_SYMBOL = "\u25BC";

    private InputTape inputTape;
    private String[] tape;
    
    public InputTapeUI() {
        getStylesheets().add(
            getClass().getResource("/css/AutomatonSimulation.css").toExternalForm()
        );
        getStyleClass().add("tapeGrid");
        
        setAlignment(Pos.CENTER);
        setPrefHeight(150);
        setPadding(new Insets(10, 10, 10, 10));
    }

    public void clearTapeView() {
        getChildren().clear();
    }
    
    private Label addEmptyCell(int column) {
        return addCell("", column);
    }

    private void addHeadSymbol(int columnIndex, int rowIndex) {
        Label headSymbol = new Label(TAPE_HEAD_SYMBOL);
        headSymbol.getStyleClass().add("head");
        add(headSymbol, columnIndex, rowIndex);
        
        GridPane.setHalignment(headSymbol, HPos.CENTER);
    }

    private Label addCell(String symbol, int column) {
        Label newLabel = new Label(symbol);
        
        if (inputTape != null && column - 1 == inputTape.getHeadPosition()) {
            newLabel.getStyleClass().add("headCell");
            addHeadSymbol(column, 0);
        } else {
            newLabel.getStyleClass().add("tapeCell");
        }
        
        add(newLabel, column, 1);
        return newLabel;
    }

    /**
     * Updates the tape view with the new input tape provided.
     * @param inputTape The input tape to be displayed.
     */
    public void updateTapeView(InputTape inputTape) {
        getChildren().clear();
        
        this.inputTape = inputTape;
        tape = inputTape.getTape();
        
        Label startingCell = addEmptyCell(0);
        
        for (int i = 0; i < tape.length - 1; i++) {
            addCell(tape[i], i + 1);
        }
        
        addEmptyCell(tape.length);
        
        if (inputTape.getHeadPosition() == tape.length) {
            addHeadSymbol(0, 0);
            startingCell.getStyleClass().add("headCell");
        }
    }

    public void initialiseInputTapeView(TextField inputWordField) {
        inputTape = null;
        String[] symbols = inputWordField.getText().split("");
        
        Label startingCell = addEmptyCell(0);
        startingCell.getStyleClass().add("headCell");
        addHeadSymbol(0, 0);
        
        for (int i = 0; i < symbols.length; i++) {
            addCell(symbols[i], i + 1);
        }
        
        addEmptyCell(symbols.length + 1);
    }

    public void showRejectingTapeView(InputTape inputTape) {
        getChildren().clear();
        
        this.inputTape = inputTape;
        tape = inputTape.getTape();
        
        addEmptyCell(0);
        
        for (int i = 0; i < tape.length - 1; i++) {
            Label newLabel = addCell(tape[i], i + 1);
            if (i == inputTape.getHeadPosition()) {
                newLabel.getStyleClass().clear();
                newLabel.getStyleClass().add("rejectedCell");
            }
        }
        
        Label finalCell = addEmptyCell(tape.length + 1);
        
        if (inputTape.getHeadPosition() == tape.length - 1) {
            finalCell.getStyleClass().clear();
            finalCell.getStyleClass().add("rejectedCell");
            addHeadSymbol(tape.length + 1, 0);
        }
    }
}
