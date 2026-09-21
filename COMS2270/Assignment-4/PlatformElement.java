package hw4;

import api.AbstractElement;

import java.util.ArrayList; 


/**
 * A PlatformElement is an element with two distinctive behaviors. First, it can
 * be set up to move horizontally within a fixed set of boundaries. On reaching
 * a boundary, the x-component of its velocity is reversed. Second, it maintains
 * a list of <em>associated</em> elements whose basic motion all occurs relative
 * to the PlatformElement.
 * 
 * @author Madhava Nallamilli
 */
//TODO: This class must directly or indirectly extend AbstractElement
public class PlatformElement extends MovingElement{

	/**
	 * Instance variables
	 */
	private double min;
    private double max; 	
	private ArrayList<AbstractElement> associatedElements;
	/**
	 * Constructs a new PlatformElement. Initially the left and right boundaries are
	 * <code>Double.NEGATIVE_INFINITY</code> and
	 * <code>Double.POSITIVE_INFINITY</code>, respectively.
	 * 
	 * @param x      x-coordinate of initial position of upper left corner
	 * @param y      y-coordinate of initial position of upper left corner
	 * @param width  object's width
	 * @param height object's height
	 */
	public PlatformElement(double x, double y, int width, int height) {
		super(x, y, width, height);	
        min = -1 * Integer.MAX_VALUE;
        max = Integer.MAX_VALUE;   
        associatedElements = new ArrayList<AbstractElement>();
	}
	
	@Override
	public void update() {
		super.update();
		if (getXReal() + getWidth() >= max) {
			setPosition(max - getWidth(), getYReal());
			setVelocity(getDeltaX() * -1, getDeltaY());
		}
		else if (getXReal() <= min) {
			setPosition(min, getYReal());
			setVelocity(getDeltaX() * -1, getDeltaY());
		}
		for (int i = 0; i < associatedElements.size(); i++) {
			associatedElements.get(i).update();
		}
	}
	
	/**
	 * deletes elements that are marked
	 */
	public void deleteMarkedAssociated() {
		for (int i = associatedElements.size() - 1; i >= 0; i--) {
			if (associatedElements.get(i).isMarked()) {
				associatedElements.remove(i);
			}
		}
	}
	
	/**
	 * 
	 * @return max
	 */
	public double getMax() {
		return max;
	}
	
	/**
	 * 
	 * @return min
	 */
	public double getMin() {
		return min;
	}
	
	/**
	 * 
	 * @param attached
	 */
	public void addAssociated(AttachedElement attached) {
		associatedElements.add(attached);
	    attached.setBase(this);
	}
	
	/**
	 * 
	 * @param follower
	 */
	public void addAssociated(FollowerElement follower) {
		associatedElements.add(follower);
	    follower.setBase(this);
	}
	
	/**
	 * 
	 * @return associatedElements
	 */
	public java.util.ArrayList<AbstractElement> getAssociated(){
		return associatedElements;
	}
	
	/**
	 * 
	 * @param min
	 * @param max
	 */
	public void setBounds(double min, double max) {
		this.min = min;
	    this.max = max;
	}

}
