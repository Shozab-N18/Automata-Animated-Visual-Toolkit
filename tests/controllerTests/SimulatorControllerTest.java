package tests.controllerTests;

import static org.junit.Assert.*;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;


import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;

import javafx.embed.swing.JFXPanel;
import controller.SimulatorController;
import model.*;
import view.AutomataSimulatorView;
import components.SpecialSymbols;
import components.serialization.*;

public class SimulatorControllerTest {
    private SimulatorController simulatorController;
    private AutomataSimulatorView automataSimulatorView;

    private Automata automata;
    private Map<State, SerializablePoint2D> states;
    private State state1;
    private State state2;
    private State state3;
    
    private Transition transition1;

    @Mock
    private AutomataIO automataIO;

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
        
        state1.setStartingState(true);
        state2.setAcceptingState(true);
        
        transition1 = new Transition(state1, state2, "a");
        state1.addTransition(transition1);
        
        automata = new Automata(states);
        simulatorController = new SimulatorController(automata);
        
        simulatorController.updateTransitionTable();
        
        automataSimulatorView = new AutomataSimulatorView(automata);
    }

    @Test
    public void testFindAcceptingPath() throws InterruptedException {
        simulatorController.findAcceptingPath("a");
        assertEquals(1, simulatorController.getSimulationLog().getItems().size());
        assertEquals("Accepted", simulatorController.getSimulationLog().getItems().get(0).get(1).get());
        assertEquals("q1 → q2", simulatorController.getSimulationLog().getItems().get(0).get(2).get());

        simulatorController.findAcceptingPath("b");
        assertEquals(1, simulatorController.getSimulationLog().getItems().size());

        state2.addTransition(new Transition(state2, state3, "b"));
        state3.setAcceptingState(true);
        simulatorController.updateTransitionTable();

        simulatorController.getSimulationLog().getItems().clear();
        simulatorController.findAcceptingPath("ab");
        
        assertEquals(1, simulatorController.getSimulationLog().getItems().size());
        assertEquals("Accepted", simulatorController.getSimulationLog().getItems().get(0).get(1).get());
        assertEquals("q1 → q2 → q3", simulatorController.getSimulationLog().getItems().get(0).get(2).get());

        simulatorController.getSimulationLog().getItems().clear();
        state3.setAcceptingState(false);
        state1.setAcceptingState(true);
        state3.addTransition(new Transition(state3, state1, SpecialSymbols.EPSILON.toString()));
        simulatorController.updateTransitionTable();
        simulatorController.findAcceptingPath("ab");

        assertEquals(1, simulatorController.getSimulationLog().getItems().size());
        assertEquals("Accepted", simulatorController.getSimulationLog().getItems().get(0).get(1).get());
        assertEquals("q1 → q2 → q3 → q1", simulatorController.getSimulationLog().getItems().get(0).get(2).get());
    }

    @Test
    public void testSimulate() {
        state1.setAcceptingState(false);
        simulatorController.simulate("a");
        simulatorController.simulateStepForward();
        simulatorController.simulateStepForward();
        assertEquals(1, simulatorController.getSimulationLog().getItems().size());
        assertEquals("a", simulatorController.getSimulationLog().getItems().get(0).get(0).get());
        assertEquals("Accepted", simulatorController.getSimulationLog().getItems().get(0).get(1).get());
        assertEquals("q1 → q2", simulatorController.getSimulationLog().getItems().get(0).get(2).get());

        simulatorController.simulate("b");
        simulatorController.simulateStepForward();
        assertEquals(2, simulatorController.getSimulationLog().getItems().size());
        assertEquals("b", simulatorController.getSimulationLog().getItems().get(1).get(0).get());
        assertEquals("Rejected", simulatorController.getSimulationLog().getItems().get(1).get(1).get());
        assertEquals("q1", simulatorController.getSimulationLog().getItems().get(1).get(2).get());

        simulatorController.simulate("");
        simulatorController.simulateStepForward();
        assertEquals(3, simulatorController.getSimulationLog().getItems().size());
        assertEquals("None", simulatorController.getSimulationLog().getItems().get(2).get(0).get());    
        assertEquals("Rejected", simulatorController.getSimulationLog().getItems().get(2).get(1).get());
        assertEquals("q1", simulatorController.getSimulationLog().getItems().get(2).get(2).get());

        simulatorController.simulate("aab");
        simulatorController.simulateStepForward();
        simulatorController.simulateStepForward();
        assertEquals(4, simulatorController.getSimulationLog().getItems().size());
        assertEquals("aab", simulatorController.getSimulationLog().getItems().get(3).get(0).get());
        assertEquals("Accepted", simulatorController.getSimulationLog().getItems().get(0).get(1).get());
        assertEquals("q1 → q2", simulatorController.getSimulationLog().getItems().get(0).get(2).get());
    }

    @Test
    public void testSimulateStepForward() {
        simulatorController.simulate("a");
        simulatorController.simulateStepForward();
        simulatorController.simulateStepForward();

        assertEquals(1, simulatorController.getSimulationLog().getItems().size());
        assertEquals("a", simulatorController.getSimulationLog().getItems().get(0).get(0).get());
        assertEquals("Accepted", simulatorController.getSimulationLog().getItems().get(0).get(1).get());
        assertEquals("q1 → q2", simulatorController.getSimulationLog().getItems().get(0).get(2).get());
    }

    @Test
    public void testSimulateStepBackward() {
        simulatorController.simulate("a");
        simulatorController.simulateStepForward();
        simulatorController.simulateStepForward();
        simulatorController.simulateStepBackward();

        assertEquals(1, simulatorController.getSimulationLog().getItems().size());
        assertEquals("a", simulatorController.getSimulationLog().getItems().get(0).get(0).get());
        assertEquals("Accepted", simulatorController.getSimulationLog().getItems().get(0).get(1).get());
        assertEquals("q1 → q2", simulatorController.getSimulationLog().getItems().get(0).get(2).get());
    }

}
