package tests.controllerTests;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import components.SpecialSymbols;
import components.serialization.SerializablePoint2D;
import javafx.embed.swing.JFXPanel;
import javafx.application.Platform;

import controller.SubsetConstructionController;
import model.*;

public class SubsetConstructionControllerTest {
    private SubsetConstructionController automataController;
    private Map<State, SerializablePoint2D> states;
    private Automata automata;
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
        
        automata = new Automata(states);
        automataController = new SubsetConstructionController(automata);
        automataController.updateTransitionTable();
        
        transition1 = new Transition(state1, state2, "a");
        state1.addTransition(transition1);
        automataController.updateTransitionTable();
    }

    @Test
    public void testConvertToDFA() throws InterruptedException {
        Platform.runLater(() -> {
            automataController.convertToDFA();
            assertNotNull(automataController.getAutomata());
        });
    }

    @Test
    public void testConstructorAndGetters() {
        assertNotNull(automataController.getAutomata());
        assertNull(automataController.getSelectedState());
        assertNull(automataController.getRejectedState());
        assertNotNull(automataController.getTransitionTable());
    }


    @Test
    public void testRemoveTransitionsToState() {
        state1.addTransition(new Transition(state1, state2, "a"));
        state1.addTransition(new Transition(state1, state3, "b"));
        state2.addTransition(new Transition(state2, state3, "a"));
        automataController.removeTransitionsToState(state3);
        assertNull(state1.getTransitionBySymbol("b"));
        assertNull(state2.getTransitionBySymbol("a"));
    }
    
    @Test
    public void testGetNewStatesSetForState() {
        Set<String> newSet = automataController.getNewStatesSetForState(states.keySet());
        System.out.println(newSet);
        assertTrue(newSet.contains("{ q1 q2 }"));
        assertTrue(newSet.contains("{ q1 q3 }"));
        assertTrue(newSet.contains("{ q2 q3 }"));
        assertTrue(newSet.contains("{ q1 q2 q3 }")); 
    }
}
