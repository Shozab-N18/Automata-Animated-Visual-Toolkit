package app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import view.AutomataConstructorView;
import view.AutomataSimulatorView;
import view.SubsetConstructionView;

import components.MainCanvas;
import components.serialization.AutomataIO;
import components.serialization.SerializablePoint2D;
import model.*;

/**
 * Main class for the application.
 */
public class App extends Application {
    private static Scene scene;
    private static TabPane tabPane;
    private static Map<State, SerializablePoint2D> states = new HashMap<>();
    private static Automata automata = new Automata(states);
    private static AutomataConstructorView automataConstructorView = new AutomataConstructorView(automata);
    private static AutomataSimulatorView automataSimulatorView = new AutomataSimulatorView(automata);
    private static SubsetConstructionView subsetConstructionView = new SubsetConstructionView(states);

    private Stage primaryStage;
    private MenuItem openItem;
    private MenuItem saveItem;
    private MenuItem exitItem;

    private BorderPane root;
    private MenuBar menuBar;
    private Menu fileMenu;
    private Menu overviewMenu;

    private Tab automataTab;
    private Tab simulationTab;
    private Tab subsetTab;
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        initialiseComponents(primaryStage);
        layoutComponents();
        initialiseEventHandlers();
        
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.setTitle("Automata Animated Visual Toolkit");
        primaryStage.show();
    }
    
    private void initialiseComponents(Stage primaryStage) {
        this.primaryStage = primaryStage;
        root = new BorderPane();
        root.getStylesheets().add(getClass().getResource("/css/general.css").toExternalForm());
        
        menuBar = new MenuBar();
        fileMenu = new Menu("File");
        overviewMenu = new Menu("Overview");
        
        openItem = new MenuItem("Open");
        saveItem = new MenuItem("Save");
        exitItem = new MenuItem("Exit");
        
        tabPane = new TabPane();
        
        automataTab = new Tab("Automata", automataConstructorView);
        simulationTab = new Tab("Simulation", automataSimulatorView);
        subsetTab = new Tab("Subset Construction Algorithm", subsetConstructionView);
        
        scene = new Scene(root, 1200, 900);
    }
    
    private void layoutComponents() {
        fileMenu.getItems().addAll(openItem, saveItem, new SeparatorMenuItem(), exitItem);
        menuBar.getMenus().addAll(fileMenu, overviewMenu);
        
        root.setTop(menuBar);
        
        automataTab.setClosable(false);
        simulationTab.setClosable(false);
        subsetTab.setClosable(false);
        
        tabPane.getTabs().addAll(automataTab, simulationTab, subsetTab);
        
        root.setCenter(tabPane);
    }
    
    private void initialiseEventHandlers() {
        openItem.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Automata File");
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                try {
                    primaryStage.setTitle("Automata Animated Visual Toolkit - " + file.getName());
                    states = AutomataIO.loadAutomata(file);
                    automata.setStates(states);
                    setAppStates(automata);
                    
                    MainCanvas.drawAllCanvases();
                } catch (IOException | ClassNotFoundException err) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Error Loading Automata");
                    alert.setContentText("An error occurred while loading the automata. Please make sure the file is valid.");
                    alert.showAndWait();
                }
            }
        });
        
        saveItem.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Automata File");
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                try {
                    AutomataIO.saveAutomata(states, file);
                } catch (IOException err) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Error Saving Automata");
                    alert.setContentText("An error occurred while saving the automata. Please try again.");
                    alert.showAndWait();
                }
            }
        });
        
        exitItem.setOnAction(e -> {
            System.exit(0);
        });
        
        scene.setOnKeyPressed(event -> {
            if (event.isControlDown()) { // Ctrl key held
                if (event.getCode() == KeyCode.DIGIT1) { // 1 key
                    tabPane.getSelectionModel().select(0);
                }
                else if (event.getCode() == KeyCode.DIGIT2) { // 2 key
                    tabPane.getSelectionModel().select(1); 
                }
                else if (event.getCode() == KeyCode.DIGIT3) { // 3 key
                    tabPane.getSelectionModel().select(2);
                }
                
                if (event.getCode() == KeyCode.W) {
                    System.exit(0);
                }
            }
        });
    }
    
    /**
     * Sets the states of the application to the new states. 
     * 
     * @param newAutomata The new automata to set the states to.
     */
    public static void setAppStates(Automata newAutomata) {
        states = newAutomata.getStates();
        automata = newAutomata;
        automataConstructorView.updateAutomata(automata);
        automataSimulatorView.updateAutomata(automata);
        MainCanvas.drawAllCanvases();
    }
    
    public static Scene getScene() {
        return scene;
    }
}