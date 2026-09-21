package hw3;
/**
 * @author Madhava Nallamilli
 */
import java.io.File;
import
java.util.ArrayList;
import java.util.Scanner;
import api.BodySegment;
import api.Cell;
import api.Exit;
import api.Wall;
/**
* Utility class with static methods for loading game files.
*/
public class GameFileUtil {
	/**
	 * Loads the file at the given file path into the given game object. When the method returns the game object has been modified to 
	 * represent the loaded game.
	 *
	 * @param filePath the path of the file to load
	 * @param game     the game to modify
	 */
	public static void load(String filePath, LizardGame game) {
		File file = new File(filePath); Scanner scan = null;
		try {
				scan = new Scanner(file);
			}
		catch(Exception e) {
			return;
			}
		String[]dims = scan.nextLine().split("x");
		int w = Integer.parseInt(dims[0]);
		int h = Integer.parseInt(dims[1]);
		game.resetGrid(w,h);
           
       for (int row = 0; row < h; row++) {
    	   String l = scan.nextLine();
           for (int col = 0; col < w; col++) {
               char symbol = l.charAt(col);
               Cell cell = game.getCell(col, row);
               if (symbol == 'W') {
            	   game.addWall(new  Wall(cell));
               }
               else if (symbol == 'E') {
                   game.addExit(new Exit(cell));
               }
               else if (symbol  == '.') {
                   break;
               }
           }
       }
       while (scan.hasNextLine()) {
           String l = scan.nextLine();
           if (l.startsWith("L")) {
               String[] parts = l.substring(2).split(" ");
               Lizard liz = new Lizard();
               ArrayList<BodySegment> segs = new ArrayList<>();
               for (String part : parts) {
                   String[] coordinates = part.split(",");
                   int col = Integer.parseInt(coordinates[0]);
                   int row = Integer.parseInt(coordinates[1]);
                   Cell segP = game.getCell(col, row);
                   BodySegment segment = new BodySegment(liz, segP);
                   segs.add(segment);
               }
               liz.setSegments(segs);
               game.addLizard(liz);
           }
       }
       scan.close();
	}
}