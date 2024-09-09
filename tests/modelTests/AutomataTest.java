package tests.modelTests;

import static org.junit.Assert.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

import components.serialization.SerializablePoint2D;
import model.*;

public class AutomataTest {
    private Automata automata;
    private State state1;
    private State state2;
    private State state3;

    @BeforeEach
    public void setUp() {
        Map<State, SerializablePoint2D> states = new HashMap<>();
        state1 = new State("q1");
        state2 = new State("q2");
        state3 = new State("q3");
        states.put(state1, new SerializablePoint2D());
        states.put(state2, new SerializablePoint2D());
        states.put(state3, new SerializablePoint2D());
        automata = new Automata(states);
        automata.setStartingState(state1);
        automata.setAsDFA();
        automata.setAlphabet("ab");
    }

    @Test
    public void testConstructorAndGetters() {
        assertTrue(automata.isDFA());
        assertTrue(automata.getAlphabet().contains("a"));
        assertTrue(automata.getAlphabet().contains("b"));
        assertFalse(automata.isValid());
    }

    @Test
    public void testSetAsDFA() {
        automata.setAsDFA();
        assertTrue(automata.isDFA());
        assertFalse(automata.getAlphabet().contains("ε"));
        assertFalse(automata.isValid());
    }

    @Test
    public void testSetAsNFA() {
        automata.setAsNFA();
        assertFalse(automata.isDFA());
        assertTrue(automata.getAlphabet().contains("ε"));
        assertFalse(automata.isValid());
    }

    @Test
    public void testSetAlphabet() {
        automata.setAlphabet("abc");
        assertTrue(automata.getAlphabet().contains("a"));
        assertTrue(automata.getAlphabet().contains("b"));
        assertTrue(automata.getAlphabet().contains("c"));
        assertFalse(automata.getAlphabet().contains("ε"));
        assertFalse(automata.isValid());
    }

    @Test
    public void testSetStartingState() {
        State newState = new State("q0");
        automata.setStartingState(newState);
        assertEquals(newState, automata.getStartingState());
        assertFalse(automata.isValid());
    }

    @Test
    public void testGetStates() {
        Map<State, SerializablePoint2D> states = automata.getStates();
        assertEquals(3, states.size());
        assertTrue(states.containsKey(state1));
        assertTrue(states.containsKey(state2));
        assertTrue(states.containsKey(state3));
    }

    @Test
    public void testSetStates() {
        Map<State, SerializablePoint2D> newStates = new HashMap<>();
        State newState1 = new State("q0");
        State newState2 = new State("q4");
        newStates.put(newState1, new SerializablePoint2D());
        newStates.put(newState2, new SerializablePoint2D());
        automata.setStates(newStates);
        assertEquals(2, automata.getStates().size());
        assertTrue(automata.getStates().containsKey(newState1));
        assertTrue(automata.getStates().containsKey(newState2));
        assertFalse(automata.isValid());
    }

    @Test
    public void testHasStartingState() {
        assertEquals(state1, automata.getStartingState());
    }

    @Test
    public void testHasAcceptingState() {
        assertFalse(automata.hasAcceptingState());
    }

    @Test
    public void testHasAlphabet() {
        assertTrue(automata.hasAlphabet());
    }

    @Test
    public void testIsValid() {
        assertFalse(automata.isValid());
    }

    @Test
    public void testClear() {
        automata.clear();
        assertEquals(0, automata.getStates().size());
        assertEquals(0, automata.getAlphabet().size());
        assertNull(automata.getStartingState());
        assertFalse(automata.isValid());
    }
}