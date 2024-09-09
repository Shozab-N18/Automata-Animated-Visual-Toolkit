package controller;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import app.App;
import components.MainCanvas;
import components.serialization.SerializablePoint2D;
import components.SpecialSymbols;

import model.*;
import view.SubsetConstructionView;

/*
 * AutomataController: Superclass for all automata controllers
 */
public class AutomataController {
    protected Automata automata;
    protected State selectedState;
    protected State rejectedState;
    protected TableView<ObservableList<StringProperty>> transitionTable;

    // Constructor
    public AutomataController(Map<State, SerializablePoint2D> states) {
        automata = new Automata(states);
        transitionTable = new TableView<>();
    }
    
    // Constructor
    public AutomataController(Automata automata) {
        this.automata = automata;
        transitionTable = new TableView<>();
    }
    
    public State getStartingState() {
        for (State state : automata.getStates().keySet()) {
            if (state.isStartingState()) {
                return state;
            }
        }
        return null;
    }
    
    /**
     * Removes all transitions to the given state from all other states
     * @param deletingState
     */
    public void removeTransitionsToState(State deletingState) {
        Iterator<State> iterator = automata.getStates().keySet().iterator();
        while (iterator.hasNext()) {
            State state = iterator.next();
            List<Transition> transitionsToRemove = new ArrayList<>();
            for (Transition transition : state.getTransitions()) {
                if (transition.getTargetState().equals(deletingState)) {
                    transitionsToRemove.add(transition);
                }
            }
            state.getTransitions().removeAll(transitionsToRemove);
        }
    }
    
    public State getSelectedState() {
        return selectedState;
    }
    
    public void setSelectedState(State selectedState) {
        this.selectedState = selectedState;
    }

    public void setTransitionTable(TableView<ObservableList<StringProperty>> transitionTable) {
        this.transitionTable = transitionTable;
    }
    
    public State getRejectedState() {
        return rejectedState;
    }
    
    // Set rejected state to null
    public void clearRejectedState() {
        rejectedState = null;
    }
    
    public Automata getAutomata() {
        return automata;
    }
    
    public String getStringAllStates() {
        StringJoiner joiner = new StringJoiner(", ", "{", "}");
        for (State state : automata.getStates().keySet()) {
            if (!state.isDiscarded()) {
                joiner.add(state.getName());
            
            }
        }
        return joiner.toString();
    }
    
    public String getStringAlphabet() {
        StringJoiner joiner = new StringJoiner(", ", "{", "}");
        for (String symbol : automata.getAlphabet()) {
            joiner.add(symbol);
        }
        return joiner.toString();
    }
    
    public String getStringStartingState() {
        for (State state : automata.getStates().keySet()) {
            if (state.isStartingState()) {
                return state.getName();
            }
        }
        return "";
    }
    
    public String getStringAllAcceptingStates() {
        StringJoiner joiner = new StringJoiner(", ", "{", "}");
        for (State state : automata.getStates().keySet()) {
            if (state.isAcceptingState()) {
                joiner.add(state.getName());
            }
        }
        return joiner.toString();
    }
    
    public State getStateByName(String name) {
        for (State state : automata.getStates().keySet()) {
            if (state.getName().equals(name)) {
                return state;
            }
            
            if (name.equals("{}") && state.getName().equals(SpecialSymbols.EMPTY_SET.toString())) {
                return state;
            }
        }
        
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("The state '" + name + "' does not exist.");
        alert.showAndWait();
        
        return null;
    }

    // Update states
    public void updateStates(Map<State, SerializablePoint2D> states) {
        automata.setStates(states);
        updateTransitionTable();
    }

    // Update transition table
    public void updateTransitionTable() {
        transitionTable.getItems().clear();
        transitionTable.getColumns().clear();
    
        TableColumn<ObservableList<StringProperty>, String> stateColumn = createEditableColumn("State", 0);
        TableColumn<ObservableList<StringProperty>, String> symbolColumn = createEditableColumn("Input Symbol", 1);
        TableColumn<ObservableList<StringProperty>, String> nextStateColumn = createEditableColumn("Next State", 2);
    
        transitionTable.getColumns().addAll(stateColumn, symbolColumn, nextStateColumn);
    
        for (State state : automata.getStates().keySet()) {
            for (Transition transition : state.getTransitions()) {
                ObservableList<StringProperty> row = FXCollections.observableArrayList();
                row.add(new SimpleStringProperty(state.getName()));
                row.add(new SimpleStringProperty(transition.getTransitionSymbol()));
                row.add(new SimpleStringProperty(transition.getTargetState().getName()));
    
                transitionTable.getItems().add(row);
            }
        }

        transitionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        MainCanvas.drawAllCanvases();
    }

