package model;

/*
 * InputTape class: The model class that models the input tape for the input word
 */
public class InputTape {
    private String[] tape;
    private int headPosition;
    private String input;
    
    // Constructor
    public InputTape(String input) {
        setInput(input);
    }
    
    public void setInput(String input) {
        tape = (input + " ").split("");
        headPosition = 0;
        this.input = input;
    }
    
    public boolean isInputEmpty() {
        return input == null || input.equals("");
    }
    
    public String readSymbol() {
        return tape[headPosition];
    }

    public void moveHeadLeft() {
        if (headPosition > 0) {
            headPosition--;
        }
    }

    public void moveHeadRight() {
        if (headPosition < tape.length - 1) {
            headPosition++;
        }
    }

    public void terminate() {
        headPosition = tape.length;
    }

    public void writeSymbol(String symbol) {
        tape[headPosition] = symbol;
    }

    public int getHeadPosition() {
        return headPosition;
    }

    public String[] getTape() {
        return tape;
    }

    public String getInput() {
        return input;
    }
}
