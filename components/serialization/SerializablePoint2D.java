package components.serialization;

import java.io.Serializable;
import javafx.geometry.Point2D;

/**
 * A serializable version of the Point2D class from JavaFX.
 * 
 * @see javafx.geometry.Point2D
 */
public class SerializablePoint2D implements Serializable {

    private static final long serialVersionUID = 1L; // Needed for the Serializable interface
    
    private double x;
    private double y;
    
    // Constructors
    public SerializablePoint2D(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public SerializablePoint2D(Point2D point) {
        this.x = point.getX();
        this.y = point.getY();
    }
    
    /*
     * Default constructor which randomly generates a point within the range of 
     * 100 to 700 for x and 100 to 500 for y.
     */
    public SerializablePoint2D() {
        this.x = Math.random() * 600 + 100;
        this.y = Math.random() * 400 + 100;
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public void setY(double y) {
        this.y = y;
    }
    
    /**
     * Calculates the distance between this point and another point.
     * 
     * @param point The point to calculate the distance to
     * @return The distance between this point and the given point
     */
    public double distance(SerializablePoint2D point) {
        return Math.sqrt(Math.pow(point.getX() - x, 2) + Math.pow(point.getY() - y, 2));
    }
}
