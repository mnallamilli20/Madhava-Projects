package edu.iastate.cs228.hw1;

import java.io.FileNotFoundException;
import java.lang.NumberFormatException; 
import java.lang.IllegalArgumentException; 
import java.util.InputMismatchException;


/**
 *  
 * @author Madhava Nallamilli
 *
 */

/**
 * 
 * This class implements insertion sort.   
 *
 */

public class InsertionSorter extends AbstractSorter 
{
	
	/**
	 * Constructor takes an array of points.  It invokes the superclass constructor, and also 
	 * set the instance variables algorithm in the superclass.
	 * 
	 * @param pts  
	 */
	public InsertionSorter(Point[] pts) 
	{
		// TODO 
		super(pts);
		super.algorithm = "insertion sort";
	}	

	
	/** 
	 * Perform insertion sort on the array points[] of the parent class AbstractSorter.  
	 */
	@Override 
	public void sort()
	{
		// TODO 
		for(int i = 1; i < points.length; i++)
		{
			int j = i;
			while((j > 0) && (points[j - 1].compareTo(points[j]) > 0))
			{
				swap(j,j-1);
				j--;
			}
		}
	}		
}
