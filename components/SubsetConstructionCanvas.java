package components;

import components.serialization.SerializablePoint2D;
import controller.AutomataController;
import controller.SubsetConstructionController;

import java.util.Optional;
import javafx.scene.control.TextInputDialog;
import javafx.scene.Cursor;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import model.Automata;
import model.State;
import view.SubsetConstructionView;

/**
 * SubsetConstructionCanvas: This class is responsible for the canvases for the subset construction view.
 */
public class SubsetConstructionCanvas<ControllerT extends AutomataController> extends MainCanvas<SubsetConstructionController> {
    private boolean isChoosingStateTransition = false;
    private State startState = null;

    public SubsetConstructionCanvas(SubsetConstructionController controller) {
        super(controller);
        this.getStylesheets().add(getClass().getResource("/css/AutomataCanvas.css").toExternalForm());
        canvas.setId("subsetConstructionCanvas");
        
        contextMenu = new ContextMenu();
    }

    @Override
    protected Color getStateInnerColor(State state) {
        if (isChoosingStateTransition && !state.isDiscarded()) {
            return Color.LIGHTGREEN;
        } else if (controller.isAttemptedStateExpected(state) && !state.isDiscarded()) {
            return Color.LIGHTGRAY;
        } else if (state.isDiscarded()) {
            return Color.WHITESMOKE;
        } else {
            return Color.LIGHTGRAY;
        }
    }

    @Override
    protected Color getStateOutlineColor(State state) {
        if (state.isDiscarded()) {
            return Color.rgb(0, 0, 0, 0.5);
        } else {
            return Color.BLACK.darker();
        }
    }
    
    @Override
    protected Color getStateTextColor(State state) {
        if (state.isDiscarded()) {
            return Color.rgb(0, 0, 0, 0.5);
        } else {
            return Color.BLACK;
        }
    }

    @Override
    protected void showStateOptions(SerializablePoint2D pressPoint, State state) {
        if (!controller.getAutomata().isDFA()) {
            return;
        }
        MenuItem makeAcceptingItem = new MenuItem(state.isAcceptingState() ? "Remove Accepting State" : "Make Accepting State");
        MenuItem makeInitialItem = new MenuItem(state.isStartingState() ? "Remove Starting State" : "Make Starting State");
        MenuItem addTransitionItem = new MenuItem("Add Transition");
        MenuItem discardStateItem = new MenuItem(state.isDiscarded() ? "Restore State" : "Discard State");
        
        makeAcceptingItem.setOnAction(e -> {
            state.toggleAcceptingState();
            controller.checkShouldBeAcceptingState(state);
            controller.checkIsConversionCorrect();
            drawAllCanvases();
            Automata automata = controller.getAutomata();
            SubsetConstructionView.updateCorrespondingAutomata(automata);
        });
        
        makeInitialItem.setOnAction(e -> {
            state.toggleStartingState();
            controller.checkShouldBeStartingState(state);
            controller.checkIsConversionCorrect();
            drawAllCanvases();
            Automata automata = controller.getAutomata();
            SubsetConstructionView.updateCorrespondingAutomata(automata);
        });
        
        addTransitionItem.setOnAction(e -> {
            isChoosingStateTransition = true;
            startState = state;
            controller.showPopupMessage("Choose the target state for the transition.");
            drawCanvas();
        });

        discardStateItem.setOnAction(e -> {
            controller.toggleDiscardedState(state);
            controller.checkShouldStateBeDiscarded(state);
            controller.checkIsConversionCorrect();
            MainCanvas.drawAllCanvases();

            Automata automata = controller.getAutomata();
            SubsetConstructionView.updateCorrespondingAutomata(automata);
        });
        
        showContextMenu(pressPoint, makeAcceptingItem, makeInitialItem, addTransitionItem, discardStateItem);
    }

