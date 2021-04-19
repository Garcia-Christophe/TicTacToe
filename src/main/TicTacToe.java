package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class TicTacToe implements Runnable {

	private String ip = "localhost";
	private int port = 22222;
	private JFrame frame;
	private final int WIDTH = 510;
	private final int HEIGHT = 510;
	private Scanner scanner;
	private Thread thread;

	private Painter painter;
	private Socket socket;
	private DataOutputStream dos;
	private DataInputStream dis;

	private ServerSocket serverSocket;

	private BufferedImage board;
	private BufferedImage markX;
	private BufferedImage markO;

	private String[] spaces;

	private boolean myTurn;
	private boolean accepted;
	private boolean unableToCommunicateWithOpponent;

	// Won: 2, Loose: 1, Tie: 0, Else: -1
	private int won;
	private int lengthOfSpace;
	private int errors;
	private int firstSpot;
	private int secondSpot;

	private Font font;
	private Font smallFont;
	private Font largerFont;

	private String waitingString;
	private String unableToCommunicateWithOpponentString;
	private String endString;

	// 0, 1, 2
	// 3, 4, 5
	// 6, 7, 8
	private int[][] wins;

	public TicTacToe() {

		// Initializing
		this.scanner = new Scanner(System.in);
		this.spaces = new String[9];
		this.wins = new int[][] { { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 }, { 0, 3, 6 }, { 1, 4, 7 }, { 2, 5, 8 },
				{ 0, 4, 8 }, { 2, 4, 6 } };
		this.won = -1;
		this.lengthOfSpace = 151;
		this.errors = 0;
		this.firstSpot = -1;
		this.secondSpot = -1;
		this.font = new Font("Verdana", Font.BOLD, 32);
		this.smallFont = new Font("Verdana", Font.BOLD, 20);
		this.largerFont = new Font("Verdana", Font.BOLD, 50);
		this.waitingString = "Waiting for another player...";
		this.unableToCommunicateWithOpponentString = "Unable to communicate with opponent.";
		this.endString = "";

		// Asking the user to enter the IP and the port
		System.out.println("Please input the IP:");
		this.ip = this.scanner.nextLine();
		System.out.println("Please input the port:");
		this.port = scanner.nextInt();
		while (this.port < 1 || this.port > 65535) {
			System.out.println("The port you entered was invalid, please try another port:");
			this.port = this.scanner.nextInt();
		}

		this.loadImages();
		if (!this.connect())
			this.initializeServer();

		// frame
		this.frame = new JFrame();
		this.painter = new Painter(this);
		this.painter.setPreferredSize(new Dimension(this.WIDTH, this.HEIGHT));
		this.frame.setTitle("Tic-Tac-Toe Game");
		this.frame.setContentPane(this.painter);
		this.frame.pack();
		this.frame.setLocationRelativeTo(null);
		this.frame.setResizable(false);
		this.frame.setVisible(true);

		// thread
		this.thread = new Thread(this, "TicTacToe");
		this.thread.start();
	}

	public void run() {
		while (true) {
			this.tick();
			painter.repaint();

			if (!this.accepted)
				this.listenForServerRequest();
		}
	}

	private void listenForServerRequest() {
		Socket soc = null;
		try {
			soc = this.serverSocket.accept();
			this.dos = new DataOutputStream(soc.getOutputStream());
			this.dis = new DataInputStream(soc.getInputStream());
			this.accepted = true;
			System.out.println("Client has requested to join, and we have accepted.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void render(Graphics g) {
		g.drawImage(this.board, 0, 0, null);
		if (this.unableToCommunicateWithOpponent) {
			g.setColor(Color.red);
			g.setFont(this.smallFont);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			int stringWidth = g2.getFontMetrics().stringWidth(this.unableToCommunicateWithOpponentString);
			g.drawString(this.unableToCommunicateWithOpponentString, this.WIDTH / 2 - stringWidth, this.HEIGHT / 2);
			return;
		}

		if (this.accepted) {
			for (int i = 0; i < this.spaces.length; i++) {
				if (this.spaces[i] != null) {
					if (this.spaces[i].equalsIgnoreCase("X"))
						g.drawImage(this.markX, (i % 3) * this.lengthOfSpace + (i % 3) * (17 + 8) + 17,
								(int) (i / 3) * this.lengthOfSpace + (17 + 8) * (int) (i / 3) + 17, null);
					else if (this.spaces[i].equalsIgnoreCase("O"))
						g.drawImage(this.markO, (i % 3) * this.lengthOfSpace + (i % 3) * (17 + 8) + 17,
								(int) (i / 3) * this.lengthOfSpace + (17 + 8) * (int) (i / 3) + 17, null);
				}
			}

			if (this.won == 2 || this.won == 1 || this.won == 0) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setStroke(new BasicStroke(10));
				g.setColor(Color.black);
				g.drawLine(
						this.firstSpot % 3 * this.lengthOfSpace + 17 * (this.firstSpot % 3 + 1)
								+ this.lengthOfSpace / 2,
						(int) (this.firstSpot / 3) * this.lengthOfSpace + 17 * (int) (this.firstSpot / 3 + 1)
								+ this.lengthOfSpace / 2,
						this.secondSpot % 3 * this.lengthOfSpace + 17 * (this.secondSpot % 3 + 1)
								+ this.lengthOfSpace / 2,
						(int) (this.secondSpot / 3) * this.lengthOfSpace + 17 * (int) (this.secondSpot / 3 + 1)
								+ this.lengthOfSpace / 2);

				g.setColor(Color.red);
				g.setFont(this.largerFont);
				if (this.won == 2)
					this.endString = "WON";
				else if (this.won == 1)
					this.endString = "LOOSE";
				else if (this.won == 0)
					this.endString = "TIE";
				int stringWidth = g2.getFontMetrics().stringWidth(this.endString);
				g.drawString(this.endString, this.WIDTH / 2 - stringWidth / 2, this.HEIGHT / 2);
			}
		} else {
			g.setColor(Color.red);
			g.setFont(this.font);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			int stringWidth = g2.getFontMetrics().stringWidth(this.waitingString);
			g.drawString(this.waitingString, this.WIDTH / 2 - stringWidth / 2, this.HEIGHT / 2);
		}
	}

	private void tick() {
		if (this.errors >= 10)
			unableToCommunicateWithOpponent = true;

		if (!myTurn && !unableToCommunicateWithOpponent) {
			try {
				int space = dis.readInt();
				this.spaces[space] = "O";
				this.checkForEnd();
				this.myTurn = true;
			} catch (IOException e) {
				e.printStackTrace();
				this.incErrors();
			}
		}
	}

	public void checkForEnd() {
		int i = 0;

		while (this.won != 2 && this.won != 1 && i < this.wins.length) {
			if (this.spaces[wins[i][0]] == "X" && this.spaces[wins[i][1]] == "X" && this.spaces[wins[i][2]] == "X") {
				this.won = 2;
				this.firstSpot = this.wins[i][0];
				this.secondSpot = this.wins[i][2];
			} else if (this.spaces[wins[i][0]] == "O" && this.spaces[wins[i][1]] == "O"
					&& this.spaces[wins[i][2]] == "O") {
				this.won = 1;
				this.firstSpot = this.wins[i][0];
				this.secondSpot = this.wins[i][2];
			}
			i++;
		}

		if (this.won != 2 && this.won != 1) {
			this.won = 0;
			for (String s : this.spaces)
				if (s == null)
					this.won = -1;
		}
	}

	private boolean connect() {
		boolean ret = true;
		try {
			this.socket = new Socket(this.ip, this.port);
			this.dos = new DataOutputStream(socket.getOutputStream());
			this.dis = new DataInputStream(socket.getInputStream());
			this.accepted = true;
		} catch (IOException e) {
			System.out
					.println("Unable to connect to the address: " + this.ip + ":" + this.port + " | Starting a server");
			ret = false;
		}
		System.out.println("Successfully connected to the server !");
		return ret;
	}

	private void initializeServer() {
		try {
			this.serverSocket = new ServerSocket(this.port, 2, InetAddress.getByName(this.ip));
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.myTurn = true;
	}

	public void loadImages() {
		try {
			this.board = ImageIO.read(getClass().getResourceAsStream("/bg.png"));
			this.markX = ImageIO.read(getClass().getResourceAsStream("/X.png"));
			this.markO = ImageIO.read(getClass().getResourceAsStream("/O.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean getAccepted() {
		return this.accepted;
	}

	public boolean getMyTurn() {
		return this.myTurn;
	}

	public boolean getUnableToCommunicateWithOpponent() {
		return this.unableToCommunicateWithOpponent;
	}

	public boolean getEnd() {
		boolean ret = false;
		if (this.won != -1)
			ret = true;
		return ret;
	}

	public String[] getSpaces() {
		return this.spaces;
	}

	public void setPositionToX(int pos) {
		if (pos < 0 || pos > this.spaces.length)
			throw new IllegalArgumentException("TicTacToe: setPositionToX(int): \"pos\" is incorrect.");
		else {
			this.spaces[pos] = "X";
		}
	}

	public void setMyTurn(boolean bool) {
		this.myTurn = bool;
	}

	public DataOutputStream getDOS() {
		return this.dos;
	}

	public void incErrors() {
		this.errors++;
	}
}
