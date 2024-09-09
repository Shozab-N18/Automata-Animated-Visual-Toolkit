package controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.StringJoiner;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import model.*;
import view.SubsetConstructionView;
import components.serialization.SerializablePoint2D;
import components.SpecialSymbols;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Controller class for the subset construction algorithm.
 */
public class SubsetConstructionController extends AutomataController {
    private State emptyState;
    private Map<State, Set<State>> dfaStateMap;
    private State dfaStartState;
    private Queue<State> queue;
    private Map<State, SerializablePoint2D> dfaStates;
    
    private Set<State> expectedDFAStates;
    
    public SubsetConstructionController(Automata automata) {
        super(automata);
        
        emptyState = new State(SpecialSymbols.EMPTY_SET.toString());
        dfaStateMap = new HashMap<>();
        dfaStartState = null;
        queue = new LinkedList<>();
        dfaStates = new HashMap<>();
        expectedDFAStates = new HashSet<>();
    }
    
    public void convertToDFA() {
        clearData();
        
        if (getStartingState() == null) {
            return;
        }
        
        dfaStartState = createDFAStartState();
        initialiseEmptyState();
        
        queue.add(dfaStartState);
        
        while (!queue.isEmpty()) {
            State currentDFAstate = queue.poll();
            processDFAState(currentDFAstate);
        }
    }
    
    public Map<State, SerializablePoint2D> getDFAStates() {
        return dfaStates;
    }

    @Override
    public State getStartingState() {
        State state = super.getStartingState();
        
        if (state == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("No Starting State");
            alert.setHeaderText("No starting state found.");
            alert.setContentText("Please add a starting state to the automata before converting to DFA.");
            alert.showAndWait();
            return null;
        } else {
            return state;
        }
    }
    