    private void showTransitionOptions(SerializablePoint2D point) {
        MenuItem removeTransitionItem = new MenuItem("Delete Transition");
        MenuItem editTransitionSymbolItem = new MenuItem("Edit Transition Symbol");

        removeTransitionItem.setOnAction(e -> {
            controller.removeTransition(lastSelectedTransition);
            controller.checkIsConversionCorrect();
            MainCanvas.drawAllCanvases();
        });

        editTransitionSymbolItem.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Input Dialog");
            dialog.setHeaderText(null);
            dialog.setContentText("Enter new transition symbol:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(name -> {
                controller.updateTransitionSymbol(lastSelectedTransition, result.get());
                MainCanvas.drawAllCanvases();
                controller.checkIsNewTransitionCorrect(lastSelectedTransition.getSourceState(), lastSelectedTransition.getTargetState(), lastSelectedTransition.getTransitionSymbol());
            });
        });

        showContextMenu(point, removeTransitionItem, editTransitionSymbolItem);
    }

    protected void setCanvasEvents() {
        super.setCanvasEvents();
        
        canvas.setOnContextMenuRequested(e -> {
            SerializablePoint2D point = new SerializablePoint2D(e.getX(), e.getY());    
            
            isMouseOverState = isMouseOverState(point);
            isMouseOverTransition = isMouseOverTransition(point);
            
            if (isMouseOverTransition) {
                showTransitionOptions(point);
            }
        });
        
        canvas.setOnMouseDragged(e -> {
            if (isMouseOverState) {
                handleStateDrag(new SerializablePoint2D(e.getX(), e.getY()));
                canvas.setCursor(Cursor.CLOSED_HAND);
            } else if (isMouseOverTransition && lastSelectedTransition != null && getTransitionUIByTransition(lastSelectedTransition).isSelfTransition()) {
                handleSelfTransitionDrag(new SerializablePoint2D(e.getX(), e.getY()));
                canvas.setCursor(Cursor.MOVE);
                MainCanvas.drawAllCanvases();
            }
            else {
                canvas.setCursor(Cursor.DEFAULT);
            }
        });
        
        canvas.setOnMouseMoved(e -> {
            SerializablePoint2D point = new SerializablePoint2D(e.getX(), e.getY());
            if (isMouseOverCanvas(point)) {
                updateCoordinatesLabel(point);
                
                isMouseOverState = isMouseOverState(point);
                isMouseOverTransition = isMouseOverTransition(point);
                
                if (isMouseOverState) {
                    canvas.setCursor(Cursor.HAND);
                } else if (isMouseOverTransition && lastSelectedTransition != null && getTransitionUIByTransition(lastSelectedTransition).isSelfTransition()) {
                    canvas.setCursor(Cursor.MOVE);
                } else if (isMouseOverTransition) {
                    canvas.setCursor(Cursor.HAND);
                }
                else {
                    canvas.setCursor(Cursor.DEFAULT);
                }
                
                if (contextMenu != null && contextMenu.isShowing() && !isMouseOverContextMenu(e)) {
                    contextMenu.hide();
                }
            } 
            else {
                coordinatesLabel.setVisible(false);
            }
        });
        

        canvas.setOnMouseClicked(e -> {
            SerializablePoint2D point = new SerializablePoint2D(e.getX(), e.getY());
            StateUI state = chooseStateUI(point);
            
            if (state != null && isChoosingStateTransition) {
                TextInputDialog symbolDialog = new TextInputDialog();
                symbolDialog.setTitle("Add Transition");
                symbolDialog.setHeaderText(null);
                symbolDialog.setContentText("Enter transition symbol.");
                
                Optional<String> transitionSymbolResult = symbolDialog.showAndWait();
                
                if (transitionSymbolResult.isPresent()) {
                    String transitionSymbol = transitionSymbolResult.get();
                    
                    State targetState = state.getState();
                    
                    if (targetState != null && !transitionSymbol.trim().isEmpty()) {
                        controller.addTransition(startState, targetState, transitionSymbol);
                        controller.checkIsNewTransitionCorrect(startState, targetState, transitionSymbol);
                        controller.checkIsConversionCorrect();
                        
                        isChoosingStateTransition = false;
                        startState = null;
                        MainCanvas.drawAllCanvases();
                    }
                } 
            }
        });
    }
}
