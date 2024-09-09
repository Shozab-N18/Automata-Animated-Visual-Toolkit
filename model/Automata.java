package model;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.HashMap;

import components.SpecialSymbols;
import components.serialization.SerializablePoint2D;

/*
 * Automata class: The model class that represents the automata
 */
public class Automata {
    private Map<State, SerializablePoint2D> states;
    protected Set<String> alphabet;
    private State startingState;
    private Set<State> acceptingStates;
    
    protected boolean isValid = false;
    protected boolean isDFA;
    
    // Constructor
    public Automata(Map<State, SerializablePoint2D> states) {
        this.states = states;
        alphabet = new HashSet<>();
        acceptingStates = new HashSet<>();
        
        setAlphabet("ab");
        setAsNFA();
    }
    
    // Default constructor
    public Automata() {
        this(new HashMap<>());
    }
    
    /**
     * Deterministic if and only if for each state, there is exactly one transition for each symbol in the alphabet
     * and every transition leads to a different state and there is a starting state and the alphabet is not empty.
     * @return boolean
     */
    public boolean isValidDFA() {
        for (State state : getStates().keySet()) {
            // check if there exists a transition for each symbol in the alphabet
            for (String symbol : alphabet) {
                if (state.getTransitionBySymbol(symbol) == null) {
                    return false;
                }
            }
            
            // check if every transition leads to a different state
            for (Transition transition : state.getTransitions()) {
                // check if there is more than one transition to the same target state
                for (Transition otherTransition : state.getTransitions()) {
                    if (!transition.equals(otherTransition) && transition.getTargetState().equals(otherTransition.getTargetState())) {
                        return false;
                    }
                }
                
                // check every transitions symbol is in the alphabet
                if (!alphabet.contains(transition.getTransitionSymbol())) {
                    return false;
                }
            }
            
            // check if there is exactly one transition for each symbol in the alphabet
            if (state.getTransitions().size() != alphabet.size()) {
                return false;
            }
        }
        return hasStartingState() && hasAlphabet();
    }

    /**
     * Non-deterministic if and only if there are no input symbols that are not in the alphabet
     * and there is a starting state and the alphabet is not empty.
     * @return boolean
     */
    public boolean isValidNFA() {
        for (State state : getStates().keySet()) {
            // check if theres a symbol not in the alphabet
            for (Transition transition : state.getTransitions()) {
                if (!alphabet.contains(transition.getTransitionSymbol())) {
                    return false;
                }
            }
        }
        
        return hasStartingState() && hasAlphabet();
    }

    public boolean isDFA() {
        return isDFA;
    }
    
    // Set the automata as a DFA and remove epsilon to the alphabet
    public void setAsDFA() {
        isDFA = true;
        alphabet.remove(SpecialSymbols.EPSILON.toString());
        isValid();  
    }

    // Set the automata as a NFA and add epsilon to the alphabet
    public void setAsNFA() {
        isDFA = false;
        alphabet.add(SpecialSymbols.EPSILON.toString());
        isValid();
    }

    public Map<State, SerializablePoint2D> getStates() {
        return states;
    }

    public void setStates(Map<State, SerializablePoint2D> states) {
        this.states = states;
        isValid();
    }

    public Set<String> getAlphabet() {
        return alphabet;
    }
    
    /**
     * Set the alphabet and add epsilon if the automata is a NFA
     * @param alphabet
     */
    public void setAlphabet(String alphabet) {
        this.alphabet = Arrays.asList(alphabet.split("")).stream()
            .map(s -> s.substring(0)).collect(java.util.stream.Collectors.toSet());
        
        if (!isDFA) {
            this.alphabet.add(SpecialSymbols.EPSILON.toString());
        }
        
        isValid();
    }

    public State getStartingState() {
        return startingState;
    }

    public void setStartingState(State startingState) {
        this.startingState = startingState;
        isValid();
    }

    public Set<State> getAcceptingStates() {
        acceptingStates.clear();
        
        for (State state : states.keySet()) {
            if (state.isAcceptingState()) {
                acceptingStates.add(state);
            }
        }
        return acceptingStates;
    }

    public boolean hasStartingState() {
        for (State state : states.keySet()) {
            if (state.isStartingState()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAcceptingState() {
        for (State state : states.keySet()) {
            if (state.isAcceptingState()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAlphabet() {
        return !alphabet.isEmpty();
    }

    public boolean isValid() {
        if (isDFA) {
            isValid = isValidDFA();  
        } else {
            isValid = isValidNFA();
        }
        return isValid;
    }

    public void clear() {
        states.clear();
        alphabet.clear();
        acceptingStates.clear();
        startingState = null;
        isValid = false;
    }
}
