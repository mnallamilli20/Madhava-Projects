package hw4;

/**
 * An element in which the <code>update</code> method updates the position each
 * frame according to a <em>velocity</em> vector (deltaX, deltaY). The units are
 * assumed to be "pixels per frame".
 * 
 * @author Madhava Nallamilli
 */
//TODO: This class must directly or indirectly extend GameElement
public class MovingElement extends SimpleElement {
	
	/**
	 * Instance variables
	 */
    private double deltaX;
    private double deltaY;
    
    /**
	 * Constructs a MovingElement with a default velocity of zero in both
	 * directions.
	 * 
	 * @param x      x-coordinate of upper left corner
	 * @param y      y-coordinate of upper left corner
	 * @param width  object's width
	 * @param height object's height
	 */
    public MovingElement(double x, double y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void update() {
    	super.update();
    	double newX = getXReal() + deltaX;
        double newY = getYReal() + deltaY;
        setPosition(newX, newY); 
    }
    
    /**
     * 
     * @return deltaX
     */
    public double getDeltaX() {
        return deltaX;
    }

    /**
     * 
     * @return deltaY
     */
    public double getDeltaY() {
        return deltaY;
    }

    /**
     * 
     * @param deltaX
     * @param deltaY
     */
    public void setVelocity(double deltaX, double deltaY) {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }
}