package hw2;
import java.lang.String;

/**
 * Models a simplified baseball-like game called Fuzzball.
 * 
 * @author Madhava Nallamilli
 */
public class FuzzballGame 
{

	private boolean gameEnded;
	private int ballCount;
	private boolean[] bases;
	private int calledStrikes;
	private int currentOuts;
	private int team0Score;
	private int team1Score;
	private boolean topOfInning;
	private int whichInning;
	private int givenNumInnings;

	/**
	 * Constructs an object called FuzzballFame with the given number of innings.
	 * @param givenNumInnings: The number of innings in the game.
	 */
	public FuzzballGame(int givenNumInnings) 
	{
		this.givenNumInnings = givenNumInnings;
		gameEnded = false;
		ballCount = 0;
		bases = new boolean[3];
		calledStrikes = 0;
		currentOuts = 0;
		team0Score = 0;
		team1Score = 0;
		topOfInning = true;
		whichInning = 1;
	}

	/**
	 * Number of strikes causing a player to be out.
	 */
	public static final int MAX_STRIKES = 2;

	/**
	 * Number of balls causing a player to walk.
	 */
	public static final int MAX_BALLS = 5;

	/**
	 * Number of outs before the teams switch.
	 */
	public static final int MAX_OUTS = 3;

	/**
	 * Represents base 1 as index 0.
	 */
	private static final int base = 0;

	/**
	 * Represents base 2 as index 1.
	 */
	private static final int base2 = 1;

	/**
	 * Represent base 3 as index 2.
	 */
	private static final int base3 = 2;

	
	/**
	 * Checks if the game has ended.
	 * @return: true if the game has ended, false otherwise.
	 */
	public boolean gameEnded() 
	{
		return gameEnded;
	}

	/**
	 * Gets the current count of balls.
	 * @return: the current ball count.
	 */
	public int getBallCount() 
	{
		return ballCount;
	}

	/**
	 * Gets the count of called strikes.
	 * @return: the count of called strikes.
	 */
	public int getCalledStrikes() 
	{
		return calledStrikes;
	}

	/**
	 * Gets the count of current outs.
	 * @return: the count of current outs.
	 */
	public int getCurrentOuts() 
	{
		return currentOuts;
	}

	/**
	 * Gets the score of Team 0.
	 * @return: the score of Team 0.
	 */
	public int getTeam0Score() 
	{
		return team0Score;
	}

	/**
	 * Gets the score of Team 1.
	 * @return: the score of Team 1.
	 */
	public int getTeam1Score() 
	{
		return team1Score;
	}

	/**
	 * Checks if it is the top of the inning.
	 * @return: true if it is the top of the inning, false otherwise
	 */
	public boolean isTopOfInning() 
	{
		return topOfInning;
	}

	/**
	 * Increments the ball count and updates the game state
	 * based on the current ball count and bases.
	 */
	public void ball() 
	{
		if (gameEnded) 
		{
	        return;
	    }
	    
	    ballCount++;
	    
	    if (ballCount == MAX_BALLS) 
	    {
	        resetCounters();
	        
	        if (!bases[base]) 
	        {
	            bases[base] = true;
	        } 
	        else if (!bases[base2]) 
	        {
	            bases[base2] = true;
	        } 
	        else if (!bases[base3]) 
	        {
	            bases[base3] = true;
	        } 
	        else 
	        {
	            addPoint();
	        }
	    }
	}

	/**
	 * Handles a caught fly ball, updating game state if the game has not ended.
	 */
	public void caughtFly() 
	{
		if(!gameEnded()) 
		{
			handleOuts();
		}
	}

	/**
	 * Handles a hit based on the distance, updating bases and scores accordingly.
	 * @param distance: The distance of the hit.
	 */
	public void hit(int distance) 
	{
		if(gameEnded == true) 
		{
			return;
		}
		calledStrikes = 0;
		ballCount = 0;
		if (distance < 15) 
		{
			handleOuts();
		}
		if (15 <= distance && distance < 150) 
		{
			advanceRunner();
		} 
		else if (150 <= distance && distance < 200) 
		{
			if (bases[base3] == true) 
			{
				addPoint();
				bases[base3] = false;
			}
			if (bases[base2] == true) 
			{
				addPoint();
				bases[base2] = false;
			}
			if (bases[base] == true) 
			{
				bases[base3] = true;
				bases[base] = false;

			}
			bases[base2] = true;

		} 
		else if (200 <= distance && distance < 250) 
		{
			if (bases[base3] == true) 
			{
				addPoint();
				bases[base3] = false;
			}
			if (bases[base2] == true) 
			{
				addPoint();
				bases[base2] = false;
			}
			if (bases[base] == true) 
			{
				addPoint();
				bases[base] = false;
			}
			bases[base3] = true;

		} 
		else if (distance >= 250) 
		{
			if (bases[base3] == true) 
			{
				addPoint();
				bases[base3] = false;
			}
			if (bases[base2] == true) 
			{
				addPoint();
				bases[base2] = false;
			}
			if (bases[base] == true) 
			{
				addPoint();
				bases[base] = false;

			}
			addPoint();
		}
	}

