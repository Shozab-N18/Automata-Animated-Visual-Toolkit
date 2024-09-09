package tests.modelTests;

import static org.junit.Assert.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.InputTape;

public class InputTapeTest {
    private InputTape inputTape;
    private String inputString = "Test Input";
    
    @BeforeEach
    public void setUp() {
        inputTape = new InputTape(inputString);

    }
    
    @Test
    public void testConstructorAndGetters() {
        assertEquals(inputString, inputTape.getInput());
        assertEquals(0, inputTape.getHeadPosition());
        assertArrayEquals(new String[] {"T", "e", "s", "t", " ", "I", "n", "p", "u", "t", " "}, inputTape.getTape());
    }
    
    @Test
    public void testSetInput() {
        String newInput = "New Input";
        inputTape.setInput(newInput);
        assertEquals(newInput, inputTape.getInput());
        assertEquals(0, inputTape.getHeadPosition());
        assertArrayEquals(new String[] {"N", "e", "w", " ", "I", "n", "p", "u", "t", " "}, inputTape.getTape());
    }
    
    @Test
    public void testReadSymbol() {
        assertEquals("T", inputTape.readSymbol());
        inputTape.moveHeadRight();
        assertEquals("e", inputTape.readSymbol());
    }
    
    @Test
    public void testMoveHeadLeft() {
        inputTape.moveHeadRight();
        inputTape.moveHeadLeft();
        assertEquals("T", inputTape.readSymbol());
        assertEquals(0, inputTape.getHeadPosition());
    }
    
    @Test
    public void testMoveHeadRight() {
        inputTape.moveHeadRight();
        assertEquals("e", inputTape.readSymbol());
        assertEquals(1, inputTape.getHeadPosition());
    }
    
    @Test
    public void testTerminate() {
        inputTape.terminate();
        assertEquals(inputTape.getTape().length, inputTape.getHeadPosition());
    }
    
    @Test
    public void testWriteSymbol() {
        inputTape.writeSymbol("W");
        assertEquals("W", inputTape.getTape()[inputTape.getHeadPosition()]);
    }
}