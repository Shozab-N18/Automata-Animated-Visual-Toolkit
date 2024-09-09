package components;


import java.util.Map;
import java.util.HashMap;

import components.serialization.SerializablePoint2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import model.State;
import model.Transition;

/*
 * StateUI class is responsible for drawing the state on the canvas.
 */
public class StateUI {
    private static final int CIRCLE_SIZE = 90;
    private State state;
    private SerializablePoint2D position;

    private Color outlineColor;
    private Color innerColor;
    private Color textColor;

    private Map<Map<State, State>, Integer> transitionCount;
    private Map<Map<State, State>, Integer> drawnTransitionCount;
    
    public StateUI(State state, SerializablePoint2D position) {
        this.state = state;
        this.position = position;

        transitionCount = new HashMap<>();
        drawnTransitionCount = new HashMap<>();
        
        for (Transition transition : state.getTransitions()) {
            State sourceState = transition.getSourceState();
            State targetState = transition.getTargetState();
            
            Map<State, State> transitionKey = new HashMap<>();
            transitionKey.put(sourceState, targetState);
            
            if (transitionCount.containsKey(transitionKey)) {
                transitionCount.put(transitionKey, transitionCount.get(transitionKey) + 1);
            } else {
                transitionCount.put(transitionKey, 1);
            }
        }
    }

    public void draw(GraphicsContext gc) {
        setOutlineColor(gc);
            
        gc.fillOval(position.getX() - CIRCLE_SIZE / 2, position.getY() - CIRCLE_SIZE / 2, CIRCLE_SIZE, CIRCLE_SIZE);
        
        // Set state's inner color
        setStateInnerColor(gc);
        
        // Paint state's inner circle
        int innerCircleSize = CIRCLE_SIZE - 4;
        gc.fillOval(position.getX() - innerCircleSize / 2, position.getY() - innerCircleSize / 2, innerCircleSize, innerCircleSize);
        
        if (state.isAcceptingState()) {
            drawAcceptingState(gc, position);
        }
        
        if (state.isStartingState()) {
            drawStartingState(gc, position);
        }
        setStateName(gc, state, position);
    }

    public void setStateOutlineColor(Color color) {
        this.outlineColor = color;
    }

    public void setStateInnerColor(Color color) {
        this.innerColor = color;
    }

    public void setStateTextColor(Color color) {
        this.textColor = color;
    }

    private void setOutlineColor(GraphicsContext gc) {
        gc.setFill(outlineColor);
    }
    
    private void setStateInnerColor(GraphicsContext gc) {
        gc.setFill(innerColor);
    }
    
    private void setStateName(GraphicsContext gc, State state, SerializablePoint2D position) {
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.setFill(textColor);
        
        Font font = gc.getFont();
        
        Text text = new Text(state.getName());
        text.setFont(font);
        
        double textWidth = text.getLayoutBounds().getWidth();
        double textHeight = text.getLayoutBounds().getHeight();
        gc.fillText(state.getName(), position.getX() - textWidth / 2, position.getY() + textHeight / 4);
    }
    
    private void drawAcceptingState(GraphicsContext gc, SerializablePoint2D position) {
        // Draw state's outer border circle
        int outerCircleSize = CIRCLE_SIZE + 4;
        int gapSize = 2;
        
        gc.setFill(Color.BLACK.darker());
        gc.setStroke(Color.BLACK.darker());
        gc.setLineWidth(2.0);
        
        // Draw the outer circle with a gap
        gc.strokeOval(position.getX() - outerCircleSize / 2 - gapSize, position.getY() - outerCircleSize / 2 - gapSize, outerCircleSize + 2 * gapSize, outerCircleSize + 2 * gapSize);
        
        gc.setLineWidth(1.0);
    }
    
    private void drawStartingState(GraphicsContext gc, SerializablePoint2D position) {
        int arrowSize = 15;
        
        double startX = position.getX() - CIRCLE_SIZE;
        double startY = position.getY();
        
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2.0);
        
        gc.strokeLine(startX, startY, startX + CIRCLE_SIZE / 2, startY);
        
        drawArrowHead(gc, startX + CIRCLE_SIZE / 2, startY, 1, 0, arrowSize);
        
        Font font = gc.getFont();
        
        Text startText = new Text("START");
        startText.setFont(font);
        
        double textWidth = startText.getLayoutBounds().getWidth();
        double textHeight = startText.getLayoutBounds().getHeight();
        double textX = startX - textWidth - 5;
        double textY = startY + textHeight / 4;
        
        gc.setFill(Color.BLACK);
        gc.fillText("START", textX, textY);
        
        gc.setLineWidth(1.0);
    } 

    private void drawArrowHead(GraphicsContext gc, double x, double y, double nx, double ny, double size) {
        double angle = Math.atan2(ny, nx);
        double arrowAngle = Math.toRadians(20);
        
        // Calculate the two points of the arrowhead
        double x1 = x - size * Math.cos(angle + arrowAngle);
        double y1 = y - size * Math.sin(angle + arrowAngle);
        double x2 = x - size * Math.cos(angle - arrowAngle);
        double y2 = y - size * Math.sin(angle - arrowAngle);
        
        // Draw the arrowhead
        gc.setFill(Color.BLACK);
        gc.fillPolygon(new double[]{x, x1, x2}, new double[]{y, y1, y2}, 3);
    }
    
    public double getX() {
        return position.getX();
    }
    
    public double getY() {
        return position.getY();
    }
    
    public State getState() {
        return state;
    }

    public static int getRadius() {
        return CIRCLE_SIZE / 2;
    }

    public SerializablePoint2D getPosition() {
        return position;
    }

    public boolean isMouseOverState(SerializablePoint2D mousePoint) {
        SerializablePoint2D position = getPosition();
        
        double distance = mousePoint.distance(position);
        
        if (distance <= getRadius()) {
            return true;
        }
        return false;
    } 

    public int getTransitionCount(State sourceState, State targetState) {
        Map<State, State> transitionKey = new HashMap<>();
        transitionKey.put(sourceState, targetState);
        
        if (transitionCount.containsKey(transitionKey)) {
            return transitionCount.get(transitionKey);
        }
        return 0;
    }

    public int getSelfTransitionCount() {
        int selfTransitionCount = 0;

        for (Transition transition : state.getTransitions()) {
            if (transition.getSourceState() == transition.getTargetState()) {
                selfTransitionCount++;
            }
        }
        return selfTransitionCount;
    }

    public void incrementDrawnTransitionCount(StateUI sourceStateUI, StateUI targetStateUI) {
        Map<State, State> transitionKey = new HashMap<>();
        transitionKey.put(sourceStateUI.getState(), targetStateUI.getState());
        
        if (drawnTransitionCount.containsKey(transitionKey)) {
            drawnTransitionCount.put(transitionKey, drawnTransitionCount.get(transitionKey) + 1);
        } else {
            drawnTransitionCount.put(transitionKey, 1);
        }
    }

    public int getDrawnTransitionCount(StateUI sourceStateUI, StateUI targetStateUI) {
        Map<State, State> transitionKey = new HashMap<>();
        transitionKey.put(sourceStateUI.getState(), targetStateUI.getState());
        
        if (drawnTransitionCount.containsKey(transitionKey)) {
            return drawnTransitionCount.get(transitionKey);
        }
        return 0;
    }
}
