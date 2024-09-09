package tests.controllerTests;

import static org.junit.Assert.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import controller.*;
import javafx.embed.swing.JFXPanel;
import model.*;
import view.SubsetConstructionView;
import components.SpecialSymbols;
import components.serialization.SerializablePoint2D;

public class ConstructionControllerTest {
    private ConstructionController constructionController;
    private SubsetConstructionView subsetConstructionView;
    private Automata automata;
    private Map<State, SerializablePoint2D> states;
    private State state1;
    private State state2;
    private State state3;
    
    private Transition transition1;

    @BeforeEach
    public void setUp() {
        JFXPanel jfxPanel = new JFXPanel();
        states = new HashMap<>();
        state1 = new State("q1");
        state2 = new State("q2");
        state3 = new State("q3");
        states.put(state1, new SerializablePoint2D());
        states.put(state2, new SerializablePoint2D());
        states.put(state3, new SerializablePoint2D());
        
        transition1 = new Transition(state1, state2, "a");
        state1.addTransition(transition1);
        
        automata = new Automata();
        constructionController = new ConstructionController(automata);
        constructionController.updateTransitionTable();

        subsetConstructionView = new SubsetConstructionView(states);
    }

    @Test
    public void testAddState() {
        assertEquals(0, automata.getStates().size());
        constructionController.addState(new SerializablePoint2D());
        assertEquals(1, automata.getStates().size());
    }

    @Test
    public void testDeleteState() {
        State state = new State("q0");
        automata.getStates().put(state, new SerializablePoint2D());
        assertEquals(1, automata.getStates().size());
        constructionController.deleteState(state);
        assertEquals(0, automata.getStates().size());
    }

    @Test
    public void testAddTransition() {
        State fromState = new State("q0");
        State toState = new State("q1");
        automata.getStates().put(fromState, new SerializablePoint2D());
        automata.getStates().put(toState, new SerializablePoint2D());
        assertEquals(0, fromState.getTransitions().size());
        constructionController.addTransition(fromState, toState, "a");
        assertEquals(1, fromState.getTransitions().size());
    }

    @Test
    public void testRemoveTransition() {
        State fromState = new State("q0");
        State toState = new State("q1");
        fromState.addTransition(toState, "a");
        assertEquals(1, fromState.getTransitions().size());
        constructionController.removeTransition(fromState.getTransitions().iterator().next());
        assertEquals(0, fromState.getTransitions().size());
    }

    @Test
    public void testUpdateStateName() {
        State state = new State("q0");
        automata.getStates().put(state, new SerializablePoint2D());
        assertEquals("q0", state.getName());
        constructionController.updateStateName(state, "q1");
        assertEquals("q1", state.getName());
    }

    @Test
    public void testToggleAcceptingState() {
        State state = new State("q0");
        automata.getStates().put(state, new SerializablePoint2D());
        assertFalse(state.isAcceptingState());
        constructionController.toggleAcceptingState(state);
        assertTrue(state.isAcceptingState());
    }

    @Test
    public void testToggleStartingState() {
        State state = new State("q0");
        automata.getStates().put(state, new SerializablePoint2D());
        assertFalse(state.isStartingState());
        constructionController.toggleStartingState(state);
        assertTrue(state.isStartingState());
    }

    @Test
    public void testUpdateAutomata() {
        assertEquals(0, automata.getStates().size());
        State state = new State("q0");
        automata.getStates().put(state, new SerializablePoint2D());
        assertEquals(1, constructionController.getAutomata().getStates().size());
        Automata newAutomata = new Automata();
        constructionController.updateAutomata(newAutomata);
        assertEquals(0, constructionController.getAutomata().getStates().size());
    }

    @Test
    public void testGetAutomataValidityLabel() {
        assertNotNull(constructionController.getAutomataValidityLabel());
    }

    @Test
    public void testSetAlphabet() {
        constructionController.setAlphabet("abc");
        assertEquals(Set.of("a", "b", "c", SpecialSymbols.EPSILON.toString()), automata.getAlphabet());
    }

    @Test
    public void testClearAutomata() {
        State state = new State("q0");
        automata.getStates().put(state, new SerializablePoint2D());
        assertEquals(1, automata.getStates().size());
        constructionController.clearAutomata();
        assertEquals(0, automata.getStates().size());
    }
}