	/**
	 * Handles outs, updates current outs and possibly switches teams and resets the strike and ball count.
	 */
	private void handleOuts() 
	{
		currentOuts++;

		if (currentOuts >= MAX_OUTS) 
		{
			switchTeam();
			resetCounters();
		} else {
			resetCounters();
		}
	}

	/**
	 * Switches current team and resets counters and bases.
	 */
	private void switchTeam() 
	{
		currentOuts = 0;
		calledStrikes = 0;
		ballCount = 0;
		bases[0] = false;
		bases[1] = false;
		bases[2] = false;
		if (topOfInning == false) 
		{
			whichInning++;
			if (whichInning > givenNumInnings) 
			{
				gameEnded = true;
			}

		}
		topOfInning = !topOfInning;
	}

	/**
	 * Adds a point to the appropriate team's score based on current inning.
	 */
	private void addPoint() 
	{
		if (isTopOfInning()) 
		{
			team0Score++;
		} 
		else 
		{
			team1Score++;
		}
	}

	/**
	 * Advances runner on bases depending on current base occupancy. 
	 */
	private void advanceRunner() 
	{
		
		if (bases[base3]) 
		{
			bases[base3] = false;
			addPoint();
		} 
		else if (bases[base2]) 
		{
			bases[base2] = false;
			bases[base3] = true;
		} 
		else if (bases[base]) 
		{
			bases[base] = false;
			bases[base2] = true;
		}
		bases[base] = true;
	}

	/**
	 * Resets the called strikes and ball count.
	 */
	private void resetCounters() 
	{
		calledStrikes = 0;
		ballCount = 0;
	}

	/**
	 * Checks if there is a runner on a specific base.
	 * @param which: The  base to check (1,2, or 3).
	 * @return: If there is a runner on the specified base, false otherwise.
	 */
	public boolean runnerOnBase(int which) 
	{
		if (which < 1 || which > 3) 
		{
			return false;
		}
		return bases[which - 1];
	}

	/**
	 * Registers a strike, updating the game state if necessary.
	 * @param swung: Indicates whether the strike was swung at. 
	 */
	public void strike(boolean swung) 
	{
		 if (gameEnded) 
		 {
		        return;
		    }
		    if (swung) 
		    {
		        handleOuts();
		        resetCounters();
		    } 
		    else 
		    {
		        calledStrikes++;
		        if (calledStrikes == MAX_STRIKES) 
		        {
		            handleOuts();
		            resetCounters();
		        }
		    }
	}

	/**
	 * Retrieves the current inning.
	 * @return: The current inning number. 
	 */
	public int whichInning() 
	{
		return whichInning;
	}

	// The methods below are provided for you and you should not modify them.
	// The compile errors will go away after you have written stubs for the
	// rest of the API methods.
	/**
	 * Returns a three-character string representing the players on base, in the
	 * order first, second, and third, where 'X' indicates a player is present and
	 * 'o' indicates no player. For example, the string "oXX" means that there are
	 * players on second and third but not on first.
	 * 
	 * @return three-character string showing players on base
	 */
	public String getBases() 
	{
		return (runnerOnBase(1) ? "X" : "o") + (runnerOnBase(2) ? "X" : "o") + (runnerOnBase(3) ? "X" : "o");
	}

	/**
	 * Returns a one-line string representation of the current game state. The
	 * format is:
	 * 
	 * <pre>
	 *      ooo Inning:1 [T] Score:0-0 Balls:0 Strikes:0 Outs:0
	 * </pre>
	 * 
	 * The first three characters represent the players on base as returned by the
	 * <code>getBases()</code> method. The 'T' after the inning number indicates
	 * it's the top of the inning, and a 'B' would indicate the bottom. The score
	 * always shows team 0 first.
	 * 
	 * @return a single line string representation of the state of the game
	 */
	public String toString() 
	{
		String bases = getBases();
		String topOrBottom = (isTopOfInning() ? "T" : "B");
		String fmt = "%s Inning:%d [%s] Score:%d-%d Balls:%d Strikes:%d Outs:%d";
		return String.format(fmt, bases, whichInning(), topOrBottom, getTeam0Score(), getTeam1Score(), getBallCount(),
				getCalledStrikes(), getCurrentOuts());
	}
}