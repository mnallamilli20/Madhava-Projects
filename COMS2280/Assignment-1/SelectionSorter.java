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
 * This class implements selection sort.   
 *
 */

public class SelectionSorter extends AbstractSorter
{
	
	/**
	 * Constructor takes an array of points.  It invokes the superclass constructor, and also 
	 * set the instance variables algorithm in the superclass.
	 *  
	 * @param pts  
	 */
	public SelectionSorter(Point[] pts)  
	{
		// TODO 
		super(pts);
		super.algorithm = "selection sort";
	}	

	
	/** 
	 * Apply selection sort on the array points[] of the parent class AbstractSorter.  
	 * 
	 */
	@Override 
	public void sort()
	{
		// TODO 
		for(int i = 0; i < points.length; i++) {
			int minimum = i;
			
			for(int j = i + 1; j < points.length; j++) {
				if(points[j].compareTo(points[minimum]) < 0) {
					minimum = j;
				}
			
			if(minimum != i) {
				swap(i,minimum);
			}
			}
		}
	}	
}
