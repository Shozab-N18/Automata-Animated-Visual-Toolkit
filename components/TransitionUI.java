package components;

import java.util.ArrayList;
import java.util.List;


import components.serialization.SerializablePoint2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import java.awt.geom.Line2D;

import model.*;

public class TransitionUI {
    private static final int ARROW_SIZE = 15;
    private static final int TEXT_GAP = 1;
    private static int CIRCLE_RADIUS;
    
    private Transition transition;
    private StateUI sourceStateUI;
    private StateUI targetStateUI;
    
    private boolean isSelfTransition;
    private List<SerializablePoint2D> transitionPoints;
    private SerializablePoint2D dragPoint;
    
    private double startX;
    private double startY;
    private double endX;
    private double endY;

    private double controlX1;
    private double controlY1;
    private double controlX2;
    private double controlY2;
    
    public TransitionUI(Transition transition, StateUI sourceStateUI, StateUI targetStateUI) {
        this.transition = transition;
        this.sourceStateUI = sourceStateUI;
        this.targetStateUI = targetStateUI;
        
        CIRCLE_RADIUS = StateUI.getRadius();
        transitionPoints = new ArrayList<>();
        isSelfTransition = sourceStateUI.getState().equals(targetStateUI.getState());
        
        startX = sourceStateUI.getX();
        startY = sourceStateUI.getY() - CIRCLE_RADIUS;
        
        endX = startX;
        endY = startY - ARROW_SIZE * 2;
        
        // Set control points for bezier curve function
        controlX1 = startX + CIRCLE_RADIUS;
        controlY1 = startY - CIRCLE_RADIUS * 1.5;
        controlX2 = startX - CIRCLE_RADIUS;
        controlY2 = startY - CIRCLE_RADIUS * 1.5;
        
        dragPoint = null;
    }
    
    public void draw(GraphicsContext gc) {
        gc.setStroke(Color.BLACK);
        
        sourceStateUI.incrementDrawnTransitionCount(sourceStateUI, targetStateUI);
        
        if (isSelfTransition) {
            drawSelfTransition(gc);
        } 
        else {
            drawNormalTransition(gc);
        }
    }
    
    private void drawSelfTransition(GraphicsContext gc) {
        if (dragPoint != null) {
            setSelfTransitionPosition(dragPoint);
        } else {
            int drawnCount = sourceStateUI.getDrawnTransitionCount(sourceStateUI, sourceStateUI);
            
            switch (drawnCount) {
                case 2:
                    setSelfTransitionPosition(new SerializablePoint2D(startX, startY + CIRCLE_RADIUS * 4));
                    break;
                case 1:
                    setSelfTransitionPosition(new SerializablePoint2D(startX, startY - CIRCLE_RADIUS * 2));
                    break;
                default:
                    setSelfTransitionPosition(new SerializablePoint2D(startX + CIRCLE_RADIUS * 3, sourceStateUI.getY()));
                    break;
            }
        }
        
        transitionPoints.add(new SerializablePoint2D(startX, startY));
        transitionPoints.add(new SerializablePoint2D(endX, endY));
        
        gc.setLineWidth(1.5); // transition line width
        
        gc.beginPath();
        gc.moveTo(startX, startY);
        gc.bezierCurveTo(controlX1, controlY1, controlX2, controlY2, startX, startY);
        gc.stroke();
        
        gc.setLineWidth(1); // Reset line width
        
        // Calculate the direction of the curve at the end point
        double dx = startX - controlX2;
        double dy = startY - controlY2;
        double angle = Math.atan2(dy, dx);
        
        // Calculate the position of the arrow head
        double arrowX = startX - ARROW_SIZE * Math.cos(angle);
        double arrowY = startY - ARROW_SIZE * Math.sin(angle);
        
        double symbolStartX = controlX1;
        double symbolStartY = controlY1 - CIRCLE_RADIUS * 0.01;
        double symbolEndX = controlX2;
        double symbolEndY = controlY2 - CIRCLE_RADIUS * 0.01;
        
        drawTransitionText(gc, symbolStartX, symbolStartY, symbolEndX, symbolEndY, transition.getTransitionSymbol());
        drawArrowHead(gc, arrowX, arrowY, startX, startY);
    }
    
