package hw3;
/**
 * @author Madhava Nallamilli
 */
import java.util.ArrayList;
import api.BodySegment;
import api.Cell;
import api.Direction;
import api.Exit;
import api.ScoreUpdateListener;
import api.ShowDialogListener;
import api.Wall;


/**
 * Class that models a game.
 */
public class LizardGame {
	private ShowDialogListener dialogListener;
	private ScoreUpdateListener scoreListener;
	private Cell[][] cells;
	private ArrayList<Lizard> lizs;


	/**
	 * Constructs a new LizardGame object with given grid dimensions.
	 *
	 * @param width  number of columns
	 * @param height number of rows
	 */
	public LizardGame(int width, int height) {
		this.lizs = new ArrayList<Lizard>();
		this.cells = new Cell[width][height];
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				this.cells[col][row] = new Cell(col,row);


			}
		}
	}


	/**
	 * Get the grid's width.
	 *
	 * @return width of the grid
	 */
	public int getWidth() {
		return cells.length;
	}


	/**
	 * Get the grid's height.
	 *
	 * @return height of the grid
	 */
	public int getHeight() {
		return cells[0].length;
	}


	/**
	 * Adds a wall to the grid.
	 * <p>
	 * Specifically, this method calls placeWall on the Cell object associated with the wall (see the Wall class for how to get the cell 
	 * associated with the wall). This class assumes a cell has already been set on the wall before being called.
	 *
	 * @param wall to add
	 */
	public void addWall(Wall wall) {
		wall.getCell().placeWall(wall);
	}


	/**
	 * Adds an exit to the grid.
	 * <p>
	 * Specifically, this method calls placeExit on the Cell object associated with the exit (see the Exit class for how to get the cell 
	 * associated with the exit). This class assumes a cell has already been set on the exit before being called.
	 *
	 * @param exit to add
	 */
	public void addExit(Exit exit) {
		exit.getCell().placeExit(exit);


	}


	/**
	 * Gets a list of all lizards on the grid. Does not iclude lizards that have exited.
	 *
	 * @return lizards list of lizards
	 */
	public ArrayList<Lizard> getLizards() {
		return lizs;
	}


	/**
	 * Adds the given lizard to the grid.
	 * <p>
	 * The scoreListener to should be updated with the number of lizards.
	 *
	 * @param lizard to add
	 */
	public void addLizard(Lizard lizard) {
		lizs.add(lizard);
		scoreListener.updateScore(lizs.size());
	}


	/**
	 * Removes the given lizard from the grid. Be aware that each cell object knows about a lizard that is placed on top of it. It is 
	 * expected that this method updates all cells that the lizard used to be on, so that they now have no lizard placed on them.
	 * <p>
	 * The scoreListener to should be updated with the number of lizards using updateScore().
	 *
	 * @param lizard to remove
	 */
	public void removeLizard(Lizard lizard) {
		for (BodySegment s : lizard.getSegments()) {
			s.getCell().removeLizard();
		}
		lizs.remove(lizard);
		scoreListener.updateScore(lizs.size());
	}


	/**
	 * Gets the cell for the given column and row.
	 * <p>
	 * If the column or row are outside of the boundaries of the grid the method returns null.
	 *
	 * @param col column of the cell
	 * @param row of the cell
	 * @return the cell or null
	 */
	public Cell getCell(int col, int row) {
		if (col >= 0 && col < getWidth() && row >= 0 && row < getHeight()) {
			return cells[col][row];
		}
		return null;
	}


	/**
	 * Gets the cell that is adjacent to (one over from) the given column and row, when moving in the given direction. For example (1, 4, UP) 
	 * returns the cell at (1, 3).
	 * <p>
	 * If the adjacent cell is outside of the boundaries of the grid, the method eturns null.
	 *
	 * @param col the given column
	 * @param row the given row
	 * @param dir the direction from the given column and row to the adjacent cell
	 * @return the adjacent cell or null
	 */
	public Cell getAdjacentCell(int col, int row, Direction dir) {
		int r = row;
		int c = col;
		switch (dir) {
		case UP: r--;
		break;
		case DOWN: r++;
		break;
		case LEFT: c--;
		break;
		case RIGHT: c++;
		break;
		}
		return  getCell(c, r);
	}


	/**
	 * Resets the grid. After calling this method the game should have a grid of size width x height containing all empty cells. Empty means 
	 * cells with no walls, exits, etc.
	 * <p>
	 * All lizards should also be removed from the grid.
	 *
	 * @param width  number of columns of the resized grid
	 * @param height number of rows of the resized grid
	 */
	public
	void resetGrid(int width, int height) {
		this.lizs  = new ArrayList<Lizard>();
		this.cells  = new Cell[width][height];
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				this.cells[col][row] = new Cell(col, row);
			}
		}
		// Removes all the lizards
		for (Lizard lizard : getLizards()) {
			removeLizard(lizard);
		}


	}


	/**
	 * Returns true if a given cell location (col, row) is available for a lizard to move into. Specifically the cell cannot contain a wall 
	 * or a lizard. Any other type of cell, icluding an exit is available.
	 *
	 * @param row of the cell being tested
	 * @param col of the cell being tested
	 * @return true if the cell is available, false otherwise
	 */
	public boolean isAvailable(int col, int row) {
		Cell c = getCell(col, row);
		return c != null && (c.isEmpty() || c.getExit() != 	null);
		}


	/**
	 * Move the lizard specified by its body segment at the given position (col, row) one cell in the given direction. The entire body of the
	 * lizard must move in a snake like fashion, in other words, each body segment pushes and pulls the segments it is connected to forward 
	 * or backward in the path of the lizard's body. The given direction may result in the lizard moving its body either forward or backward 
	 * by one cell.
	 * <p>
	 * The segments of a lizard's body are linked together and movement must always be "in-line" with the body. It is allowed to implement 
	 * movement by either shifting every body segment one cell over or by creating a new head or tail segment and removing an existing head 
	 * or tail segment to achieve the same effect of movement in the forward or backward direction.
	 * <p>
	 * If any segment of the lizard moves over an exit cell, the lizard should be removed from the grid.
	 * <p>
	 * If there are no lizards left on the grid the player has won the puzzle then the dialog listener should be used to display 
	 * (see showDialog) the message "You win!".
	 * <p>
	 * It is possible that the given direction is not in-line with the body of the lizard (as described above), in that case this method 
	 * should do nothing.
	 * <p>
	 * It is possible that the given column and row are outside the bounds of the grid, in that case this method should do nothing.
	 * <p>
	 * It is possible that there is no lizard at the given column and row, in that case this method should do nothing.
	 * <p>
	 * It is possible that the lizard is blocked and cannot move in the requested direction, in that case this method should do nothing.
	 *
	<p>
	 * <b> Developer's note: You may have noticed that there are a lot of details
	 * that need to be considered when implement this method method. It is highly
	 * recommend to explore how you can use the public API methods of this class,
	 * Grid and Lizard (hint: there are many helpful methods in those classes that
	 * will simplify your logic here) and also create your own private helper
	 * methods. Break the problem into smaller parts are work on each part
	 * individually.</b>
	 *
	 * @param col the given column of a selected segment
	 * @param row the given row of a selected segment
	 * @param dir the given direction to move the selected segment
	 */
	public void move(int col, int row, Direction dir) {
		if (col > 0 && col < (cells.length  - 1) && row > 0 && row < (cells[0].length  - 1)) {
			if  (getCell(col, row).getLizard() != null) {
				BodySegment seg = getCell(col, row).getLizard().getSegmentAt(getCell(col, row));
				Lizard liz = seg.getLizard(); 
				ArrayList<BodySegment> segs = liz.getSegments();
				BodySegment hS = liz.getHeadSegment();
				BodySegment tS = liz.getTailSegment();
				Cell tC = tS.getCell();
				// Head Front
				if (seg.equals(hS)) {
					if (liz.getHeadDirection().equals(dir)) { // Head forwards
						if (dir.equals(Direction.UP)) {
							if (isAvailable(col, row - 1)) {
								for (int i = 0; i < segs.size() - 1;i++) {
									segs.get(i).setCell(segs.get(i + 1).getCell());
								}
								tC.removeLizard();
								hS.setCell(getCell(col, row - 1));
							}
						}
						if (dir.equals(Direction.DOWN)) {
							if (isAvailable(col, row + 1)) {
								for (int i = 0; i < segs.size() - 1; i++) {
									segs.get(i).setCell(segs.get(i + 1).getCell());
								}
								tC.removeLizard();
								hS.setCell(getCell(col, row + 1));
							}
						}
						if (dir.equals(Direction.LEFT)) {
							if (isAvailable(col - 1, row)) {
								for (int i = 0; i < segs.size() - 1; i++) {
									segs.get(i).setCell(segs.get(i + 1).getCell());
								}
								tC.removeLizard();
								hS.setCell(getCell(col - 1, row));
							}
						}
						if (dir.equals(Direction.RIGHT)){
							if (isAvailable(col + 1, row)) {
								for (int i = 0; i < segs.size() - 1; i++) {
									segs.get(i).setCell(segs.get(i + 1).getCell());
								}
								tC.removeLizard();
								hS.setCell(getCell(col + 1, row));
							}
						}
					}
				}
				// Tail Front
				else if (seg.equals(tS)) {
					int cH = hS.getCell().getCol();
					int rH = hS.getCell().getRow();
					if (liz.getTailDirection() == Direction.UP && dir == Direction.DOWN) {
						if (isAvailable(cH,rH + 1)) {
							for (int i = 0; i < segs.size() - 1; i++) {
								segs.get(i).setCell(segs.get(i + 1).getCell());
							}
							tC.removeLizard();
							hS.setCell(getCell(cH, rH + 1));
						}
					}
					if (liz.getTailDirection() == Direction.DOWN && dir == Direction.UP) {
						if (isAvailable(cH, rH - 1)) {
							for (int i = 0; i < segs.size() - 1; i++) { 
								segs.get(i).setCell(segs.get(i + 1).getCell());
							}
							tC.removeLizard();
							hS.setCell(getCell(cH, rH - 1));
						}
					}
					if (liz.getTailDirection() == Direction.LEFT && dir == Direction.RIGHT)
					{
						if (isAvailable(cH + 1, rH)) {
							for (int i = 0; i < segs.size() - 1; i++) {
								segs.get(i).setCell(segs.get(i + 1).getCell());
							}
							tC.removeLizard();
							hS.setCell(getCell(cH + 1, rH));
						}
					}
					if (liz.getTailDirection() == Direction.RIGHT && dir == Direction.LEFT) {
						if (isAvailable(cH - 1, rH)) {
							for (int i = 0; i <segs.size() - 1; i++) {
								segs.get(i).setCell(segs.get(i + 1).getCell());
							}
							tC.removeLizard();
							hS.setCell(getCell(cH - 1, rH));
						}
					}
				}
				Cell c = getAdjacentCell(col, row, dir);
				if (c.getExit() != null) {
					removeLizard(liz);
				}
				if (lizs.size() == 0) {
					dialogListener.showDialog("You win!");
				}
			}
		}
	}


	/**
	 * Sets callback listeners for game events.
	 *
	 * @param dialogListener listener for creating a user dialog
	 * @param scoreListener  listener for updating the player's score
	 */
	public void setListeners(ShowDialogListener dialogListener, ScoreUpdateListener scoreListener) {
		this.dialogListener  = dialogListener; 
		this.scoreListener  = scoreListener;
	}


	/**
	 * Load the game from the given file path
	 *
	 * @param filePath location of file to load
	 */
	public void load(String filePath) {
		GameFileUtil.load(filePath, this);
	}


	@Override
	public String toString() {
		String str = "---------- GRID ----------\n";
		str += "Dimensions:\n";
		str += getWidth() + " " + getHeight() + "\n";
		str += "Layout:\n";
		for (int y = 0; y < getHeight(); y++) {
			if  (y > 0) {
				str += "\n";
			}
			for (int x = 0; x < getWidth(); x++) {
				str += getCell(x,y);
			}
		}
		str += "\nLizards:\n";
		for (Lizard l : getLizards()) {
			str += l;
		}
		str += "\n--------------------------\n";
		return str;
	}
}