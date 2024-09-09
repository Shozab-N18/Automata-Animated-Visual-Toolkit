package controller;

import java.util.Set;
import java.util.stream.Collectors;

import components.serialization.SerializablePoint2D;
import components.SpecialSymbols;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import model.Automata;
import model.State;
import model.Transition;
import view.SubsetConstructionView;

/*
 * ConstructionController class is the controller for the automaton construction view.
 */
public class ConstructionController extends AutomataController{
    private Label automataValidityLabel;
    private int stateCounter = 0;

    // Constructor
    public ConstructionController(Automata automata) {
        super(automata);
        this.automataValidityLabel = new Label("Automata is valid: false");
    }
    
    /**
     * Adds a state to the automaton at the given position.
     * @param position
     */
    public void addState(SerializablePoint2D position) {
        String automatonName = "q" + stateCounter++;
        State state = new State(automatonName);
        automata.getStates().put(state, position);
        updateAutomataValidityLabel(automata.isValid());
        updateSubsetConstructionView();
    }
    
    public void setSelectedState(State state) {
        selectedState = state;
    }
    
    /**
     * Removes all transitions to and from the given state.
     * @param state
     */
    public void deleteState(State state) {
        automata.getStates().remove(state);
        removeTransitionsToState(state);
        stateCounter--;
        
        updateTransitionTable();
        updateAutomataValidityLabel(automata.isValid());
        updateSubsetConstructionView();
    }

    public void addTransition(State fromState, State toState, String symbol) {
        if (symbol.equals("-")) {
            symbol = SpecialSymbols.EPSILON.toString();
        }
        
        for (Transition transition : fromState.getTransitions()) {
            if (transition.getTargetState().equals(toState) && transition.getTransitionSymbol().equals(symbol)) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("The transition already exists.");
                alert.setContentText("Please try a different transition.");
                alert.showAndWait();
                return;
            }
        }

        if (automata.getAlphabet().contains(symbol)) {
            fromState.addTransition(toState, symbol);
            updateTransitionTable();
            updateAutomataValidityLabel(automata.isValid());
            updateSubsetConstructionView();
        } else {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("The symbol '" + symbol + "' is not in the automaton's alphabet.");
            alert.setContentText("Please add the symbol to the alphabet first.");
            alert.showAndWait();
        }
    }
    
    public void removeTransition(Transition transition) {
        transition.getSourceState().removeTransition(transition);
        updateTransitionTable();
        updateAutomataValidityLabel(automata.isValid());
        updateSubsetConstructionView();
    }

    public void updateStateName(State state, String name) {
        if (name.equals("{}")) {
            name = SpecialSymbols.EMPTY_SET.toString();
        }
        
        Set<String> stateNames = automata.getStates().keySet().stream().map(State::getName).collect(Collectors.toSet());
        
        if (stateNames.contains(name)) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("The state name '" + name + "' already exists.");
            alert.setContentText("Please try a different name.");
            alert.showAndWait();
            return;
        }
        
        state.updateName(name);
        updateTransitionTable();
        updateSubsetConstructionView();
    }

    public void toggleAcceptingState(State state) {
        state.toggleAcceptingState();
        updateTransitionTable();
        updateAutomataValidityLabel(automata.isValid());
        updateSubsetConstructionView();
    }

    public void toggleStartingState(State state) {
        state.toggleStartingState();
        updateTransitionTable();
        updateAutomataValidityLabel(automata.isValid());
        updateSubsetConstructionView();
    }

    public void updateAutomata(Automata automata) {
        this.automata = automata;
        updateTransitionTable();
        updateAutomataValidityLabel(automata.isValid());
        updateSubsetConstructionView();
        
        stateCounter = automata.getStates().size();
    }

    public void updateAutomataValidityLabel(boolean isValid) {
        automataValidityLabel.setText("Automata is valid: " + isValid);
    }  

    public Label getAutomataValidityLabel() {
        return automataValidityLabel;
    }

    protected void updateSubsetConstructionView() {
        SubsetConstructionView.updateCorrespondingAutomata(automata);
    }

    @Override
    public void setAlphabet(String alphabet) {
        automata.setAlphabet(alphabet);
        SubsetConstructionView.updateAlphabetView(alphabet);
        updateAutomataValidityLabel(automata.isValid());
        updateTransitionTable();
        updateSubsetConstructionView();
    }

    @Override
    public void clearAutomata() {
        automata.clear();
        stateCounter = 0;
        updateTransitionTable();
        selectedState = null;
    }
}