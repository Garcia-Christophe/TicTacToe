package launcher;

import javax.swing.SwingUtilities;

import main.TicTacToe;

/**
 * The Launcher is used to launch the game TicTacToe, it contains the main
 * method.
 * 
 * @author Christophe
 * @version 1.0
 */
public class Launcher {

	/**
	 * Main
	 * 
	 * @param args possible arguments
	 */
	public static void main(String args[]) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new TicTacToe();
			}
		});
	}
}