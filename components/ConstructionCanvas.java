package components;

import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.util.Optional;

import components.serialization.SerializablePoint2D;
import controller.ConstructionController;
import model.State;

/*
 * This class is responsible for the construction canvas of the application.
 */
public class ConstructionCanvas extends MainCanvas<ConstructionController> {
    private Color stateInnerColor = Color.LIGHTGRAY;
    private boolean isChoosingStateTransition = false;
    private State startState = null;
    private boolean isEpsilonTransition = false;

    public ConstructionCanvas(ConstructionController controller) {
        super(controller);
        
        this.getStylesheets().add(getClass().getResource("/css/AutomataCanvas.css").toExternalForm());
        
        canvas.setId("automataCanvas");
        
        contextMenu = new ContextMenu();
    }
    
    // Control options for the context menu
    
    private void showBaseOptions(SerializablePoint2D point) {
        MenuItem addStateItem = new MenuItem("Add Automaton");
        
        addStateItem.setOnAction(e -> {
            controller.addState(point);
            MainCanvas.drawAllCanvases();
        });
        
        showContextMenu(point, addStateItem);
    }
    
    @Override
    protected void showStateOptions(SerializablePoint2D point, State state) {
        MenuItem deleteItem = new MenuItem("Delete");
        MenuItem renameItem = new MenuItem("Rename");
        MenuItem makeAcceptingItem = new MenuItem(state.isAcceptingState() ? "Remove Accepting State" : "Make Accepting State");
        MenuItem makeInitialItem = new MenuItem(state.isStartingState() ? "Remove Starting State" : "Make Starting State");
        MenuItem addTransitionItem = new MenuItem("Add Transition");
        MenuItem epsilonTransitionItem = new MenuItem("Add ε-Transition");
        
        boolean isNFA = !controller.getAutomata().isDFA();
        epsilonTransitionItem.setDisable(!isNFA);

        if (state.isStartingState()) {
            makeInitialItem.setDisable(false);
        } else {
            makeInitialItem.setDisable(controller.getAutomata().hasStartingState());
        }

        deleteItem.setOnAction(e -> {
            controller.deleteState(state);
            MainCanvas.drawAllCanvases();
        });

        renameItem.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Input Dialog");
            dialog.setHeaderText(null);
            dialog.setContentText("Enter new name:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(name -> {
                controller.updateStateName(state, result.get());
                MainCanvas.drawAllCanvases();
            });
        });

        makeAcceptingItem.setOnAction(e -> {
            controller.toggleAcceptingState(state);
            MainCanvas.drawAllCanvases();
        });

        makeInitialItem.setOnAction(e -> {
            controller.toggleStartingState(state);
            MainCanvas.drawAllCanvases();
        });

        addTransitionItem.setOnAction(e -> {
            isChoosingStateTransition = true;
            startState = state;
            controller.showPopupMessage("Choose the target state for the transition.");
            stateInnerColor = Color.LIGHTGREEN;
            drawCanvas();
        });
        
        epsilonTransitionItem.setOnAction(e -> {
            isChoosingStateTransition = true;
            startState = state;
            controller.showPopupMessage("Choose the target state for the ε-transition.");
            stateInnerColor = Color.LIGHTGREEN;
            isEpsilonTransition = true;
            drawCanvas();
        });
        
        showContextMenu(point, deleteItem, renameItem, makeAcceptingItem, makeInitialItem, addTransitionItem, epsilonTransitionItem);
    }  
    
    private void showTransitionOptions(SerializablePoint2D point) {
        MenuItem removeTransitionItem = new MenuItem("Delete Transition");
        MenuItem editTransitionSymbolItem = new MenuItem("Edit Transition Symbol");

        removeTransitionItem.setOnAction(e -> {
            controller.removeTransition(getTransitionUIByTransition(lastSelectedTransition).getTransition());
            MainCanvas.drawAllCanvases();
        });

        editTransitionSymbolItem.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Input Dialog");
            dialog.setHeaderText(null);
            dialog.setContentText("Enter new transition symbol:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(name -> {
                controller.updateTransitionSymbol(getTransitionUIByTransition(lastSelectedTransition).getTransition(), result.get());
                MainCanvas.drawAllCanvases();
            });
        });

        showContextMenu(point, removeTransitionItem, editTransitionSymbolItem);
    }

    @Override
    protected Color getStateInnerColor(State state) {
        return stateInnerColor;
    }

    private void resetStatesAfterAddingTransition() {
        isChoosingStateTransition = false;
        startState = null;
        stateInnerColor = Color.LIGHTGRAY;
        isEpsilonTransition = false;
        MainCanvas.drawAllCanvases();
    }

    protected void setCanvasEvents() {
        super.setCanvasEvents();

        canvas.setOnContextMenuRequested(e -> {
            SerializablePoint2D point = new SerializablePoint2D(e.getX(), e.getY());    
            
            isMouseOverState = isMouseOverState(point);
            isMouseOverTransition = isMouseOverTransition(point);
            
            if (isMouseOverTransition) {
                showTransitionOptions(point);
            } else if (!isMouseOverState) {
                showBaseOptions(point);
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
                canvas.setCursor(javafx.scene.Cursor.DEFAULT);
            }
        });
        
        canvas.setOnMouseMoved(e -> {
            SerializablePoint2D point = new SerializablePoint2D(e.getX(), e.getY());
            if (isMouseOverCanvas(point)) {
                updateCoordinatesLabel(point);
                
                isMouseOverState = isMouseOverState(point);
                isMouseOverTransition = isMouseOverTransition(point);
                
                if (isMouseOverState) {
                    canvas.setCursor(javafx.scene.Cursor.HAND);
                } else if (isMouseOverTransition && lastSelectedTransition != null && getTransitionUIByTransition(lastSelectedTransition).isSelfTransition()) {
                    canvas.setCursor(Cursor.MOVE);
                } else if (isMouseOverTransition) {
                    canvas.setCursor(javafx.scene.Cursor.HAND);
                }
                else {
                    canvas.setCursor(javafx.scene.Cursor.DEFAULT);
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
            
            if (state != null && isChoosingStateTransition && !isEpsilonTransition) {
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
                        resetStatesAfterAddingTransition();
                    }
                } 
            } else if (state != null && isChoosingStateTransition && isEpsilonTransition) {
                controller.addTransition(startState, state.getState(), SpecialSymbols.EPSILON.toString());
                resetStatesAfterAddingTransition();
            }
            
        });
    }
    
    public Label getCoordinatesLabel() {
        return coordinatesLabel;
    }
}
