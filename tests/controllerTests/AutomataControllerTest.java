package tests.controllerTests;

import static org.junit.Assert.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javafx.embed.swing.JFXPanel;

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.beans.property.StringProperty;
import javafx.application.Platform;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import model.*;
import components.serialization.SerializablePoint2D;
import controller.AutomataController;
import components.SpecialSymbols;

public class AutomataControllerTest {
    private AutomataController automataController;
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
        
        automataController = new AutomataController(states);
        automataController.updateTransitionTable();
        
        transition1 = new Transition(state1, state2, "a");
        state1.addTransition(transition1);
        automataController.updateTransitionTable();
    }

    @Test
    public void testConstructorAndGetters() {
        assertNotNull(automataController.getAutomata());
        assertNull(automataController.getSelectedState());
        assertNull(automataController.getRejectedState());
        assertNotNull(automataController.getTransitionTable());
    }

    @Test
    public void testGetStartingState() {
        assertNull(automataController.getStartingState());
        state1.setStartingState(true);
        assertEquals(state1, automataController.getStartingState());
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
    public void testGetAllStates() {
        String allStates = automataController.getStringAllStates();
        assertTrue(allStates.contains("q1"));
        assertTrue(allStates.contains("q2"));
        assertTrue(allStates.contains("q3"));
    }

    @Test
    public void testGetAlphabet() {
        Set<String> alphabet = new HashSet<>();
        alphabet.add("a");
        alphabet.add("b");
        alphabet.add(SpecialSymbols.EPSILON.toString());
        assertEquals(alphabet , automataController.getAutomata().getAlphabet());
        automataController.getAutomata().setAlphabet("ab");
        assertEquals("{a, b, ε}", automataController.getStringAlphabet());
    }

    @Test
    public void testGetStartingStateAsString() {
        assertEquals("", automataController.getStringStartingState());
        state1.setStartingState(true);
        assertEquals("q1", automataController.getStringStartingState());
    }

    @Test
    public void testGetAllAcceptingStates() {
        assertEquals(Set.of(), automataController.getAutomata().getAcceptingStates());
        state1.setAcceptingState(true);
        state2.setAcceptingState(true);
        assertEquals(Set.of(state1, state2), automataController.getAutomata().getAcceptingStates());
    }

    @Test
    public void testUpdateStates() {
        assertEquals(3, automataController.getAutomata().getStates().size());
        Map<State, SerializablePoint2D> newStates = new HashMap<>();
        State newState1 = new State("q0");
        State newState2 = new State("q4");
        newStates.put(newState1, new SerializablePoint2D());
        newStates.put(newState2, new SerializablePoint2D());
        automataController.updateStates(newStates);
        assertEquals(2, automataController.getAutomata().getStates().size());
    }

    @Test
    public void testUpdateTransitionTable() {
        automataController.updateTransitionTable();
        TableView<ObservableList<StringProperty>> table = automataController.getTransitionTable();
        assertEquals(1, table.getItems().size());
        assertEquals(3, table.getColumns().size());
    }

    @Test
    public void testUpdateControllerTable() throws InterruptedException {
        Platform.runLater(() -> {
            automataController.updateControllerTable(0, 0, "q2");
            assertNull(state1.getTransitionBySymbol("a"));
            assertTrue(state2.getTransitionBySymbol("a").getTargetState().equals(state2));
        });
        Thread.sleep(1000); 
    }

    @Test
    public void testUpdateTransitionSymbol() throws InterruptedException {
        Platform.runLater(() -> {
            automataController.updateTransitionSymbol(transition1, "b");
            assertNull(state1.getTransitionBySymbol("a"));
            assertNotNull(state1.getTransitionBySymbol("b"));
        });
        Thread.sleep(1000);
    }

    @Test
    public void testClearAutomata() {
        automataController.clearAutomata();
        assertEquals(0, automataController.getAutomata().getStates().size());
        assertEquals(0, automataController.getAutomata().getAlphabet().size());
        assertNull(automataController.getSelectedState());
    }

    @Test
    public void testSetAlphabet() {
        automataController.setAlphabet("abc");
        assertEquals("{a, b, c, ε}", automataController.getStringAlphabet());
    }
}
