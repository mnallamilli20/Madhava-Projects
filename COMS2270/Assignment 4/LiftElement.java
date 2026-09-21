package hw4;

import java.util.ArrayList;
import api.AbstractElement;

/**
 * An element with two distinctive behaviors. First, it can be set up to move
 * vertically within a fixed set of boundaries. On reaching a boundary, the
 * y-component of its velocity is reversed. Second, it maintains a list of
 * <em>associated</em> elements whose basic motion all occurs relative to the
 * LiftElement.
 * 
 * @author Madhava Nallamilli
 */
//TODO: This class must directly or indirectly extend GameElement
public class LiftElement extends MovingElement{

	/**
	 * Instance variables
	 */
	private double min;
    private double max; 
    private ArrayList<AbstractElement> associatedElements;
	/**
	 * Constructs a new Elevator. Initially the upper and lower boundaries are
	 * <code>Double.NEGATIVE_INFINITY</code> and
	 * <code>Double.POSITIVE_INFINITY</code>, respectively.
	 * 
	 * @param x      x-coordinate of initial position of upper left corner
	 * @param y      y-coordinate of initial position of upper left corner
	 * @param width  element's width
	 * @param height element's height
	 */
	public LiftElement(double x, double y, int width, int height) {
		super(x, y, width, height);
        min = Double.NEGATIVE_INFINITY;
        max = Double.POSITIVE_INFINITY;
        associatedElements = new ArrayList<AbstractElement>();
	}

	@Override
	public void update() {
		super.update();
		if (getYReal() + getHeight() >= max) {
			setPosition(getXReal(), max - getHeight());
			setVelocity(getDeltaX(), getDeltaY() * -1);
		}
		else if (getYReal() <= min) {
			setPosition(getXReal(), min);
			setVelocity(getDeltaX(), getDeltaY() * -1);
		}
		for (int i = 0; i < associatedElements.size(); i++) {
			associatedElements.get(i).update();
		}
	}
	
	/**
	 * 
	 * @param attached
	 */
	public void addAssociated(AttachedElement attached) {
		attached.setBase(this);
        associatedElements.add(attached);
	}
	
	/**
	 * 
	 * @param follower
	 */
	public void addAssociated(FollowerElement follower) {
		follower.setBase(this);
        associatedElements.add(follower);
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
	
	/**
	 * 
	 * @return min
	 */
	public double getMin() {
		return min;
	}
	
	/**
	 * 
	 * @return max
	 */
	public double getMax() {
		return max;
	}
	
}