package tests.modelTests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import components.SpecialSymbols;
import model.State;
import model.Transition;

public class StateTest {
    private State state1;
    private State state2;
    private State state3;
    private Transition transition1;
    private Transition transition2;
    private Transition transition3;
    
    @BeforeEach
    public void setUp() {
        state1 = new State("q1");
        state2 = new State("q2");
        state3 = new State("q3");
        transition1 = new Transition(state1, state2, "a");
        transition2 = new Transition(state1, state2, "b");
        transition3 = new Transition(state1, state3, "c");
    }
    
    @Test
    public void testConstructorAndGetters() {
        assertEquals("q1", state1.getName());
        assertFalse(state1.isAcceptingState());
        assertFalse(state1.isStartingState());
        assertTrue(state1.getTransitions().isEmpty());
        assertFalse(state1.isDiscarded());
    }
    
    @Test
    public void testSetters() {
        state1.setAcceptingState(true);
        assertTrue(state1.isAcceptingState());
        
        state1.setStartingState(true);
        assertTrue(state1.isStartingState());
        
        state1.toggleAcceptingState();
        assertFalse(state1.isAcceptingState());
        
        state1.toggleStartingState();
        assertFalse(state1.isStartingState());
    }
    
    @Test
    public void testUpdateName() {
        state1.updateName("newName");
        assertEquals("newName", state1.getName());
    }
    
    @Test
    public void testGetTransitionBySymbol() {
        state1.addTransition(transition1);
        state1.addTransition(transition2);
        
        assertEquals(transition1, state1.getTransitionBySymbol("a"));
        assertEquals(transition2, state1.getTransitionBySymbol("b"));
        assertNull(state1.getTransitionBySymbol("c"));
    }
    
    @Test
    public void testGetTransitionSymbolToTargetState() {
        state1.addTransition(transition1);
        state1.addTransition(transition3);
        
        assertEquals("a", state1.getTransitionSymbolToTargetState(state2));
        assertEquals("c", state1.getTransitionSymbolToTargetState(state3));
        assertNull(state1.getTransitionSymbolToTargetState(new State("q4")));
    }
    
    @Test
    public void testGetMultipleTransitions() {
        state1.addTransition(transition1);
        state1.addTransition(transition2);
        
        Map<Set<Transition>, String> multipleTransitions = state1.getMultipleTransitionsToTarget(state2);
        Set<Transition> totalTransitions = multipleTransitions.keySet().iterator().next();

        assertEquals("ab", multipleTransitions.get(Set.of(transition1, transition2)));
        assertEquals(2, totalTransitions.size());
        assertTrue(multipleTransitions.containsKey(Set.of(transition1, transition2)));
    }
    
    @Test
    public void testEpsilonClosure() {
        State state4 = new State("q4");
        Transition epsilonTransition = new Transition(state1, state4, SpecialSymbols.EPSILON.toString());
        state1.addTransition(transition1);
        state1.addTransition(transition2);
        state1.addTransition(epsilonTransition);
        
        Set<State> closure = State.epsilonClosure(state1, new HashSet<>());
        assertEquals(3, state1.getTransitions().size());
        assertEquals(2, closure.size());
        assertTrue(closure.contains(state1));
        assertFalse(closure.contains(state2));
        assertFalse(closure.contains(state3));
        assertTrue(closure.contains(state4));
    }
    
    @Test
    public void testGetNextStatesFromSymbol() {
        state1.addTransition(transition1);
        state1.addTransition(transition2);
        
        Set<State> nextStates = state1.getNextStatesFromSymbol("a");
        assertEquals(1, nextStates.size());
        assertTrue(nextStates.contains(state2));
        
        nextStates = state1.getNextStatesFromSymbol("b");
        assertEquals(1, nextStates.size());
        assertTrue(nextStates.contains(state2));
        
        nextStates = state1.getNextStatesFromSymbol("c");
        assertTrue(nextStates.isEmpty());
    }
    
    @Test
    public void testGetNonEpsilonNextStatesFromSymbol() {
        state1.addTransition(transition1);
        state1.addTransition(transition3);
        
        Set<State> nextStates = state1.getNonEpsilonNextStatesFromSymbol("a");
        assertEquals(1, nextStates.size());
        assertTrue(nextStates.contains(state2));
        
        nextStates = state1.getNonEpsilonNextStatesFromSymbol("c");
        assertEquals(1, nextStates.size());
        assertTrue(nextStates.contains(state3));
        
        nextStates = state1.getNonEpsilonNextStatesFromSymbol("b");
        assertTrue(nextStates.isEmpty());
    }
    
    @Test
    public void testGetNextStateFromSymbol() {
        state1.addTransition(transition1);
        assertEquals(state2, state1.getNextStateFromSymbol("a"));
        
        assertNull(state1.getNextStateFromSymbol("b"));
    }
    
    @Test
    public void testClearTransitions() {
        state1.addTransition(transition1);
        state1.addTransition(transition2);
        state1.clearTransitions();
        assertTrue(state1.getTransitions().isEmpty());
    }
    
    @Test
    public void testEqualsMethod() {
        State stateCopy = state1.copy();
        assertTrue(state1.equals(stateCopy));
        
        State differentState = new State("different");
        assertFalse(state1.equals(differentState));
    }
}
