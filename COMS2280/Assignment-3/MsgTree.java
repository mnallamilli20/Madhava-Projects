package edu.iastate.cs2280.hw3;

import java.util.Stack;

/**
 *  
 * @author Madhava Nallamilli
 *
 */

public class MsgTree {
	
	
	public char payloadChar;
	public MsgTree left;
	public MsgTree right;
	
	
	
	private static int staticCharIdx = 0;
	
	/**
     * Constructs MsgTree constructor from encoding string
     * 
     * @param encodingString the string that represents the binary tree encoding
     */
	public MsgTree(String encodingString){
		if (staticCharIdx == 0) {
            staticCharIdx = 0; 
        }

        if (staticCharIdx >= encodingString.length()) return;

        this.payloadChar = encodingString.charAt(staticCharIdx++);

        if (this.payloadChar == '^') {
            this.left = new MsgTree(encodingString);
            this.right = new MsgTree(encodingString);
        }
    
	}
	
	/**
     * Constructs leaf node with specified character payload
     * 
     * @param payloadChar the character to store in this leaf node
     */
	public MsgTree(char payloadChar){
		this.payloadChar = payloadChar;
        this.left = null;
        this.right = null;
	}
	
	/**
     * Recursively prints characters and corresponding binary codes
     * from the tree
     * 
     * @param root the root of the tree to traverse
     * @param code the current binary code being built for the character
     */
	public static void printCodes(MsgTree root, String code){
		if (root == null) return;

        if (root.left == null && root.right == null) {
            String displayChar = (root.payloadChar == '\n') ? "\\n" : (root.payloadChar == ' ') ? "' '" : String.valueOf(root.payloadChar);
            System.out.printf("%s\t%s\n", displayChar, code);
        }

        printCodes(root.left, code + "0");
        printCodes(root.right, code + "1");
	}
	
	/**
     * Decodes given compressed message using binary tree and prints decoded message
     * Calls printStatistics() to display statistics about encoding
     * 
     * @param codes the root of the MsgTree containing the encoding scheme
     * @param msg the encoded bit string to decode
     */
	public void decode(MsgTree codes, String msg) {
	    MsgTree currentNode = codes;
	    StringBuilder decodedMessage = new StringBuilder();

	    for (char bit : msg.toCharArray()) {
	        currentNode = (bit == '0') ? currentNode.left : currentNode.right;

	        if (currentNode.left == null && currentNode.right == null) {
	            decodedMessage.append(currentNode.payloadChar);
	            currentNode = codes;  
	        }
	    }

	 
	    System.out.println(decodedMessage.toString());
	    printStatistics(msg, decodedMessage.toString());
	}
	
	 /**
     * Resets the static character index used for decoding
     * 
     */
	public static void resetStaticCharIdx() {
	    staticCharIdx = 0;
	}
	
	 /**
     * Prints average bits per character, total characters decoded, and space savings
     * 
     * @param encoded the encoded message string
     * @param decoded the decoded message string
     */
	private void printStatistics(String encoded, String decoded) {
	    double avgBitsPerChar = (double) encoded.length() / decoded.length();
	    int totalChars = decoded.length();
	    double spaceSaving = (1.0 - ((double) encoded.length() / (decoded.length() * 16))) * 100;

	    System.out.println("STATISTICS:");
	    System.out.printf("Avg bits/char:       \t%.1f\n", avgBitsPerChar);
	    System.out.println("Total characters:\t" + totalChars);
	    System.out.printf("Space Saving:          \t%.1f%%\n", spaceSaving);
	}
}


