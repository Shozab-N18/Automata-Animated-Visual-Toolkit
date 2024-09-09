package components;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.Map;

import app.App;
import components.serialization.SerializablePoint2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import controller.AutomataController;
import model.State;
import model.Transition;

/*      
 * MainCanvas: A superclass for all canvases in the application. 
 * This class provides the basic functionality for drawing states and transitions on the canvas.
 * It parametrizes the type of controller that is associated with the canvas and provides methods for drawing the canvas, states, and transitions.
 */
public class MainCanvas<ControllerT extends AutomataController> extends StackPane {
    protected Label coordinatesLabel;
    protected boolean isMouseOverState = false;
    protected boolean isMouseOverTransition = false;
    
    protected SerializablePoint2D lastMousePosition = null;
    protected boolean isDraggingState = false;
    protected Transition lastSelectedTransition = null;
    protected static State interactingState = null;
    protected State rejectedState = null;
    
    protected ContextMenu contextMenu;
    
    protected Canvas canvas;
    protected ControllerT controller;
    
    protected static List<MainCanvas<? extends AutomataController>> instances = new ArrayList<>();
    
    protected List<StateUI> stateUIs = new ArrayList<>();
    protected List<TransitionUI> transitionUIs = new ArrayList<>();
    
    protected SerializablePoint2D newTransitionDragPoint = null;
    protected static Map<Transition, SerializablePoint2D> transitionDragPoints = new HashMap<>();
    
    public MainCanvas(ControllerT controller) {
        this.controller = controller;
        this.setStyle("-fx-background-color: white;");
        instances.add(this);
        
        canvas = new Canvas(2000, 2000);
        canvas.setFocusTraversable(false);
        getChildren().add(canvas);
        
        
        coordinatesLabel = new Label();
        coordinatesLabel.setId("coordinatesLabel");
        StackPane.setAlignment(coordinatesLabel, Pos.BOTTOM_RIGHT);  
        getChildren().add(coordinatesLabel);
        
        drawCanvas();
        setCanvasEvents();
    }
    
    /**
     * Sets the controller associated with the canvas.
     * @param controller
     */
    public void setController(ControllerT controller) {
        this.controller = controller;
    }
    
    /*
     * Draws all the canvases in the application.
     */
    public static void drawAllCanvases() {
        for (MainCanvas<? extends AutomataController> canvas : instances) {
            canvas.drawCanvas();
        }
    }
    
    public static void drawSimulatorCanvas() {
        for (MainCanvas<? extends AutomataController> canvas : instances) {
            if (canvas instanceof SimulatorCanvas) {
                canvas.drawCanvas();
            }
        }
    }
    
    /*
     * Clears the canvas and redraws the states and transitions on the canvas.
     */
    public void drawCanvas() {
        stateUIs.clear();
        transitionUIs.clear();
        
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.WHITE);
        
        if (this instanceof SimulatorCanvas) {
            drawDottedCanvas(gc);
        } else if (this instanceof ConstructionCanvas || this instanceof SubsetConstructionCanvas) {
            drawSquareGridCanvas(gc);
        }
        
