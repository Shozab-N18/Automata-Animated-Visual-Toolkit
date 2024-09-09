package model;

import java.io.Serializable;
/*
 * Transition: Represents a transition between two states in an automaton.
 */
public class Transition implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private State sourceState;
    private String transitionSymbol;
    private State nextState;

    // Constructor
    public Transition(State sourceState, State targetState, String transitionSymbol) {
        this.sourceState = sourceState;
        this.transitionSymbol = transitionSymbol;
        this.nextState = targetState;
    }

    public State getSourceState() {
        return sourceState;
    }

    public String getTransitionSymbol() {
        return transitionSymbol;
    }

    public State getTargetState() {
        return nextState;
    }

    public void updateTransitionSymbol(String symbol) {
        this.transitionSymbol = symbol;
    }

    public void setTargetState(State targetState) {
        this.nextState = targetState;
    }

    public void setSourceState(State sourceState) {
        this.sourceState = sourceState;
    }

    public Transition copy() {
        return new Transition(sourceState, nextState, transitionSymbol);
    }

    @Override
    public String toString() {
        return sourceState.getName() + " -> " + transitionSymbol + " -> " + nextState.getName();
    }

    @Override
    public boolean equals(Object obj) {
        Transition transition = (Transition) obj;
        return this.sourceState.getName().equals(transition.getSourceState().getName()) && this.transitionSymbol.equals(transition.getTransitionSymbol()) && this.nextState.getName().equals(transition.getTargetState().getName());
    }
}