    private void drawNormalTransition(GraphicsContext gc) {
        double dx = targetStateUI.getX() - sourceStateUI.getX();
        double dy = targetStateUI.getY() - sourceStateUI.getY();
        
        double magnitude = Math.sqrt(dx * dx + dy * dy); // root(x^2 + y^2) = magnitude of the line
        
        // Normalize the change in x and y to get the unit vector
        double nx = dx / magnitude;
        double ny = dy / magnitude;
        
        boolean hasSingleTransitionBetween = hasSingleTransitionBetweenStates(sourceStateUI.getState(), targetStateUI.getState());
        boolean hasSingleTransitionFromStateToTarget = sourceStateUI.getTransitionCount(sourceStateUI.getState(), targetStateUI.getState()) <= 1;
        
        // Calculate the intersection points with the circle
        double startX = sourceStateUI.getX() + nx * CIRCLE_RADIUS;
        double startY = sourceStateUI.getY() + ny * CIRCLE_RADIUS;
        double endX = targetStateUI.getX() - nx * CIRCLE_RADIUS;
        double endY = targetStateUI.getY() - ny * CIRCLE_RADIUS;
        
        if (!hasSingleTransitionBetween && hasSingleTransitionFromStateToTarget) {
            double distance = CIRCLE_RADIUS * 0.3;
            // Moves transition to the side of the circle
            startX += ny * distance;
            startY -= nx * distance;
            endX   += ny * distance;
            endY   -= nx * distance;
        } 
        else if (!hasSingleTransitionBetween && !hasSingleTransitionFromStateToTarget) {
            int drawnCount = sourceStateUI.getDrawnTransitionCount(sourceStateUI, targetStateUI);
            double distance = CIRCLE_RADIUS * 0.3;
            
            startX += ny * distance;
            startY -= nx * distance;
            endX   += ny * distance;
            endY   -= nx * distance;
            
            distance = determineDistanceForCurrentTransition(drawnCount, 0.3);
            startX += ny * distance;
            startY -= nx * distance;
            endX   += ny * distance;
            endY   -= nx * distance;
        }
        else if (!hasSingleTransitionFromStateToTarget) {
            int drawnCount = sourceStateUI.getDrawnTransitionCount(sourceStateUI, targetStateUI);
            double distance = determineDistanceForCurrentTransition(drawnCount, 0.6);
            
            startX += ny * distance;
            startY -= nx * distance;
            endX   += ny * distance;
            endY   -= nx * distance;
        }
        
        gc.setLineWidth(1.5);
        gc.strokeLine(startX, startY, endX, endY);
        gc.setLineWidth(1);
        
        transitionPoints.add(new SerializablePoint2D(startX, startY));
        transitionPoints.add(new SerializablePoint2D(endX, endY));
        
        drawTransitionText(gc, startX, startY, endX, endY, transition.getTransitionSymbol());
        drawArrowHead(gc, startX, startY, endX, endY);
    }    

    /**
     * Determines the distance gap between transitions for the current transition when there are multiple transitions between two states
     * @param drawnCount
     * @param availableSpaceFactor
     * @return
     */
    private double determineDistanceForCurrentTransition(int drawnCount, double availableSpaceFactor) {
        int totalTransitions = sourceStateUI.getTransitionCount(sourceStateUI.getState(), targetStateUI.getState());
        
        double maxDistance = CIRCLE_RADIUS * availableSpaceFactor;
        double spaceBetweenTransitions = maxDistance / (totalTransitions - 1);
        double distance = spaceBetweenTransitions * drawnCount - maxDistance;
        
        if (totalTransitions == 2) {
            distance = CIRCLE_RADIUS * availableSpaceFactor/2;
        }
        
        if(drawnCount % 2 == 0) {
            distance *= -1;
        }
        
        return distance;
    }
    
    private boolean hasSingleTransitionBetweenStates(State sourceState, State targetState) {
        // check if there is a transition from the target state to the source state
        for (Transition sourceStateTransition : sourceState.getTransitions()) {
            for (Transition targetStateTransition : targetState.getTransitions()) {
                if (targetStateTransition.getTargetState().getName().equals(sourceState.getName())) {
                    return false;
                } 
            }
        }
        
        return true;
    }
    
    /**
     * Checks if the mouse is over the transition
     * @param mousePoint
     * @return
     */
    public boolean isMouseOverTransition(SerializablePoint2D mousePoint) {
        int distanceThreshold = 5;
        
        for (int i = 0; i < transitionPoints.size() - 1; i++) {
            SerializablePoint2D p1 = transitionPoints.get(i);
            SerializablePoint2D p2 = transitionPoints.get(i + 1);
            
            double distance = Line2D.ptSegDist(p1.getX(), p1.getY(), p2.getX(), p2.getY(), mousePoint.getX(), mousePoint.getY());
            
            boolean isMouseOutsideState = !sourceStateUI.isMouseOverState(mousePoint) && !targetStateUI.isMouseOverState(mousePoint);
            boolean isOnTransitionLine = distance <= distanceThreshold;
            
            if (isMouseOutsideState && isOnTransitionLine) {
                return true;
            }
        }
        
        SerializablePoint2D symbolPoint = transitionPoints.get(transitionPoints.size() - 1);
        double symbolDistance = mousePoint.distance(symbolPoint);
        if (symbolDistance <= distanceThreshold) {
            return true;
        }
        
        return false;
    }
    
