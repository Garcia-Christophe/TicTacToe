package main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.swing.JPanel;

class Painter extends JPanel implements MouseListener {

	/**
	 * Generated serial version ID.
	 */
	private static final long serialVersionUID = -4101607528977330025L;

	TicTacToe game;

	public Painter(TicTacToe ttt) {
		if (ttt == null)
			throw new IllegalArgumentException("Painter: Painter(TicTacToe): \"ttt\" is null.");
		else {
			this.game = ttt;
			this.setFocusable(true);
			this.requestFocus();
			this.setBackground(Color.white);
			this.addMouseListener(this);
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		this.game.render(g);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (this.game.getAccepted()) {
			if (this.game.getMyTurn() && !this.game.getUnableToCommunicateWithOpponent() && !this.game.getEnd()) {
				int x = e.getX() / 174;
				int y = e.getY() / 174;
				y *= 3;
				int position = x + y;

				if (this.game.getSpaces()[position] == null) {
					this.game.setPositionToX(position);
					this.game.setMyTurn(false);
					this.repaint();
					Toolkit.getDefaultToolkit().sync();

					try {
						this.game.getDOS().writeInt(position);
						this.game.getDOS().flush();
					} catch (IOException e1) {
						this.game.incErrors();
						e1.printStackTrace();
					}

					System.out.println("Data was sent.");
					this.game.checkForEnd();
				}
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}
