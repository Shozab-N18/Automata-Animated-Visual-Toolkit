package components.serialization;

import java.io.*;
import java.util.Map;
import model.State;

/**
 * AutomataIO class which handles the input and output of states data.
 */
public class AutomataIO {
    /**
     * Saves the given states to the specified file.
     * 
     * @param states 
     * @param file 
     * @throws IOException
     */
    public static void saveAutomata(Map<State, SerializablePoint2D> states, File file) throws IOException {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file))) {
            outputStream.writeObject(states);
        }
    }
    /**
     * Loads the states from the specified file.
     * 
     * @param file 
     * @return The states loaded from the file
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Map<State, SerializablePoint2D> loadAutomata(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<State, SerializablePoint2D>) inputStream.readObject();
        }
    }
}
