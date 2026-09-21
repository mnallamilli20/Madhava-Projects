package hw4;

/**
 * Moving element in which the vertical velocity is adjusted each frame by a
 * gravitational constant to simulate gravity. The element can be set to
 * "grounded", meaning gravity will no longer influence its velocity.
 * 
 * @author Madhava Nallamilli
 */
//TODO: This class must directly or indirectly extend GameElement
public class FlyingElement extends MovingElement{

	/**
	 * Instance variables
	 */
	private double gravity;
	private boolean grounded;
	
	/**
	 * Constructs a new FlyingElement. By default it should be grounded, meaning
	 * gravity does not influence its velocity.
	 * 
	 * @param x      x-coordinate of upper left corner
	 * @param y      y-coordinate of upper left corner
	 * @param width  element's width
	 * @param height element's height
	 */
	public FlyingElement(double x, double y, int width, int height) {
		super(x, y, width, height);
		gravity = 0;
		grounded = false;
	}
	
	/**
	 * 
	 * @param grounded
	 */
	public void setGrounded(boolean grounded) {
		this.grounded = grounded;
	}
	
	/**
	 * 
	 * @param gravity
	 */
	public void setGravity(double gravity) {
		this.gravity = gravity;
	}
	
	
	/**
	 * 
	 * @return grounded
	 */
	public boolean isGrounded() {
		return grounded;
	}

	@Override
	public void update() {
		super.update();
	    if (!grounded) {
	        setVelocity(getDeltaX(), getDeltaY() + gravity);
	    }
	}
}