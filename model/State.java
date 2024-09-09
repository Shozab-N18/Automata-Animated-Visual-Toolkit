package model;

import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.io.Serializable;

import components.SpecialSymbols;

/*
 * State model class. Represents a single state in a finite automaton.
 */
public class State implements Serializable {
    private static final long serialVersionUID = 1L; // Required for serializable objects
    
    private String name;
    private boolean isAcceptingState;
    private boolean isStartingState;
    private Set<Transition> transitions;
    
    private boolean isDiscarded;
    
    // Constructor
    public State(String name) {
        this.name = name;
        isAcceptingState = false;
        isStartingState = false;
        transitions = new HashSet<>();
        isDiscarded = false;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean isAcceptingState() {
        return isAcceptingState;
    }
    
    public void setAcceptingState(boolean acceptingState) {
        isAcceptingState = acceptingState;
    }
    
    public void setStartingState(boolean startingState) {
        isStartingState = startingState;
    }
    
    public boolean isStartingState() {
        return isStartingState;
    }
    
    public void updateName(String name) {
        this.name = name;
    }
    
    public void toggleAcceptingState() {
        isAcceptingState = !isAcceptingState;
    }
    
    public void toggleStartingState() {
        isStartingState = !isStartingState;
    }
    
    public Set<Transition> getTransitions() {
        return transitions;
    }
    
    /**
     * Get the transition from the current state to the target state with the given symbol.
     * @param symbol
     * @return Transition
     */
    public Transition getTransitionBySymbol(String symbol) {
        for (Transition transition : transitions) {
            if (transition.getTransitionSymbol().equals(symbol)) {
                return transition;
            }
        }
        return null;
    }
    
    /**
     * Get the transition symbol that goes from the current state to the target state.
     * @param targetState
     * @return String
     */
    public String getTransitionSymbolToTargetState(State targetState) {
        for (Transition transition : transitions) {
            if (transition.getTargetState().equals(targetState)) {
                return transition.getTransitionSymbol();
            }
        }
        return null;
    }
    
    /**
     * For the current state, find all transitions to a specific state and return them in a map 
     * with the set of transitions as the key and the merged symbols for the transitions as the value.
     * @param targetState
     * @return Map<Set<Transition>, String>
     */
    public Map<Set<Transition>, String> getMultipleTransitionsToTarget(State targetState) {
        Set<Transition> multipleTransitions = new HashSet<>();
        String mergedSymbols = "";
        for (Transition transition : transitions) {
            if (transition.getTargetState().equals(targetState)) {
                multipleTransitions.add(transition);
                mergedSymbols += transition.getTransitionSymbol();
            }
        }
        
        Map<Set<Transition>, String> result = new HashMap<>();
        result.put(multipleTransitions, mergedSymbols);
        return result;
    }
    
    /**
     * For a given state, find its epsilon closure. I.e. the set of states that can be reached from the state using epsilon transitions.
     * @param state
     * @return Set<State>
     */
    public static Set<State> epsilonClosure(State state, Set<State> visited) {
        Set<State> closure = new HashSet<>();
        if (visited.contains(state)) {
            return closure;
        }
        visited.add(state); 
        closure.add(state);
        
        for (Transition transition : state.getTransitions()) {
            if (transition.getTransitionSymbol().equals(SpecialSymbols.EPSILON.toString())) {
                closure.addAll(epsilonClosure(transition.getTargetState(), visited));
            }
        }
        return closure;
    }
    
    /**
     * For the state object, find the next states that can be reached from the state using a specific symbol.
     * @param symbol
     * @return Set<State>
     */
    public Set<State> getNextStatesFromSymbol(String symbol) {
        Set<State> nextStates = new HashSet<>();
        for (Transition transition : transitions) {
            if (transition.getTransitionSymbol().equals(symbol)) {
                nextStates.add(transition.getTargetState());
            }
            else if (transition.getTransitionSymbol().equals(SpecialSymbols.EPSILON.toString())) {
                nextStates.add(transition.getTargetState());
                nextStates.addAll(epsilonClosure(transition.getTargetState(), new HashSet<>()));
            }
        }
        return nextStates;
    }
    
    /**
     * For the state object, find the next states that can be reached from the state 
     * using a specific symbol that is not an epsilon transition.
     * @param symbol
     * @return Set<State>
     */
    public Set<State> getNonEpsilonNextStatesFromSymbol(String symbol) {
        Set<State> nextStates = new HashSet<>();
        for (Transition transition : transitions) {
            if (transition.getTransitionSymbol().equals(symbol)) {
                nextStates.add(transition.getTargetState());
            }
        }
        return nextStates;
    }
    
    /**
     * For the state object, find the next state that can be reached from the state using a specific symbol.
     * @param symbol
     * @return State
     */
    public State getNextStateFromSymbol(String symbol) {
        for (Transition transition : transitions) {
            if (transition.getTransitionSymbol().equals(symbol)) {
                return transition.getTargetState();
            }
        }
        return null;
    }
    
    public void addTransition(State toState, String symbol) {
        transitions.add(new Transition(this, toState, symbol));
    }
    
    public void addTransition(Transition transition) {
        transitions.add(transition);
    }
    
    public void removeTransition(Transition transition) {
        transitions.remove(transition);
    }
    
    public void updateTransition(Transition transition, String newSymbol) {
        transition.updateTransitionSymbol(newSymbol);
    }
    
    public void clearTransitions() {
        transitions.clear();
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    public boolean isDiscarded() {
        return isDiscarded;
    }
    
    public void toggleDiscarded() {
        isDiscarded = !isDiscarded;
    }
    
    /**
     * Check if the current state object is equal to another state object.
     * @param state
     * @return boolean
     */
    @Override
    public boolean equals(Object obj) {
        State state = (State) obj;
        
        if (!this.name.equals(state.getName())) {
            return false;
        }
        
        // Compare transitions
        for (Transition transition : transitions) {
            boolean found = false;
            for (Transition otherTransition : state.getTransitions()) {
                if (transition.equals(otherTransition)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        
        if (isAcceptingState != state.isAcceptingState()) {
            return false;
        }
        
        if (isStartingState != state.isStartingState()) {
            return false;
        }
        
        if (isDiscarded != state.isDiscarded()) {
            return false;
        }
        
        return true;
    }

    private void setDiscarded(boolean isDiscarded) {
        this.isDiscarded = isDiscarded;
    }

    /**
     * Create and return a copy of the state object.
     * @return State
     */
    public State copy() {
        State copy = new State(name);
        copy.setAcceptingState(isAcceptingState);
        copy.setStartingState(isStartingState);
        copy.setDiscarded(isDiscarded);
        for (Transition transition : transitions) {
            copy.addTransition(transition.copy());
        }
        return copy;
    }
}