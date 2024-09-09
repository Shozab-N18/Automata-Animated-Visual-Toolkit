package controller;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.HashMap;
import java.util.HashSet;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.util.Duration;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;

import components.SpecialSymbols;
import components.MainCanvas;
import model.*;
import view.AutomataSimulatorView;

/*
 * SimulatorController class is responsible for controlling the simulation of an automata.
 */
public class SimulatorController extends AutomataController {
    private boolean isInputAccepted;
    private Double delay = 0.5;
    
    private TableView<ObservableList<StringProperty>> simulationLog;
    private Map<String, String> loggedInputWords = new HashMap<>();
    
    protected InputTape inputTape;
    
    private Runnable onSimulationFinished;
    
    private Timeline timeline;
    private KeyFrame keyFrame;
    
    private boolean isPaused = false;
    private boolean isStopped = true;
    private boolean wasPlayingBeforeChoosingNextState = false;
    
    // keeps track of the previous states visited during simulation
    private Stack<State> previousStates; 
    // keeps track of the possible next states to transition to
    private Set<State> possibleNextStates; 
    // the current state the simulation is on
    private State selectedNextState;
    
    // Constructor
    public SimulatorController(Automata automata) {
        super(automata);
        simulationLog = new TableView<>();
        loggedInputWords = new HashMap<>();
        previousStates = new Stack<>();
        
        TableColumn<ObservableList<StringProperty>, String> inputColumn = new TableColumn<>("Input Word");
        TableColumn<ObservableList<StringProperty>, String> resultColumn = new TableColumn<>("Result");
        TableColumn<ObservableList<StringProperty>, String> pathColumn = new TableColumn<>("Path");
        
        inputColumn.setCellValueFactory(cellData -> cellData.getValue().get(0));
        resultColumn.setCellValueFactory(cellData -> cellData.getValue().get(1));
        pathColumn.setCellValueFactory(cellData -> cellData.getValue().get(2));
        
        inputColumn.prefWidthProperty().bind(simulationLog.widthProperty().multiply(0.3));
        resultColumn.prefWidthProperty().bind(simulationLog.widthProperty().multiply(0.2));
        pathColumn.prefWidthProperty().bind(simulationLog.widthProperty().multiply(0.5)); 
        
        simulationLog.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                ObservableList<StringProperty> selectedItem = simulationLog.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    Alert alert = new Alert(AlertType.INFORMATION);
                    ButtonType replayButtonType = new ButtonType("Replay", ButtonData.OK_DONE);
                    alert.getButtonTypes().addAll(replayButtonType);
                    
                    Button replayButton = (Button) alert.getDialogPane().lookupButton(replayButtonType);
                    replayButton.setOnAction(event -> {
                        stop();
                        String input = selectedItem.get(0).getValue();
                        if (input == "None") {
                            input = "";
                        }
                        simulate(input);
                        alert.close();
                    });
                    
                    alert.setTitle("Information");
                    alert.setHeaderText("Simulation Data");
                    alert.setContentText("Input Word: " + selectedItem.get(0).getValue() + "\nResult: " + selectedItem.get(1).getValue() + "\nPath: " + selectedItem.get(2).getValue());
                    alert.showAndWait();
                }
            }
        });
        
        
        
        
        simulationLog.getColumns().addAll(inputColumn, resultColumn, pathColumn);
        
        simulationLog.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        simulationLog.setMinHeight(200);
        simulationLog.setMinWidth(200);
        simulationLog.setMaxHeight(600);
        simulationLog.setMaxWidth(1200);
        simulationLog.setPrefHeight(250);
        simulationLog.setPrefWidth(450);
        inputTape = new InputTape(" ");
    }

    @Override
    public State getStartingState() {
        State state = super.getStartingState();
        
        if (state == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("No Starting State");
            alert.setHeaderText("No starting state found.");
            alert.setContentText("Please add a starting state to the automata before simulating.");
            alert.showAndWait();
            return null;
        } else {
            return state;
        }
    }
    
    /**
     * Find an accepting path for the input string provided.
     * @param inputString
     */
    public void findAcceptingPath(String inputString) {
        Set<String> visitedStates = new HashSet<>();
        Stack<State> path = new Stack<>();
        Stack<String> transitionSymbols = new Stack<>();
        
        State startingState = getStartingState();
        if (startingState != null) {
            searchForAcceptingPath(startingState, transitionSymbols, visitedStates, path, inputString);
        }
    }
    
    /**
     * Recursively search for an accepting path in the automaton for the given input string.
     * @param currentState
     * @param transitionSymbols
     * @param visitedStates
     * @param path
     * @param inputString
     */
    private void searchForAcceptingPath(State currentState, Stack<String> transitionSymbols, Set<String> visitedStates, Stack<State> path, String inputString) {
        // A key representing the current state and transition symbols used to reach it
        String currentStateKey = currentState.getName() + ":" + getTransitionsStringIncludingEpsilon(transitionSymbols);
        
        // Backtrack if the current state has already been visited to avoid infinite loops
        if (visitedStates.contains(currentStateKey)) {
            return;
        }
        
        visitedStates.add(currentStateKey);
        
        // Add the current state to the path
        path.push(currentState);
        
        // Log path in table if the input string has been consumed and if the current state is an accepting state
        if (getMergedTransitionsSymbolsExcludingEpsilon(transitionSymbols).equals(inputString) && currentState.isAcceptingState()) {
            String pathString = generateStringPath(path, inputString);
            boolean isSimulationAlreadyLogged = loggedInputWords.containsKey(inputTape.getInput()) && loggedInputWords.get(inputTape.getInput()).equals(pathString);
            
            Stack<State> tempStack = new Stack<>();
            tempStack.addAll(path);
            
            if (!isSimulationAlreadyLogged && path != null && !path.isEmpty()) {
                logSimulationResult(inputString, true, pathString);
            }
            path.pop(); // Remove the current state from the path before backtracking
            return;
        }
        
        // Handle epsilon transitions 
        Set<State> epsilonStates = currentState.getNextStatesFromSymbol(SpecialSymbols.EPSILON.toString());
        for (State epsilonState : epsilonStates) {
            Stack<String> nextTransitionSymbols = new Stack<>();
            nextTransitionSymbols.addAll(transitionSymbols);
            nextTransitionSymbols.push(SpecialSymbols.EPSILON.toString());
            searchForAcceptingPath(epsilonState, nextTransitionSymbols, visitedStates, path, inputString);
        }
        
        int index = getMergedTransitionsSymbolsExcludingEpsilon(transitionSymbols).length();
        if (index < inputString.length()) { // Check if there are more symbols to consume
            char nextSymbol = inputString.charAt(index);
            // Handle non-epsilon transitions
            Set<State> nextStates = currentState.getNonEpsilonNextStatesFromSymbol(String.valueOf(nextSymbol));
            for (State nextState : nextStates) {
                Stack<String> nextTransitionSymbols = new Stack<>();
                nextTransitionSymbols.addAll(transitionSymbols);
                nextTransitionSymbols.push(String.valueOf(nextSymbol));
                searchForAcceptingPath(nextState, nextTransitionSymbols, visitedStates, path, inputString);
            }
        }
        
        path.pop();
    }
    
    
    private String getMergedTransitionsSymbolsExcludingEpsilon(Stack<String> transitionSymbols) {
        StringBuilder transitionsString = new StringBuilder();
        for (String symbol : transitionSymbols) {
            if (!symbol.equals(SpecialSymbols.EPSILON.toString())) {
                transitionsString.append(symbol);
            }
        }
        return transitionsString.toString();
    }
    
    private String getTransitionsStringIncludingEpsilon(Stack<String> transitionSymbols) {
        StringBuilder transitionsString = new StringBuilder();
        for (String symbol : transitionSymbols) {
            transitionsString.append(symbol);
        }
        return transitionsString.toString();
    }
    
    private String generateStringPath(Stack<State> path, String inputString) {
        StringBuilder pathString = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            State state = path.get(i);
            pathString.append(state.getName());
            if (i < path.size() - 1) {
                pathString.append(" → ");
            }
        }

        return pathString.toString();
    }
    
    /**
     * Simulate a random path on the automata with a random input string.
     */
    public void simulateRandomPath() {
        clearRejectedState();
        stop();
        selectedState = getStartingState();
        if (selectedState == null) {
            stop();
            return;
        }
        
        MainCanvas.drawSimulatorCanvas();
        
        Random random = new Random();
        int length = random.nextInt(7) + 1;
        
        StringBuilder stringBuilder = new StringBuilder();
        ArrayList<String> alphabet = new ArrayList<>(automata.getAlphabet());
        alphabet.remove(SpecialSymbols.EPSILON.toString());
        
        for (int i = 0; i < length; i++) {
            String randomChar = alphabet.get(random.nextInt(alphabet.size()));
            stringBuilder.append(randomChar);
        }
        
        String randomInput = stringBuilder.toString();
        
        inputTape.setInput(randomInput);
        AutomataSimulatorView.updateTapeView(inputTape);
        
        isStopped = false;
        isPaused = true;
        Timeline timeline = new Timeline();
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(delay), event -> {
            if (!isPaused && !isStopped) {
                simulateStepForward();
            }
        });
        timeline.getKeyFrames().add(keyFrame);
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    /**
     * Simulate the input string on the automata.
     * @param inputString
     */
    public void simulate(String inputString) {
        clearRejectedState();
        selectedState = getStartingState();
        if (selectedState == null) {
            stop();
            return;
        }
        System.out.println("Starting state: " + selectedState);
        
        MainCanvas.drawSimulatorCanvas();
        
        inputTape.setInput(inputString);
        AutomataSimulatorView.updateTapeView(inputTape);
        
        isStopped = false;
        isPaused = true;
        timeline = new Timeline();
        keyFrame = new KeyFrame(Duration.seconds(delay), event -> {
            if (!isPaused && !isStopped) {
                System.out.println("Simulating step forward");
                simulateStepForward();
            }
        });
        timeline.getKeyFrames().add(keyFrame);
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    /*
     * Simulate a single step forward in the automata.
     */
    public void simulateStepForward() {
        // Check if the simulation should be stopped
        if (isEndOfInput() && noPossibleNextStates()) {
            handleEndOfInput();
            setTimedResetView();
            return;
        } else if (inputTape.getHeadPosition() > inputTape.getInput().length()) {
            stop(); 
            return;
        }
        
        // Continue the simulation
        String symbol = inputTape.readSymbol();
        Set<State> states = selectedState.getNextStatesFromSymbol(symbol);
        
        if (states.isEmpty()) {
            endSimulationAsRejected();
            return;
        }
        
        handleNextState(states, 0);
    }
    
    /**
     * Handle the next state to transition to based on the current state and input symbol.
     * @param states
     */
    private void handleNextState(Set<State> states, int index) {
        ArrayList<State> nextStates = new ArrayList<>(states);
        String transitionSymbol = selectedState.getMultipleTransitionsToTarget(nextStates.get(index)).values().iterator().next();
        
        // Handle choice between staying in the same state and transitioning via epsilon symbol at the end of input
        if (transitionSymbol.contains(SpecialSymbols.EPSILON.toString()) && isEndOfInput()) {
            nextStates.add(selectedState);
            states.add(selectedState);
        }
        
        if (nextStates.size() == 1 && transitionSymbol.length() > 1 && transitionSymbol.contains(inputTape.readSymbol())) {
            String chosenSymbol = requestUserToChooseTransitionSymbol(transitionSymbol);
            if (!chosenSymbol.equals(SpecialSymbols.EPSILON.toString()) && !chosenSymbol.equals(inputTape.readSymbol())) {
                // Transition symbol chosen cant be used by the input tape head
                // Terminate simulation
                endSimulationAsRejected();
                return;
            } else {
                transitionSymbol = chosenSymbol;
            }
        }
        
        // Recurse to fetch the transition symbol for the next state in the list 
        // until a next state with a valid transition symbol is found.
        if (transitionSymbol == null || transitionSymbol.isEmpty() || transitionSymbol == "") {
            if (index < nextStates.size() - 1) {
                handleNextState(states, index + 1);
            }
            return;
        } 
        
        if (nextStates.size() == 1 && transitionSymbol.equals(SpecialSymbols.EPSILON.toString())){
            previousStates.push(selectedState);
            
            selectedState = nextStates.get(0);
            MainCanvas.drawSimulatorCanvas();
        } else if (nextStates.size() == 1) {
            previousStates.push(selectedState);
            
            selectedState = nextStates.get(0);
            MainCanvas.drawSimulatorCanvas();
            moveInputHeadRight();
        }  else {
            setPossibleNextStates(states);
            MainCanvas.drawSimulatorCanvas();
            wasPlayingBeforeChoosingNextState = !isPaused;
            pause();
            showPopupMessageToSelectNextState();
        }
    }
    
    /**
     * Move the input tape head to the right and update the tape view.
     */
    public void moveInputHeadRight() {
        inputTape.moveHeadRight();
        AutomataSimulatorView.updateTapeView(inputTape);
    }

    private void showPopupMessageToSelectNextState() {
        showPopupMessage("Multiple states available. Choose the next state to transition to.");
    }
    
    /**
     * Choose the selected next state to transition to.
     * @param state
     */
    public void chooseSelectedNextState(State state) {
        previousStates.push(selectedState);
        
        State previousSelectedState = getPreviousStates().peek();
        String symbol = previousSelectedState.getMultipleTransitionsToTarget(state).values().iterator().next(); 
        
        if (symbol.contains(inputTape.readSymbol())) {
            String chosenSymbol = requestUserToChooseTransitionSymbol(symbol);
            if (chosenSymbol.isEmpty() || chosenSymbol == "") {
                return;
            } 
        
            if (!chosenSymbol.equals(SpecialSymbols.EPSILON.toString()) && !chosenSymbol.equals(inputTape.readSymbol())) {
                // Transition symbol chosen cant be used by the input tape head
                // Terminate simulation
                setPossibleNextStates(null);
                endSimulationAsRejected();
                return;
            } else {
                symbol = chosenSymbol;
            }
        } else {
            symbol = SpecialSymbols.EPSILON.toString();
        }
        
        setSelectedNextState(state);
        
        if (previousSelectedState != null && symbol != null && !symbol.equals(SpecialSymbols.EPSILON.toString())) {
            moveInputHeadRight();
        }
        
        setSelectedState(state);
        setPossibleNextStates(null);
        MainCanvas.drawSimulatorCanvas();
        if (isEndOfInput() || noPossibleNextStates()) {
            addIntermediaryStatesToPath(previousSelectedState, state, new Stack<>());
            handleEndOfInput();
            setTimedResetView();
            return;
        } else if (!isEndOfInput()) {
            addIntermediaryStatesToPath(previousSelectedState, state, new Stack<>());
        }
        
        if (wasPlayingBeforeChoosingNextState) {
            continueSimulation();
        }
    }

    private String requestUserToChooseTransitionSymbol(String symbol) {
        if (symbol.length() > 1 && symbol.contains(SpecialSymbols.EPSILON.toString())) {
            StringBuilder possibleSymbols = new StringBuilder();
            for (int i = 0; i < symbol.length(); i++) {
                possibleSymbols.append(symbol.substring(i, i + 1));
                
                if (i < symbol.length() - 1) {
                    possibleSymbols.append(", ");
                }
            }
            
            TextInputDialog targetStateDialog = new TextInputDialog();
            targetStateDialog.setTitle("Multiple Transitions");
            targetStateDialog.setHeaderText("Choose a transition symbol.");
            targetStateDialog.setContentText("Choice from: [" + possibleSymbols + "]. Enter '-' for epsilon symbol.");
            
            Optional<String> transitionSymbolResult = targetStateDialog.showAndWait();
            
            if (transitionSymbolResult.isPresent()) {
                if (transitionSymbolResult.get().equals("-")) {
                    symbol = SpecialSymbols.EPSILON.toString();
                } else if (symbol.contains(transitionSymbolResult.get())) {
                    symbol = transitionSymbolResult.get();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Invalid Transition Symbol");
                    alert.setHeaderText("Invalid transition symbol.");
                    alert.setContentText("Please enter a valid transition symbol.");
                    alert.showAndWait();
                    return "";
                }
            } else {
                return "";
            }
        }  else if (symbol.length() > 1) {
            symbol = inputTape.readSymbol();
        } else if (symbol.isEmpty()) {
            symbol = SpecialSymbols.EPSILON.toString();
        }
        return symbol;
    }
    
    /**
     * Add intermediary states to the previous states stack to keep track of the path via epsilon transitions.
     * @param previousSelectedState
     * @param newSelectedState
     * @param visitedStates
     */
    private void addIntermediaryStatesToPath(State previousSelectedState, State newSelectedState, Stack<State> visitedStates) {
        String epsilonSymbol = SpecialSymbols.EPSILON.toString();
        
        Set<State> epsilonStates = previousSelectedState.getNextStatesFromSymbol(epsilonSymbol);
        
        for (State epsilonState : epsilonStates) {
            if (epsilonState != null && !visitedStates.contains(epsilonState)) {
                if (epsilonState.equals(newSelectedState)) {
                    // Target state reached, add intermediary states to simulation path
                    previousStates.addAll(visitedStates);
                    return;
                } else {
                    // Continue exploring epsilon transitions
                    visitedStates.push(epsilonState);
                    addIntermediaryStatesToPath(epsilonState, newSelectedState, visitedStates);
                }
            }
        }
    }
    
    private boolean isEndOfInput() {
        return inputTape.getHeadPosition() >= inputTape.getInput().length();
    }
    
    private boolean noPossibleNextStates() {
        return selectedState != null && selectedState.getNextStatesFromSymbol(inputTape.readSymbol()).isEmpty();
    }
    
    /**
     * Determine if the input string is accepted by the automata.
     */
    private void handleEndOfInput() {
        if (selectedState != null && selectedState.isAcceptingState()) {
            isInputAccepted = true;
        } 
        else {
            isInputAccepted = false;
        }
    }
    
    private void setTimedResetView() {
        isStopped = true;
        logSimulationResult();
        // setFinalCanvasView();
        Timeline resetTimeline = new Timeline(new KeyFrame(Duration.seconds(delay), event -> {
            setFinalCanvasView();
            terminate();
        }));
        resetTimeline.play();
    }
    
    private void endSimulationAsRejected() {
        isInputAccepted = false;
        setTimedResetView();
    }
    
    public void setFinalCanvasView() {
        if (isInputAccepted) {
            selectedState = null;
            MainCanvas.drawSimulatorCanvas();
            AutomataSimulatorView.updateTapeView(inputTape);
            inputTape.terminate();
            previousStates.clear();
            AutomataSimulatorView.updateTapeView(inputTape);
        } else {
            rejectedState = selectedState;
            selectedState = null;
            MainCanvas.drawSimulatorCanvas();
            previousStates.clear();
            AutomataSimulatorView.showRejectingTapeView(inputTape);
            inputTape.terminate();
        }
        
        isPaused = false;
        isStopped = true;
    }
    
    public void simulateStepBackward() {
        if (previousStates.isEmpty()) {
            return;
        }
        
        State previousState = previousStates.pop();
        
        if (!previousState.getTransitionSymbolToTargetState(selectedState).equals(SpecialSymbols.EPSILON.toString())) {
            inputTape.moveHeadLeft();
            AutomataSimulatorView.updateTapeView(inputTape);
        }
        possibleNextStates = null;
        selectedState = previousState;
        MainCanvas.drawSimulatorCanvas();
    }
    
    public void play() {
        if (isPaused || isStopped) {
            isPaused = false;
            isStopped = false;
        }
    }
    
    public void pause() {
        isPaused = true;
    }
    
    public void continueSimulation() {
        isPaused = false;
        play();
    }
    
    public void next() {
        if (!isStopped) {
            simulateStepForward();
        }
    }
    
    public void previous() {
        simulateStepBackward();
    }
    
    public void stop() {
        previousStates.clear();
        selectedState = null;
        rejectedState = null;
        possibleNextStates = null;
        terminate();
        inputTape.terminate();
        AutomataSimulatorView.updateTapeView(inputTape);
        MainCanvas.drawSimulatorCanvas();
    }
    
    public void setDelay(Double delay) {
        this.delay = delay;
    }
    
    private void logSimulationResult() {
        String path = generateStringPath();
        boolean isSimulationAlreadyLogged = loggedInputWords.containsKey(inputTape.getInput()) && loggedInputWords.get(inputTape.getInput()).equals(path);
        
        if (!isSimulationAlreadyLogged && path != null && !path.isEmpty()) {
            logSimulationResult(inputTape.getInput(), isInputAccepted, path);
        }
    }
    
    public void logSimulationResult(String inputWord, boolean isAccepted, String path) {
        ObservableList<StringProperty> logEntry = FXCollections.observableArrayList();
        
        if (inputWord == null || inputWord.isEmpty() || inputWord.equals(" ")) {
            inputWord = "None";
        }
        
        logEntry.add(new SimpleStringProperty(inputWord));
        logEntry.add(new SimpleStringProperty(isAccepted ? "Accepted" : "Rejected"));
        logEntry.add(new SimpleStringProperty(path));
        
        simulationLog.getItems().add(logEntry);
        
        loggedInputWords.put(inputWord, path);
    }

    public TableView<ObservableList<StringProperty>> getSimulationLog() {
        return simulationLog;
    }

    public void setOnSimulationFinished(Runnable callback) {
        this.onSimulationFinished = callback;
    }

    public void updateAutomata(Automata automata) {
        this.automata = automata;
        simulationLog.getItems().clear();
        loggedInputWords.clear();
    }

    private void terminate() {
        logSimulationResult();
        isPaused = false;
        isStopped = true;
        if (onSimulationFinished != null) {
            onSimulationFinished.run();
        }
        if (timeline != null) {
            timeline.stop();
        }
        previousStates.clear();
    }

    public Stack<State> getPreviousStates() {
        return previousStates;
    }

    public Set<State> getPossibleNextStates() {
        return possibleNextStates;
    }

    public void setPossibleNextStates(Set<State> possibleNextStates) {
        this.possibleNextStates = possibleNextStates;
    }

    public State getSelectedNextState() {
        return selectedNextState;
    }

    public void setSelectedNextState(State selectedNextState) {
        this.selectedNextState = selectedNextState;
    }

    private String generateStringPath() {
        StringBuilder path = new StringBuilder();
        previousStates.push(selectedState);
        Stack<State> tempStack = reverseStack(previousStates);

        while (!tempStack.isEmpty()) {
            State state = tempStack.pop();
            if (state != null) {
                path.append(state.getName());
            }
            if (!tempStack.isEmpty() && tempStack.peek() != null){
                path.append(" → ");
            }
        }
        return path.toString();
    }

    public void clearSimulationLog() {
        simulationLog.getItems().clear();
        loggedInputWords.clear();
    }

    private Stack<State> reverseStack(Stack<State> stack) {
        Stack<State> reversedStack = new Stack<>();
        while (!stack.isEmpty()) {
            reversedStack.push(stack.pop());
        }
        return reversedStack;
    }    

    public boolean isPaused() {
        return isPaused;
    }

    public boolean isStopped() {
        return isStopped;
    }
}
