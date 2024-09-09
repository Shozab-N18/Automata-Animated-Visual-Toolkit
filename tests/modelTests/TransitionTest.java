package tests.modelTests;


import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.*;

public class TransitionTest {
    private State sourceState;
    private State targetState;
    private String transitionSymbol;
    private Transition transition;
    
    @BeforeEach
    public void setUp() {
        sourceState = new State("q0");
        targetState = new State("q1");
        transitionSymbol = "a";
        transition = new Transition(sourceState, targetState, transitionSymbol);
    }
    
    @Test
    public void testConstructorAndGetters() {
        assertEquals(sourceState, transition.getSourceState());
        assertEquals(transitionSymbol, transition.getTransitionSymbol());
        assertEquals(targetState, transition.getTargetState());
    }
    
    @Test
    public void testUpdateTransitionSymbol() {
        String newSymbol = "b";
        transition.updateTransitionSymbol(newSymbol);
        assertEquals(newSymbol, transition.getTransitionSymbol());
    }
    
    @Test
    public void testSetTargetState() {
        State newTargetState = new State("q2");
        transition.setTargetState(newTargetState);
        assertEquals(newTargetState, transition.getTargetState());
    }
    
    @Test
    public void testSetSourceState() {
        State newSourceState = new State("q3");
        transition.setSourceState(newSourceState);
        assertEquals(newSourceState, transition.getSourceState());
    }
    
    @Test
    public void testCopy() {
        Transition copiedTransition = transition.copy();
        assertEquals(transition.getSourceState(), copiedTransition.getSourceState());
        assertEquals(transition.getTransitionSymbol(), copiedTransition.getTransitionSymbol());
        assertEquals(transition.getTargetState(), copiedTransition.getTargetState());
    }
    
    @Test
    public void testEquals() {
        assertFalse(transition.equals(new Transition(sourceState, targetState, "b")));
        
        Transition equalTransition = new Transition(sourceState, targetState, transitionSymbol);
        assertTrue(transition.equals(equalTransition));
    }
}
