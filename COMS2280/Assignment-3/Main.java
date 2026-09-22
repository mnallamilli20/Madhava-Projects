package edu.iastate.cs2280.hw3;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Scanner;

/**
 *  
 * @author Madhava Nallamilli
 *
 *
 * Allows user to input file name and call methods from MsgTree to decode the given file
 */
public class Main {
    public static void main(String[] args) throws IOException {
    	Scanner scanner = new Scanner(System.in);
    	System.out.print("Please enter filename to decode: ");
    	String fileName = scanner.nextLine();
    	scanner.close();
        List<String> lines = Files.readAllLines(Paths.get(fileName));

        if (lines.size() < 2) {
            System.err.println("Invalid file format.");
            return;
        }

        // Build the pattern string 
        StringBuilder patternBuilder = new StringBuilder();
        for (int i = 0; i < lines.size() - 1; i++) {
            patternBuilder.append(lines.get(i));
            if (i < lines.size() - 2 || !lines.get(i).isEmpty()) {
                patternBuilder.append('\n'); 
            }
        }
        String pattern = patternBuilder.toString().replace("\r", "").replace("\uFEFF", ""); // Clean the pattern string

        
        String binCode = lines.get(lines.size() - 1).trim(); 

        // Reset static index before building the tree
        MsgTree.resetStaticCharIdx();
        MsgTree tree = new MsgTree(pattern);  

        
        System.out.println("character  code");
        System.out.println("-------------------------");
        MsgTree.printCodes(tree, "");

        // Decode the message and print
        System.out.println("MESSAGE:");
        tree.decode(tree, binCode);
    }
}