    /**
     * Check if the state is an attempted state and if it is, check if it is the expected state.
     * @param state
     * @return boolean
     */
    public boolean isAttemptedStateExpected(State state) {
        if (expectedDFAStates.isEmpty()) {
            return true;
        }
        for (State expectedState: expectedDFAStates) {
            if (expectedState.getName().equals(state.getName())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Initialise the conversion attempt by creating new DFA states
     * @param expectedDFAStates
     * @param nfaStates
     */
    public void attemptConversion(Map<State, SerializablePoint2D> expectedDFAStates, Map<State, SerializablePoint2D> nfaStates) {
        dfaStates.clear();
        this.expectedDFAStates = expectedDFAStates.keySet(); // Get the expected DFA states
        
        // Create new states for the NFA states and add them to the DFA states
        for (State state: nfaStates.keySet()) {
            State dfaState = new State(state.getName());
            dfaStates.put(dfaState, new SerializablePoint2D());
        }
        
        Set<String> newStateSet = new HashSet<>();
        newStateSet = getNewStatesSetForState(nfaStates.keySet()); // Get the additional states that should be in the DFA
        
        for (String stateName: newStateSet) {
            State state = new State(stateName);
            dfaStates.put(state, new SerializablePoint2D());
        }
        
        initialiseEmptyState();
        
        updateStates(dfaStates);
        updateTransitionTable();
    }
    
    /**
     * Get the set of new states that should be in the DFA for a given set of NFA states by combining all possible states.
     * @param states
     * @return Set<String>
     */
    public Set<String> getNewStatesSetForState(Set<State> states) {
        Set<String> newStatesSet = new HashSet<>();
        
        for (State state1 : states) {
            for (State state2 : states) {
                if (!state1.getName().equals(state2.getName())) {
                    StringBuilder combination = new StringBuilder("{ ");
                    String stateName1 = state1.getName();
                    String stateName2 = state2.getName();
                    
                    // Check if in alphabetical order
                    if (stateName1.compareTo(stateName2) < 0) {
                        combination.append(stateName1).append(" ").append(stateName2);
                    } else {
                        combination.append(stateName2).append(" ").append(stateName1);
                    }
                    
                    combination.append(" }");
                    newStatesSet.add(combination.toString());
                }
            }
        }

        // Add the combination of all possible states
        StringBuilder allStatesCombination = new StringBuilder("{ ");
        for (State state : states) {
            allStatesCombination.append(state.getName()).append(" ");
        }
        allStatesCombination.append("}");
        newStatesSet.add(allStatesCombination.toString());
        
        return newStatesSet;
    }
    
    public void checkIsNewTransitionCorrect(State fromState, State targetState, String transitionSymbol) {
        State tempState = null;

        for (State dfaState: expectedDFAStates) {
            if (dfaState.getName().equals(fromState.getName())) {
                tempState = dfaState;
                break;
            }
        }

        if (tempState == null) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("The transition is incorrect.");
            alert.setContentText("There should not be a transition from this state.");
            alert.show();
            return;
        }
        
        for (Transition transition: tempState.getTransitions()) {
            if (transition.getTargetState().getName().equals(targetState.getName())) {
                if (transition.getTransitionSymbol().equals(transitionSymbol)) {
                    Alert alert = new Alert(AlertType.CONFIRMATION);
                    alert.setTitle("Correct");
                    alert.setHeaderText(null);
                    alert.setContentText("The transition is correct.");
                    alert.show();
                    return;
                } else {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("The transition is incorrect.");
                    alert.setContentText("There is no transition from " + tempState.getName() + " to " + targetState.getName() + " with the symbol " + transitionSymbol + ".");
                    alert.show();
                    return;
                }
            }
        }
        
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("The transition is incorrect.");
        alert.setContentText("There should not be a transition to this state.");
        alert.show();
    }
    
    public void checkShouldBeAcceptingState(State state) {
        for (State dfaState: expectedDFAStates) {
            if (dfaState.getName().equals(state.getName())) {
                if (dfaState.isAcceptingState() == state.isAcceptingState()) {
                    Alert alert = new Alert(AlertType.CONFIRMATION);
                    alert.setTitle("Correct");
                    alert.setHeaderText(null);
                    if (state.isAcceptingState()) {
                        alert.setContentText("The state " + state + " is an accepting state.");
                    } else {
                        alert.setContentText("The state " + state + " is not an accepting state.");
                    }
                    alert.show();
                    return;
                } else {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("This change is incorrect.");
                    alert.setContentText("The state "+ state + " should " + (dfaState.isAcceptingState() ? "be" : "not be") + " an accepting state.");
                    alert.show();
                    return;
                }
            }
        }
        
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("This change is incorrect.");
        alert.setContentText("There should not be a state with this name.");
        alert.show();
    }

    public void checkShouldBeStartingState(State state) {
        for (State dfaState: expectedDFAStates) {
            if (dfaState.getName().equals(state.getName())) {
                if (dfaState.isStartingState() == state.isStartingState()) {
                    Alert alert = new Alert(AlertType.CONFIRMATION);
                    alert.setTitle("Correct");
                    alert.setHeaderText(null);
                    if (state.isStartingState()) {
                        alert.setContentText("The state " + state + " is a starting state.");
                    } else {
                        alert.setContentText("The state " + state + " is not a starting state.");
                    }
                    alert.show();
                    return;
                } else {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("This change is incorrect.");
                    alert.setContentText("The state should " + (dfaState.isStartingState() ? "be" : "not be") + " a starting state.");
                    alert.show();
                    return;
                }
            }
        }
        
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("This change is incorrect.");
        alert.setContentText("There should not be a state with this name.");
        alert.show();
    }
    
    public void checkShouldStateBeDiscarded(State state) {
        boolean doesStateExist = false;
        boolean shouldBeDiscarded = false;
        
        for (State state1 : expectedDFAStates) {
            if (state1.getName().equals(state.getName())) {
                doesStateExist = true;
                break;
            }
        }
        
        shouldBeDiscarded = !doesStateExist;
        
        if (shouldBeDiscarded && !state.isDiscarded()) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("This change is incorrect.");
            alert.setContentText("The state " + state + " should be discarded.");
            alert.show();
        } else if (!shouldBeDiscarded && state.isDiscarded()) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("This change is incorrect.");
            alert.setContentText("The state " + state + " should not be discarded.");
            alert.show();
        } else {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Correct");
            alert.setHeaderText(null);
            alert.setContentText("The state " + state + (state.isDiscarded() ? " is discarded." : " should not be discarded."));
            alert.show();
        }
    }
    

    public boolean checkIsConversionCorrect() {
        for (State expectedState: expectedDFAStates) {
            for (State dfaState : dfaStates.keySet()) {
                if (expectedState.getName().equals(dfaState.getName())) {
                    if (!expectedState.equals(dfaState)) {
                        return false;
                    }
                }
            }
        }
        
        // check if all states not present in the expected automata are discarded
        for (State dfaState : dfaStates.keySet()) {
            boolean found = false;
            
            for (State expectedState : expectedDFAStates) {
                if (expectedState.getName().equals(dfaState.getName())) {
                    found = true;
                    break;
                }
            }
            // If the state is not found in the expected automaton and it's not discarded
            if (!found && !dfaState.isDiscarded()) {
                System.out.println("State " + dfaState.getName() + " is not discarded when it should be.");
                return false;
            }
        }
        
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Correct");
        alert.setHeaderText("Congratulations!");
        alert.setContentText("The conversion attempt is fully correct.");
        alert.show();
        return true;
    }
    
