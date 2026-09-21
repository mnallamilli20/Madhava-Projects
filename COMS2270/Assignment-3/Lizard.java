package hw3;
/**
 * @author Madhava Nallamilli
 */
import java.util.ArrayList;
import api.BodySegment;
import api.Cell;
import api.Direction;

/**
 * Represents a Lizard as a collection of body segments.
 */
public class Lizard {
	/**
	 * Constructs a Lizard object.
	 */
	private ArrayList<BodySegment> segments;
	public Lizard() {
		
		segments = new ArrayList<>();
	}

	/**
	 * Sets the segments of the lizard. Segments should be ordered from tail to
	 * head.
	 * 
	 * @param segments list of segments ordered from tail to head
	 */
	public void setSegments(ArrayList<BodySegment> segments) {
		this.segments = segments;
	}

	/**
	 * Gets the segments of the lizard. Segments are ordered from tail to head.
	 * 
	 * @return a list of segments ordered from tail to head
	 */
	public ArrayList<BodySegment> getSegments() {
		
		return segments;
	}

	/**
	 * Gets the head segment of the lizard. Returns null if the segments have not
	 * been initialized or there are no segments.
	 * 
	 * @return the head segment
	 */
	public BodySegment getHeadSegment() {		
		if (!segments.isEmpty()) {
			return segments.get(segments.size() - 1);
		}
		else {
			return null;
		}
	}

	/**
	 * Gets the tail segment of the lizard. Returns null if the segments have not
	 * been initialized or there are no segments.
	 * 
	 * @return the tail segment
	 */
	public BodySegment getTailSegment() {		
		if (!segments.isEmpty()) {
			return segments.get(0);
		}
		else {
			return null;
		}
	}

	/**
	 * Gets the segment that is located at a given cell or null if there is no
	 * segment at that cell.
	 * 
	 * @param cell to look for lizard
	 * @return the segment that is on the cell or null if there is none
	 */
	public BodySegment getSegmentAt(Cell cell) {
		
		for (BodySegment segment : segments) {
			if (segment.getCell().equals(cell)) {
				return segment;
			}
		}
		return null;
	}

	/**
	 * Get the segment that is in front of (closer to the head segment than) the
	 * given segment. Returns null if there is no segment ahead.
	 * 
	 * @param segment the starting segment
	 * @return the segment in front of the given segment or null
	 */
	public BodySegment getSegmentAhead(BodySegment segment) {
		
		int currentIndex = segments.indexOf(segment);
        if (currentIndex != -1 && currentIndex < segments.size() - 1) {
            return segments.get(currentIndex + 1);
        }
        return null;
	}

	/**
	 * Get the segment that is behind (closer to the tail segment than) the given
	 * segment. Returns null if there is not segment behind.
	 * 
	 * @param segment the starting segment
	 * @return the segment behind of the given segment or null
	 */
	public BodySegment getSegmentBehind(BodySegment segment) {
		
		int currentIndex = segments.indexOf(segment);
        if (currentIndex > 0) {
            return segments.get(currentIndex - 1);
        }
        return null;
	}

	/**
	 * 
	 * @param fromCell
	 * @param toCell
	 * @return
	 */
	private Direction directionFromTo(Cell fromCell, Cell toCell) {
        int dRow = toCell.getRow() - fromCell.getRow();
        int dCol = toCell.getCol() - fromCell.getCol();

        if (dRow == -1 && dCol == 0) {
            return Direction.UP;
        } else if (dRow == 1 && dCol == 0) {
            return Direction.DOWN;
        } else if (dRow == 0 && dCol == -1) {
            return Direction.LEFT;
        } else if (dRow == 0 && dCol == 1) {
            return Direction.RIGHT;
        } else {
            return null; 
        }
    }
	/**
	 * Gets the direction from the perspective of the given segment point to the
	 * segment ahead (in front of) of it. Returns null if there is no segment ahead
	 * of the given segment.
	 * 
	 * @param segment the starting segment
	 * @return the direction to the segment ahead of the given segment or null
	 */
	public Direction getDirectionToSegmentAhead(BodySegment segment) {
		
		BodySegment aheadSegment = getSegmentAhead(segment);
        if (aheadSegment != null) {
            Cell currentCell = segment.getCell();
            Cell aheadCell = aheadSegment.getCell();
            return directionFromTo(currentCell, aheadCell);
        }
        return null;
	}

	/**
	 * Gets the direction from the perspective of the given segment point to the
	 * segment behind it. Returns null if there is no segment behind of the given
	 * segment.
	 * 
	 * @param segment the starting segment
	 * @return the direction to the segment behind of the given segment or null
	 */
	public Direction getDirectionToSegmentBehind(BodySegment segment) {
		BodySegment behindSegment = getSegmentBehind(segment);
        if (behindSegment != null) {
            Cell currentCell = segment.getCell();
            Cell behindCell = behindSegment.getCell();
            return directionFromTo(currentCell, behindCell);
        }
        return null;
	}
	

	/**
	 * Gets the direction in which the head segment is pointing. This is the
	 * direction formed by going from the segment behind the head segment to the
	 * head segment. A lizard that does not have more than one segment has no
	 * defined head direction and returns null.
	 * 
	 * @return the direction in which the head segment is pointing or null
	 */
	public Direction getHeadDirection() {
		if (segments.size() <= 1) {
	        return null;
	    }
	    
	    // Get the segment ahead of the head segment
	    BodySegment aheadSegment = segments.get(segments.size() - 2);
	    
	    // Get the head segment
	    BodySegment headSegment = segments.get(segments.size() - 1);
	    
	    // Calculate direction from aheadSegment to headSegment
	    return directionFromTo(aheadSegment.getCell(), headSegment.getCell());
        
	}

	/**
	 * Gets the direction in which the tail segment is pointing. This is the
	 * direction formed by going from the segment ahead of the tail segment to the
	 * tail segment. A lizard that does not have more than one segment has no
	 * defined tail direction and returns null.
	 * 
	 * @return the direction in which the tail segment is pointing or null
	 */
	public Direction getTailDirection() {
		if (segments.size() <= 1) {
	        return null;
	    }
	    
	    // Get the segment behind the tail segment
	    BodySegment behindSegment = segments.get(1);
	    
	    // Get the tail segment
	    BodySegment tailSegment = segments.get(0);
	    
	    // Calculate direction from behindSegment to tailSegment
	    return directionFromTo(behindSegment.getCell(), tailSegment.getCell());
        
	}

	@Override
	public String toString() {
		String result = "";
		for (BodySegment seg : getSegments()) {
			result += seg + " ";
		}
		return result;
	}
}
