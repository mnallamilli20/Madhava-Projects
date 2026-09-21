/**
 * @author Madhava Nallamilli
 */

package hw1;

public class Balloon 
{
	// Value for the heat loss factor.
	private double heatLoss = 0.1;

	
	// Value for the volume of air in the balloon in m^3.
	private int vol = 61234;

	// Value for the gas constant in J/kgK.
	private double gasConstant = 287.05;


	// Value for the standard pressure in hPa..
	private double standardPressure = 1013.25;


	// Value for Kelvin at 0 degrees C.
	private double kelvinsAtZeroC = 273.15;


	// Value for the outside air's temperature in Celsius.
	private double airTemp;


	// Value for the temperature of the air inside the balloon.
	private double balloonTemp;


	// Value for direction of the wind in degrees.
	private double windDirection;


	// Value for the altitude.
	private double altitude;


	// Value for the remaining amount of fuel.
	private double remaningFuel;


	// Value for the burn rate of the fuel.
	private double fuelBurnRate;


	// Value for the mass of the balloon.
	private double balloonMass;


	// Value for the velocity.
	private double velocity;


	// Value for the length of the tether.
	private double tetherLength;


	// Value for the simulation time.
	private double simulationTime;
	
	// Value for the original wind direction
	private double originalWindDir;
	
	// Value for the original air temperature
	private double originalAirTemp;






	/**
	 * Constructs a balloon.
	 *
	 *
	@param airTemp        Outside air temperature in Celsius.
	 *
	@param windDirection  Wind direction in degrees (between 0 and 360).
	 */
	public Balloon(double airTemp, double windDirection)
	{
	   this.airTemp = airTemp;
	   this.balloonTemp = airTemp;
	   this.windDirection = windDirection;
	  
	   originalWindDir = windDirection;
	   originalAirTemp = airTemp;
	}
	
	
	/**
	 * Gets the remaining fuel in the balloon.
	 *
	 *
	@return Remaining fuel.
	 */
	public double getFuelRemaining() 
	{
		return remaningFuel;
	}
	
	
	/**
	 * Sets the remaining fuel used in the balloon.
	 *
	 *
	@param fuel The amount of fuel to set.
	 */
	public void setFuelRemaning(double fuel) 
	{
		this.remaningFuel = fuel;
	}
	
	
	/**
	 * Gets the mass of the balloon.
	 *
	 *
	@return The mass of the balloon.
	 */
	public double getBalloonMass() 
	{
		return balloonMass;
	}
	
	
	/**
	 * Sets the mass of the balloon.
	 *
	 *
	@param mass The mass to set.
	 */
	public void setBalloonMass(double mass) 
	{
		this.balloonMass = mass;
	}
	
	
	/**
	 * Gets the outside air temperature.
	 *
	 *
	@return Outside air temperature in
	Celsius.
	 */
	public double getOutsideAirTemp() 
	{
	   return airTemp;
	}
	
	
	/**
	 * Sets the outside air temperature.
	 *
	 *
	@param
	temp The outside air temperature to set
	 */
	public void setOutsideAirTemp(double temp) 
	{
		this.airTemp = temp;
	}
	
	
	/**
	 * Gets the fuel burn rate.
	 *
	 *
	@return The fuel burn rate.
	 */
	public double getFuelBurnRate() 
	{
	   return fuelBurnRate;
	}
	
	
	/**
	 * Sets the fuel burn rate.
	 *
	 *
	@param rate The fuel burn rate to set.
	 */
	public void setFuelBurnRate(double rate) 
	{
	   this.fuelBurnRate = rate;
	}
	
	
	/**
	 * Gets the balloon temperature.
	 *
	 *
	@return The balloon temperature.
	 */
	public double getBalloonTemp() 
	{
	   return balloonTemp;
	}
	
	
	/**
	 * Sets the balloon temperature.
	 *
	 *
	@param
	temp The balloon temperature to set.
	 */
	public void setBalloonTemp(double temp) 
	{
	   this.balloonTemp = temp;
	}
	
	
	/**
	 *
	Ges the velocity of the balloon.
	 *
	 *
	@return The velocity of the balloon.
	 */
	public double getVelocity() 
	{
	   return velocity;
	}
	
	
	/**
	 * Gets the altitude of the balloon.
	 *
	 *
	@return The altitude of the balloon.
	 */
	public double getAltitude() 
	{
	   return altitude;
	}
	
	
	/**
	 * Gets the length of the
	tether.
	 *
	 *
	@return The length of the
	tether.
	 */
	public double getTetherLength() 
	{
	   return tetherLength;
	}
	
	
	/**
	 * Gets the remaining length of the
	tether.
	 *
	@return Remaining length of the
	tether.
	 */
	public double getTetherRemaining() 
	{
	   return tetherLength - altitude;
	}
	
	
	/**
	 * Sets the length of the
	tether.
	 *
	 *
	@param length The length of the
	tether to set.
	 */
	public void setTetherLength(double length) 
	{
	   this.tetherLength = length;
	}
	
	
	/**
	 * Gets the wind direction in degrees.
	 *
	@return The wind direction in degrees.
	 */
	public double getWindDirection() 
	{
	   return windDirection;
	}
	
	
	/**
	 * Changes the wind direction by adding the given value.
	 *
	 *
	@param
	degree The value to add to the current wind direction.
	 */
	public void changeWindDirection(double degree) 
	{
	   windDirection += degree;
	   windDirection = (windDirection + 360) % 360;
	}
	
	
	/**
	 * Gets the number of full minutes that have passed in the simulation.
	 *
	 *
	@return The number of full minutes.
	 */
	public long getMinutes() 
	{
	   return (long) (simulationTime / 60);
	}
	
	
	/**
	 * Gets the number of seconds past the number of full minutes.
	 *
	 *
	@return The number of seconds (between 0 and 59 inclusive).
	 */
	public long getSeconds() {
	   return (long) (simulationTime % 60);
	}
	
	
	/**
	 * updates variables after balloon moves
	 */
	public void update() 
	{
	   simulationTime += 1;
	   fuelBurnRate = Math.min(remaningFuel, fuelBurnRate);
	   
	   double fuelConsumed = Math.min(fuelBurnRate, remaningFuel);
	   remaningFuel -= fuelConsumed;
	   
	   double changeT = fuelBurnRate + (airTemp - balloonTemp) * heatLoss; 
	   balloonTemp += changeT;
	
	   double airDensity = standardPressure / (gasConstant * (airTemp + kelvinsAtZeroC));
	   
	   double balloonDensity = standardPressure / (gasConstant * (balloonTemp + kelvinsAtZeroC));
	   
	   double forceLift = vol * (airDensity - balloonDensity) * 9.81;
	   
	   double forceGravity = 9.81 * balloonMass;
	   
	   double netForce = forceLift - forceGravity;
	   
	   double netAcceleration = netForce / balloonMass;
	   velocity = velocity + netAcceleration;
	   altitude += velocity;
	   altitude = Math.max(0, Math.min(tetherLength, altitude));
	}
	
	
	/**
	 * Resets the simulation to the beginning.
	 */
	public void reset() 
	{
	   this.airTemp = originalAirTemp;
	   
	   this.windDirection = originalWindDir;
	   
	   this.remaningFuel = 0;
	   
	   this.fuelBurnRate = 0;
	   
	   this.balloonMass = 0;
	   
	   this.velocity = 0;
	   
	   this.altitude = 0;
	   
	   this.tetherLength = 0;
	   
	   this.simulationTime = 0;
	   
	   this.balloonTemp = airTemp;
	}

}