    private void processDFAState(State currentDFAstate) {
        Set<String> alphabet = new HashSet<>(getAutomata().getAlphabet());
        alphabet.remove(SpecialSymbols.EPSILON.toString());
        
        for (String symbol : alphabet) {
            Set<State> newDFAStateComposition = computeNewDFAStateComposition(currentDFAstate, symbol);
            
            if (newDFAStateComposition.contains(emptyState) && newDFAStateComposition.size() > 1){
                newDFAStateComposition.remove(emptyState);
            }
            
            if (shouldAddNewState(newDFAStateComposition)){
                State newState = createNewDFAState(newDFAStateComposition);
                dfaStateMap.put(newState, newDFAStateComposition);
                
                queue.add(newState);
                addTransition(currentDFAstate, newState, symbol);
            } else {
                State existingState = getExistingDFAState(newDFAStateComposition);
                addTransition(currentDFAstate, existingState, symbol);
            }
        }
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
        } else {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("The symbol '" + symbol + "' is not in the automaton's alphabet.");
            alert.show();
        }
    }

    public void toggleDiscardedState(State state) {
        state.toggleDiscarded();
        for (Map.Entry<State, SerializablePoint2D> item : dfaStates.entrySet()) {
            if (item.getKey().getName().equals(state.getName()) && state.isDiscarded()) {
                item.setValue(new SerializablePoint2D(800, 600));
            }
        }
        
        if (state.isDiscarded()) {
            state.setAcceptingState(false);
            state.setStartingState(false);
            state.clearTransitions();
            removeTransitionsToState(state);
        }
        
        updateStates(dfaStates);
    }
    

    private State getExistingDFAState(Set<State> composition) {
        for (Map.Entry<State, Set<State>> entry : dfaStateMap.entrySet()) {
            if (entry.getValue().equals(composition)) {
                return entry.getKey();
            }
        }
        return emptyState;
    }
    
    public Set<State> computeNewDFAStateComposition(State currentDFAstate, String symbol) {
        Set<State> newDFAStateComposition = new HashSet<>();
        
        for (State state : dfaStateMap.get(currentDFAstate)) {
            Set<State> reachableStates = getReachableStates(state, symbol);
            Set<State> reachStatesClosure = new HashSet<>();
            
            for (State reachableState : reachableStates) {
                reachStatesClosure.addAll(State.epsilonClosure(reachableState, new HashSet<>()));
            }
            newDFAStateComposition.addAll(reachStatesClosure);
        }
        
        return newDFAStateComposition;
    }

    public Set<State> getReachableStates(State state, String symbol) {
        Set<State> reachableStates = new HashSet<>();
        
        for (Transition transition : state.getTransitions()) {
            if (transition.getTransitionSymbol().equals(symbol)) {
                reachableStates.add(transition.getTargetState());
            }
        }
        
        return reachableStates;
    }
    
    public String getMergedStatesName(Set<State> states) {
        List<String> sortedNames = new ArrayList<>();
        for (State state : states) {
            if (!state.getName().equals(SpecialSymbols.EMPTY_SET.toString())) {
                sortedNames.add(state.getName());
            }
        }
        Collections.sort(sortedNames);
        
        StringJoiner joiner = new StringJoiner(" ");
        for (String name : sortedNames) {
            joiner.add(name);
        }
        
        String mergedNames = joiner.toString();
        
        if (mergedNames.isEmpty()) {
            return SpecialSymbols.EMPTY_SET.toString();
        } else if (mergedNames.contains(" ")) {
            return "{ " + mergedNames + " }";
        } else {
            return mergedNames;
        }
    }


    public boolean hasAcceptingState(Set<State> states) {
        for (State state : states) {
            if (state.isAcceptingState()) {
                return true;
            }
        }
        return false;
    }


    private State createDFAStartState() {
        Set<State> startingClosure = State.epsilonClosure(getStartingState(), new HashSet<>());
        State dfaStartState = createNewDFAState(startingClosure);
        dfaStartState.toggleStartingState();
        dfaStateMap.put(dfaStartState, startingClosure);
        return dfaStartState;
    }

    private void initialiseEmptyState() {
        dfaStates.put(emptyState, new SerializablePoint2D());
        
        Set<String> alphabet = new HashSet<>(getAutomata().getAlphabet());
        alphabet.remove(SpecialSymbols.EPSILON.toString());
        
        for (String symbol : alphabet) {
            emptyState.addTransition(new Transition(emptyState, emptyState, symbol));
        }
    }

    private boolean shouldAddNewState(Set<State> newDFAStateComposition) {
        return !newDFAStateComposition.isEmpty() && !newDFAStateComposition.contains(emptyState) && !dfaStateMap.containsValue(newDFAStateComposition);
    }

    private State createNewDFAState(Set<State> newDFAStateComposition) {
        State newState = new State(getMergedStatesName(newDFAStateComposition));
        newState.setAcceptingState(hasAcceptingState(newDFAStateComposition));
        dfaStates.put(newState, new SerializablePoint2D());
        return newState;
    }

    private void clearData() {
        dfaStateMap.clear();
        queue.clear();
        dfaStartState = null;
        emptyState.getTransitions().clear();
        dfaStates.clear();
    }

    public void removeTransition(Transition transition) {
        transition.getSourceState().removeTransition(transition);
        updateTransitionTable();
        SubsetConstructionView.updateCorrespondingAutomata(automata);
    }
}
