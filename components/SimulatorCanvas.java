package components;

import javafx.scene.control.ContextMenu;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseButton;

import java.util.Stack;
import java.util.Set;

import components.serialization.SerializablePoint2D;
import controller.SimulatorController;
import model.State;
/*
 * SimulatorCanvas: This class is responsible for coloring the states of the automata based on the current state of the simulator.
 */
public class SimulatorCanvas extends MainCanvas<SimulatorController> {
    
    public SimulatorCanvas(SimulatorController controller) {
        super(controller);
        this.getStylesheets().add(getClass().getResource("/css/SimulatorCanvas.css").toExternalForm());
        canvas.setId("automataCanvas");
        contextMenu = new ContextMenu();
    }
    
    @Override
    protected void handleStateDrag(SerializablePoint2D dragPoint) {
        return;
    }
    
    /**
     * Choses the selected next state based on the click point on the canvas
     * @param clickPoint
     */
    protected void handleStateSelect(SerializablePoint2D clickPoint) {
        for (StateUI stateUI : stateUIs) {
            State state = stateUI.getState();
            boolean isStatePossibleNextState = false;
            if (controller.getPossibleNextStates() != null && controller.getPossibleNextStates().contains(state)) {
                isStatePossibleNextState = true;
            }
            double distance = clickPoint.distance(stateUI.getPosition());
            
            // If click is within the radius of the state, select the state
            if (distance <= StateUI.getRadius()) {
                if (isStatePossibleNextState) {
                    controller.chooseSelectedNextState(state);
                    lastMousePosition = clickPoint;
                }
                MainCanvas.drawAllCanvases();
                break;
            }
        }
    }
    
    /**
     * Sets the color of the state based on the current state of the simulator
     * @param state
     */
    @Override
    protected Color getStateInnerColor(State state) {
        State selectedState = controller.getSelectedState();
        Stack<State> previousStates = controller.getPreviousStates();
        Set<State> possibleNextStates = controller.getPossibleNextStates();
        
        if (possibleNextStates != null && possibleNextStates.contains(state)) {
            return Color.LIGHTSALMON;
        }
        else if (selectedState != null && selectedState.equals(state)) {
            return Color.LIGHTGREEN;
        }  
        else if (!previousStates.isEmpty() && previousStates.contains(state)) {
            return Color.LIGHTBLUE;
        } 
        else if (controller.getRejectedState() != null && controller.getRejectedState().equals(state)) {
            return Color.LIGHTCORAL;
        } 
        else {
            return Color.LIGHTGRAY;
        }
    }

    @Override
    protected void setCanvasEvents() {
        super.setCanvasEvents();
        
        canvas.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                handleStateSelect(new SerializablePoint2D(e.getX(), e.getY()));
            }
            if (contextMenu != null && contextMenu.isShowing()) {
                contextMenu.hide();
            }
        });
    }
}