        drawStates(gc);
        drawTransitions(gc);
    }
    
    /**
     * Draws the states on the canvas and adds them to the list of stateUIs.
     * @param gc The graphics context of the canvas.
     */
    protected void drawStates(GraphicsContext gc) {
        for (State state : controller.getAutomata().getStates().keySet()) {
            StateUI stateUI = new StateUI(state, controller.getAutomata().getStates().get(state));
            
            stateUI.setStateOutlineColor(getStateOutlineColor(state));
            stateUI.setStateInnerColor(getStateInnerColor(state));
            stateUI.setStateTextColor(getStateTextColor(state));
            stateUI.draw(gc);
            stateUIs.add(stateUI);
        }
    }
    
    /**
     * Draws the transitions on the canvas and adds them to the list of transitionUIs.
     * @param gc The graphics context of the canvas.
     */
    protected void drawTransitions(GraphicsContext gc) {
        for (StateUI sourceStateUI : stateUIs) {
            for (Transition transition : sourceStateUI.getState().getTransitions()) {
                StateUI targetStateUI = getStateUIByState(transition.getTargetState());
                if (targetStateUI != null) {
                    TransitionUI transitionUI = new TransitionUI(transition, sourceStateUI, targetStateUI);
                    if (transitionDragPoints.containsKey(transition)) {
                        transitionUI.setSelfTransitionPosition(transitionDragPoints.get(transition));
                    }
                    transitionUI.draw(gc);
                    transitionUIs.add(transitionUI);
                }
            }
        }
    }
    
    protected StateUI getStateUIByState(State state) {
        for (StateUI stateUI : stateUIs) {
            if (stateUI.getState().equals(state)) {
                return stateUI;
            }
        }
        return null;
    }
    
    protected TransitionUI getTransitionUIByTransition(Transition transition) {
        for (TransitionUI transitionUI : transitionUIs) {
            if (transitionUI.getTransition().equals(transition)) {
                return transitionUI;
            }
        }
        return null;
    }
    
    /**
     * Shows a context menu at the specified point with the specified menu items.
     * @param point
     * @param items
     */
    protected void showContextMenu(SerializablePoint2D point, MenuItem... items) {
        contextMenu.getItems().clear();
        contextMenu.getItems().addAll(items);
        SerializablePoint2D screenPoint = new SerializablePoint2D(canvas.localToScreen(new Point2D(point.getX(), point.getY())));
        
        contextMenu.show(canvas, screenPoint.getX(), screenPoint.getY());
    }
    
    /**
     * Checks if the mouse event is over the context menu.
     * @param e
     * @return boolean
     */
    protected boolean isMouseOverContextMenu(MouseEvent e) {
        double mouseX = e.getScreenX();
        double mouseY = e.getScreenY();
        double menuX = contextMenu.getX();
        double menuY = contextMenu.getY();
        
        return (mouseX >= menuX && mouseX <= menuX + contextMenu.getWidth() && mouseY >= menuY && mouseY <= menuY + contextMenu.getHeight());
    }
    
    // Drawing helper methods
    
    protected Color getStateOutlineColor(State state) {
        if (interactingState != null && interactingState.equals(state)) {
            return Color.BLACK.darker();
        } else {
            return Color.BLACK.darker();
        }
    }
    
    protected Color getStateInnerColor(State state) {
        if (interactingState != null && interactingState.equals(state)) {
            return Color.LIGHTGRAY;
        } else {
            return Color.LIGHTGRAY;
        }
    }
    
    protected Color getStateTextColor(State state) {
        if (interactingState != null && interactingState.equals(state)) {
            return Color.BLACK;
        } else {
            return Color.BLACK;
        }
    }
    
    // Mouse event helper methods
    
    protected boolean isMouseOverCanvas(SerializablePoint2D mousePoint) {
        return (mousePoint.getX() >= 0 && mousePoint.getY() >= 0 && mousePoint.getX() <= getWidth() && mousePoint.getY() <= getHeight());
    }
    
    protected boolean isMouseOverState(SerializablePoint2D mousePoint) {
        for (StateUI stateUI : stateUIs) {
            if (stateUI.isMouseOverState(mousePoint)) {
                return true;
            }
        }
        return false;
    }
    
    protected boolean isMouseOverTransition(SerializablePoint2D mousePoint) {
        for (TransitionUI transitionUI : transitionUIs) {
            if (transitionUI.isMouseOverTransition(mousePoint)) {
                lastSelectedTransition = transitionUI.getTransition();
                return true;
            }
        }
        
        return false;
    }

    protected void drawDottedCanvas(GraphicsContext gc) {
        int gridSize = 15;
        gc.setFill(Color.LIGHTGRAY);
        
        int scaledWidth = (int) getWidth();
        int scaledHeight = (int) getHeight();
        
        for (int x = 0; x < scaledWidth; x += gridSize) {
            for (int y = 0; y < scaledHeight; y += gridSize) {
                gc.fillRect(x, y, 2, 2);
            }
        }
    }
    
    protected void drawSquareGridCanvas(GraphicsContext gc) {
        int gridSize = 25;
        gc.setStroke(Color.LIGHTGRAY);
        
        int scaledWidth = (int) getWidth();
        int scaledHeight = (int) getHeight();
        
        for (int x = 0; x < scaledWidth; x += gridSize) {
            gc.strokeLine(x, 0, x, scaledHeight);
        }
        
        for (int y = 0; y < scaledHeight; y += gridSize) {
            gc.strokeLine(0, y, scaledWidth, y);
        }
    }
    
    protected void updateCoordinatesLabel(SerializablePoint2D point) {
        int scaledX = (int) point.getX();
        int scaledY = (int) point.getY();
        coordinatesLabel.setText("Coordinates: (" + scaledX + ", " + scaledY + ")");
    }    
    
    // Event handling
    
    protected void handleStateDrag(SerializablePoint2D dragPoint) {
        if (isDraggingState && interactingState != null && lastMousePosition != null) {
            SerializablePoint2D currentStatePosition = controller.getAutomata().getStates().get(interactingState);
            
            double deltaX = dragPoint.getX() - lastMousePosition.getX();
            double deltaY = dragPoint.getY() - lastMousePosition.getY();
            SerializablePoint2D newPosition = new SerializablePoint2D(currentStatePosition.getX() + deltaX, currentStatePosition.getY() + deltaY);
            
            // Update state position
            controller.getAutomata().getStates().put(interactingState, newPosition);
            lastMousePosition = dragPoint;
            
            // Update drag point of self-transition
            for (Transition transition : interactingState.getTransitions()) {
                SerializablePoint2D selfTransitionDragPoint = getTransitionUIByTransition(transition).getDragPoint();
                if (getTransitionUIByTransition(transition).isSelfTransition() && selfTransitionDragPoint != null) {
                    transitionDragPoints.put(transition, new SerializablePoint2D(
                        selfTransitionDragPoint.getX() + deltaX,
                        selfTransitionDragPoint.getY() + deltaY
                    ));
                }
            }
            
            MainCanvas.drawAllCanvases();
        }
        isMouseOverState = isMouseOverState(dragPoint);
        updateCoordinatesLabel(dragPoint);
    }
    
    protected void handleSelfTransitionDrag(SerializablePoint2D dragPoint) {
        if (lastSelectedTransition != null) {
            transitionDragPoints.put(lastSelectedTransition, dragPoint);
        }
    }
    
    protected void handleMousePress(SerializablePoint2D pressPoint, MouseEvent e) {
        Map<State, SerializablePoint2D> states = controller.getAutomata().getStates();
        
        for (Map.Entry<State, SerializablePoint2D> entry : states.entrySet()) {
            State state = entry.getKey();
            SerializablePoint2D position = entry.getValue();
            int radius = StateUI.getRadius();
            
            double distance = pressPoint.distance(position);
            
            if (distance <= radius) {
                if (e.getButton() == MouseButton.SECONDARY) {
                    showStateOptions(pressPoint, state);
                } 
                else {
                    interactingState = state;
                    lastMousePosition = pressPoint;
                    isDraggingState = true;
                }
                break;
            }
        }
    }  
    
    protected void showStateOptions(SerializablePoint2D pressPoint, State state) {
        return;
    }

    protected StateUI chooseStateUI(SerializablePoint2D pressPoint) {
        for (StateUI stateUI : stateUIs) {
            if (stateUI.isMouseOverState(pressPoint)) {
                return stateUI;
            }
        }
        return null;
    }
    
    protected void handleMouseRelease() {
        isDraggingState = false;
        interactingState = null;
        lastMousePosition = null;
        lastSelectedTransition = null;
        MainCanvas.drawAllCanvases();
    }
    
    public Label getCoordinatesLabel() {
        return coordinatesLabel;
    }
    
    protected void setCanvasEvents() {
        canvas.setOnMouseEntered(e -> coordinatesLabel.setVisible(true));
        
        canvas.setOnMouseExited(e -> coordinatesLabel.setVisible(false));
        
        canvas.setOnMouseClicked(e -> {
            if (contextMenu != null && contextMenu.isShowing()) {
                contextMenu.hide();
            }
        });
        
        canvas.setOnMousePressed(e -> handleMousePress(new SerializablePoint2D(e.getX(), e.getY()), e));
        
        canvas.setOnMouseReleased(e -> handleMouseRelease());
    }
}