    private void drawTransitionText(GraphicsContext gc, double startX, double startY, double endX, double endY, String text) {
        double symbolX = (startX + endX) / 2;
        double symbolY = (startY + endY) / 2;
        
        transitionPoints.add(new SerializablePoint2D(symbolX, symbolY));
        
        Font font = Font.font(gc.getFont().getFamily(), FontWeight.BOLD, 12);
        gc.setFont(font);
        
        Text textNode = new Text(text);
        textNode.setFont(gc.getFont());
        
        double textWidth = textNode.getLayoutBounds().getWidth();
        double textHeight = textNode.getLayoutBounds().getHeight();
        
        double rectangleWidth = textWidth + TEXT_GAP;
        double rectangleHeight = textHeight + TEXT_GAP;
        
        gc.setFill(Color.WHITE);
        gc.fillRect(symbolX - rectangleWidth / 2, symbolY - rectangleHeight / 2, rectangleWidth, rectangleHeight);
        
        gc.setFill(Color.BLACK);
        gc.fillText(text, symbolX - textWidth / 2, symbolY + textHeight / 4);
    }
    
    private void drawArrowHead(GraphicsContext gc, double startX, double startY, double endX, double endY) {
        double dx = endX - startX;
        double dy = endY - startY;
        
        double magnitude = Math.sqrt(dx * dx + dy * dy);
        
        double nx = dx / magnitude;
        double ny = dy / magnitude;
        
        double arrowAngle = Math.toRadians(20);
        double x1 = endX - ARROW_SIZE * (nx * Math.cos(arrowAngle) - ny * Math.sin(arrowAngle));
        double y1 = endY - ARROW_SIZE * (ny * Math.cos(arrowAngle) + nx * Math.sin(arrowAngle));
        double x2 = endX - ARROW_SIZE * (nx * Math.cos(-arrowAngle) - ny * Math.sin(-arrowAngle));
        double y2 = endY - ARROW_SIZE * (ny * Math.cos(-arrowAngle) + nx * Math.sin(-arrowAngle));
        
        gc.setFill(Color.BLACK);
        gc.fillPolygon(new double[]{endX, x1, x2}, new double[]{endY, y1, y2}, 3);
    }
    
    public Transition getTransition() {
        return transition;
    }
    
    public boolean isSelfTransition() {
        return isSelfTransition;
    }
    
    /**
     * Sets the drag point for the self transition and updates the transition to the new position.
     * @param dragPoint
     */
    public void setSelfTransitionPosition(SerializablePoint2D dragPoint) {
        this.dragPoint = dragPoint;
        
        double dx = dragPoint.getX() - sourceStateUI.getX();
        double dy = dragPoint.getY() - sourceStateUI.getY();
        
        if (Math.abs(dx) >= StateUI.getRadius() && Math.abs(dy) >= StateUI.getRadius()) {
            double angle = Math.atan2(dy, dx);
            double x = sourceStateUI.getX() + StateUI.getRadius() * Math.cos(angle);
            double y = sourceStateUI.getY() + StateUI.getRadius() * Math.sin(angle);
            dx = x - sourceStateUI.getX();
            dy = y - sourceStateUI.getY();
        }
        else if (Math.abs(dx) >= StateUI.getRadius()) {
            dx = dx > 0 ? StateUI.getRadius() : -StateUI.getRadius();
        }
        if (Math.abs(dy) >= StateUI.getRadius()) {
            dy = dy > 0 ? StateUI.getRadius() : -StateUI.getRadius();
        }
        
        // Update the transition start points
        startX = sourceStateUI.getX() + dx;
        startY = sourceStateUI.getY() + dy;
        
        double angle = Math.atan2(dy, dx);
        double controlDistance = CIRCLE_RADIUS * 1.5;
        
        // Update the positions of the control points for the bezier curve
        controlX1 = dragPoint.getX() + controlDistance * Math.cos(angle + Math.PI / 2);
        controlY1 = dragPoint.getY() + controlDistance * Math.sin(angle + Math.PI / 2);
        controlX2 = dragPoint.getX() + controlDistance * Math.cos(angle - Math.PI / 2);
        controlY2 = dragPoint.getY() + controlDistance * Math.sin(angle - Math.PI / 2);
    }

    public boolean equals(TransitionUI transitionUI) {
        return transition.equals(transitionUI.getTransition()) && isSelfTransition == transitionUI.isSelfTransition();
    }

    public SerializablePoint2D getDragPoint() {
        return dragPoint;
    }
}