    // Create an editable column for the transition table
    public TableColumn<ObservableList<StringProperty>, String> createEditableColumn(String title, int columnIndex) {
        TableColumn<ObservableList<StringProperty>, String> column = new TableColumn<>(title);
        
        column.setCellValueFactory(cellData -> cellData.getValue().get(columnIndex));
        column.setCellFactory(TextFieldTableCell.forTableColumn()); // Make the column editable
        
        column.setOnEditCommit(e -> {
            TablePosition<ObservableList<StringProperty>, String> pos = e.getTablePosition(); 
            String newValue = e.getNewValue(); 
            int row = pos.getRow(); 
            ObservableList<StringProperty> rowData = e.getRowValue(); 
            
            if (shouldUpdateTable(row, columnIndex, newValue)) {
                updateControllerTable(row, columnIndex, newValue);
                rowData.set(columnIndex, new SimpleStringProperty(newValue));
            } else {
                rowData.set(columnIndex, new SimpleStringProperty(e.getOldValue()));
                column.getTableView().refresh();
            }
        });
        
        return column;
    }

    private boolean shouldUpdateTable(int row, int columnIndex, String newValue) {
        if (newValue == null || newValue.isEmpty()) {
            return false;
        }
        
        if (columnIndex == 0 || columnIndex == 2) {
            return getStateByName(newValue) != null;
        } else if (columnIndex == 1) {
            if (automata.getAlphabet().contains(newValue) || newValue.equals("-")) {
                return true;
            } else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("The symbol '" + newValue + "' is not in the automaton's alphabet.");
                alert.showAndWait();
            }
        }
        
        return false;
    }
    

    /**
     * Updates the controller's transition table with the new value
     * @param row
     * @param columnIndex
     * @param newValue
     */
    public void updateControllerTable(int row, int columnIndex, String newValue) {
        State sourceState = getStateByName(transitionTable.getItems().get(row).get(0).get());
        String symbol = transitionTable.getItems().get(row).get(1).get();
        Transition transition = sourceState.getTransitionBySymbol(symbol);
        
        if (columnIndex == 0) {
            State newSourceState = getStateByName(newValue);
            
            if (newSourceState != null) {
                sourceState.removeTransition(transition);
                transition.setSourceState(newSourceState);
                newSourceState.addTransition(transition);
                updateTransitionTable();
            }
        } else if (columnIndex == 1) {
            updateTransitionSymbol(transition, newValue);
        } else if (columnIndex == 2) {
            State newTargetState = getStateByName(newValue);
            
            if (newTargetState != null) {
                transition.setTargetState(newTargetState);
                updateTransitionTable();
            }
        }
    }
    
    public TableView<ObservableList<StringProperty>> getTransitionTable() {
        return transitionTable;
    }

    /**
     * Updates the given transition object with the new symbol
     * @param transition
     * @param symbol
     */
    public void updateTransitionSymbol(Transition transition, String symbol) {
        if (symbol.equals("-")) {
            symbol = SpecialSymbols.EPSILON.toString();
        }
        
        for (Transition eachTransition : transition.getSourceState().getTransitions()) {
            if (eachTransition.getTargetState().equals(transition.getTargetState()) && eachTransition.getTransitionSymbol().equals(symbol)) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("The transition already exists.");
                alert.setContentText("Please try a different transition.");
                alert.showAndWait();
                return;
            }
        } 

        if (automata.getAlphabet().contains(symbol) || symbol.equals(SpecialSymbols.EPSILON.toString())) {
            transition.updateTransitionSymbol(symbol);
            updateTransitionTable();
            automata.isValid();
            SubsetConstructionView.updateCorrespondingAutomata(automata);
        } else {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("The symbol '" + symbol + "' is not in the automaton's alphabet.");
            alert.showAndWait();
        }
    }

    public void clearAutomata() {
        automata.clear();
        updateTransitionTable();
        selectedState = null;
    }

    public void setAlphabet(String alphabet) {
        automata.setAlphabet(alphabet);
        updateTransitionTable();
    }

    public void showPopupMessage(String stringText) {
        VBox content = new VBox();
        content.getStylesheets().add(getClass().getResource("/css/general.css").toExternalForm());
        content.getStyleClass().add("popup-content");
        
        Label label = new Label(stringText);
        content.getChildren().add(label);
        
        Popup popup = new Popup();
        popup.getContent().add(content);
        
        Window appWindow = App.getScene().getWindow();
        popup.show(appWindow, appWindow.getX() + appWindow.getWidth() - 300, appWindow.getY() + 50);
        
        Timeline popupTimeline = new Timeline(new KeyFrame(Duration.seconds(4), event -> popup.hide()));
        popupTimeline.play();
    }
}
