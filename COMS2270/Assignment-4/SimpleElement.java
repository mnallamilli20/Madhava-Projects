package hw4;

// TODO:

// SimpleElement extends AbstractElement directly. 
// MovingElement and VanishingElement extend SimpleElement.
// AttachedElemtn, FlyingElement, FollowerElement, LiftElement, and PlatformElement extend MovingElement.


import java.awt.Rectangle;
import api.AbstractElement;

/**
 * Minimal concrete extension of AbstractElement. The <code>update</code> method
 * in this implementation just increments the frame count.
 * 
 * @author Madhava Nallamilli
 */
// TODO: This class must directly or indirectly extend AbstractElement
public class SimpleElement extends AbstractElement{

	/**
	 * Instance variables
	 */
	private int width;
	private int height;
	private double x;
	private double y;
	private int frameCount;
	private boolean markedForDeletion;
	/**
	 * Constructs a new SimpleElement.
	 * 
	 * @param x      x-coordinate of upper left corner
	 * @param y      y-coordinate of upper left corner
	 * @param width  element's width
	 * @param height element's height
	 */
	public SimpleElement(double x, double y, int width, int height) {	
		this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        frameCount = 0;
        markedForDeletion = false;
	}

	/**
	 * @return (int)Math.round(x)
	 */
	public int getXInt() {
		return (int)Math.round(x);
	}
	
	/**
	 * @return (int)Math.round(y)
	 */
	public int getYInt() {
		return (int)Math.round(y);
	}
	
	/**
	 * @return height
	 */
	public int getHeight() {
		return height;
	}
	
	/**
	 * @return width
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * @return Rectangle(getXInt(), getYInt(), getWidth(), getHeight())
	 */
	public Rectangle getRect(){
		return new Rectangle(getXInt(), getYInt(), getWidth(), getHeight());
	}

	@Override
	public double getXReal() { //
		return x;
	}
	
	@Override
	public double getYReal() { //
		return y;
	}
	
	@Override
	public void update() { //
		frameCount++;
	}
	
	@Override
	public int getFrameCount() { //
		return frameCount;
	}
	
	@Override
	public boolean isMarked() { //
		return markedForDeletion;
	}
	
	@Override
	public void markForDeletion() { //
		markedForDeletion = true;
	}

	@Override
	public void setPosition(double newX, double newY) {
		x = newX;
	    y = newY;
	}

	@Override
	public boolean collides(AbstractElement other) {
		java.awt.Rectangle rectangle1 = getRect();
	    java.awt.Rectangle rectangle2 = other.getRect();
	    return rectangle1.intersects(rectangle2);
	}
}