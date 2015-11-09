import acm.program.*;
import acm.graphics.*;

import java.awt.Color;
import java.awt.event.*;
import acm.util.*;
import java.io.*;

public class Snake extends GraphicsProgram implements SnakeConstants{


	public static void main(String[] args) {
		new Snake().start(args);
	}

	public void run(){
		addKeyListeners();
		addMouseListeners();
		while(true){
			initWorld();
			playGame();
			prepToPlayAgain();
		}

	}

	private void initWorld(){
		removeAll();
		drawGrid();
		drawBorder();
		initSnake();
		placeNewFood();
		initScore();
		initHighscore();
		playAgain = new GLabel("Click anywhere to start");
		playAgain.setFont("Cambria-" + 20);
		add(playAgain, APPWIDTH/2 - playAgain.getWidth()/2, playAgain.getAscent() + APPHEIGHT/NCOLUMNS);
		takeInKeyCommands = false;
		waitForClick();
	}

	private void playGame(){
		takeInKeyCommands = true;
		remove(playAgain);
		while(!(hitItself() || hitTheEdge()) ){
			moveInDirection();
			for(int i = snakeLength - 1; i >= 0; i--){
				if(snakeX[i] == foodX && snakeY[i] == foodY){
					removeFood();
				}
			}
			pause(DELAY);
		}
		grid[snakeX[snakeLength -1]][snakeY[snakeLength -1]].setFillColor(Color.GRAY);
		grid[snakeX[snakeLength -1]][snakeY[snakeLength -1]].setColor(Color.GRAY);

		if(snakeLength > getHighScore()){
			updateHighScorecard();
		}
	}

	private void prepToPlayAgain(){
		playAgain = new GLabel("Click anywhere to play again");
		playAgain.setFont("Cambria-" + 20);
		add(playAgain, APPWIDTH/2 - playAgain.getWidth()/2, playAgain.getAscent() + APPHEIGHT/NCOLUMNS);
		waitForClick();
		resetIvars();
	}

	private void resetIvars(){
		snakeLength = SNAKEINIT;
		currentDirection = UP;
		grid = new GRect[NROWS][NCOLUMNS];
		snakeX = new int[APPWIDTH*APPHEIGHT];
		snakeY = new int[APPWIDTH*APPHEIGHT];
		multiplier = 0;
		addState = false;
		stateCounter = 0;
	}

	private void drawBorder(){
		for(int i = 0; i < NROWS; i++){
			grid[0][i].setFillColor(Color.BLACK);
			grid[0][i].setColor(Color.BLACK);
		}
		for(int i = 0; i < NCOLUMNS; i++){
			grid[i][0].setFillColor(Color.BLACK);
			grid[i][0].setColor(Color.BLACK);
		}
		for(int i = 0; i < NROWS; i++){
			grid[NROWS - 1][i].setFillColor(Color.BLACK);
			grid[NROWS - 1][i].setColor(Color.BLACK);
		}
		for(int i = 0; i < NCOLUMNS; i++){
			grid[i][NCOLUMNS -1].setFillColor(Color.BLACK);
			grid[i][NCOLUMNS -1].setColor(Color.BLACK);
		}
	}

	private void initScore(){
		scorecard = new GLabel("Score: " + SNAKEINIT);
		scorecard.setFont("Cambria-" + 20);
		add(scorecard, APPWIDTH - scorecard.getWidth() - APPWIDTH/NCOLUMNS,
				scorecard.getAscent() + APPWIDTH/NCOLUMNS);
	}

	private void updateScorecard(){
		remove(scorecard);
		scorecard = new GLabel("Score: " + snakeLength);
		scorecard.setFont("Cambria-" + 20);
		add(scorecard, APPWIDTH - scorecard.getWidth() - APPWIDTH/NCOLUMNS,
				scorecard.getAscent() + APPWIDTH/NCOLUMNS);
	}

	private int getHighScore(){
		try{
			BufferedReader rd = new BufferedReader(new FileReader(FILE));
			String line = rd.readLine();
			rd.close();
			return Integer.parseInt(line);
		} catch (IOException ex) { 
			throw new ErrorException(ex);
		}
	}

	private void initHighscore(){
		highScorecard = new GLabel("High Score: " + getHighScore());
		highScorecard.setFont("Cambria-" + 20);
		add(highScorecard, APPWIDTH - highScorecard.getWidth() - APPWIDTH/NCOLUMNS, 
				scorecard.getAscent() + highScorecard.getAscent() + APPWIDTH/NCOLUMNS);
	}


	private void updateHighScorecard(){
		try{
			PrintWriter wr = new PrintWriter(new FileWriter(FILE));
			wr.println(snakeLength);
			wr.close();
		} catch (IOException ex) { 
			throw new ErrorException(ex);
		}

		remove(highScorecard);
		initHighscore();
	}

	private void initSnake(){
		for(int i = 0; i < snakeLength; i++){
			snakeX[i] = NROWS/2;
			snakeY[i] = NCOLUMNS/2 - i;
		}
		drawSnake();
	}


	private void drawSnake(){
		takeInKeyCommands = false;
		for(int i = 0; i < snakeLength - 1; i++){
			grid[snakeX[i]][snakeY[i]].setFillColor(Color.BLUE);
			grid[snakeX[i]][snakeY[i]].setColor(Color.BLUE);
		}
		grid[snakeX[snakeLength - 1]][snakeY[snakeLength - 1]].setFillColor(Color.BLACK);
		grid[snakeX[snakeLength - 1]][snakeY[snakeLength - 1]].setColor(Color.BLACK);
		takeInKeyCommands = true;
	}

	private void removeSnake(){
		takeInKeyCommands = false;
		for(int i = 0; i < snakeLength; i++){
			grid[snakeX[i]][snakeY[i]].setFillColor(Color.WHITE);
			grid[snakeX[i]][snakeY[i]].setColor(Color.WHITE);
		}
		takeInKeyCommands = true;
	}


	private void moveSnakeRight(){
		removeSnake();
		if(addState){
			snakeX[snakeLength] = snakeX[snakeLength -1] + 1;
			snakeY[snakeLength] = snakeY[snakeLength -1];
			snakeLength++;
			updateScorecard();
			stateCounter++;
			if(stateCounter == INCREMENT*multiplier){
				addState = false;
				stateCounter = 0;
				multiplier = 0;
			}

		} else {
			for(int i = 1; i < snakeLength; i++){
				snakeX[i - 1] = snakeX[i];
				snakeY[i - 1] = snakeY[i];
			}
			snakeX[snakeLength -1] += 1;
		}
		drawSnake();
		currentDirection = RIGHT;

	}

	private void moveSnakeLeft(){
		removeSnake();
		if(addState){
			snakeX[snakeLength] = snakeX[snakeLength -1] - 1;
			snakeY[snakeLength] = snakeY[snakeLength -1];
			snakeLength++;
			updateScorecard();
			stateCounter++;
			if(stateCounter == INCREMENT*multiplier){
				addState = false;
				stateCounter = 0;
				multiplier = 0;
			}
		} else {
			for(int i = 1; i < snakeLength; i++){
				snakeX[i - 1] = snakeX[i];
				snakeY[i - 1] = snakeY[i];
			}
			snakeX[snakeLength -1] -= 1;
		}
		drawSnake();
		currentDirection = LEFT;

	}

	private void moveSnakeDown(){
		removeSnake();
		if(addState){
			snakeX[snakeLength] = snakeX[snakeLength -1];
			snakeY[snakeLength] = snakeY[snakeLength -1] + 1;
			snakeLength++;
			updateScorecard();
			stateCounter++;
			if(stateCounter == INCREMENT*multiplier){
				addState = false;
				stateCounter = 0;
				multiplier = 0;
			}
		} else {
			for(int i = 1; i < snakeLength; i++){
				snakeX[i - 1] = snakeX[i];
				snakeY[i - 1] = snakeY[i];
			}
			snakeY[snakeLength -1] += 1;
		}
		drawSnake();
		currentDirection = DOWN;

	}

	private void moveSnakeUp(){
		removeSnake();
		if(addState){
			snakeX[snakeLength] = snakeX[snakeLength -1];
			snakeY[snakeLength] = snakeY[snakeLength -1] - 1;
			snakeLength++;
			updateScorecard();
			stateCounter++;
			if(stateCounter == INCREMENT*multiplier){
				addState = false;
				stateCounter = 0;
				multiplier = 0;
			}
		} else {
			for(int i = 1; i < snakeLength; i++){
				snakeX[i - 1] = snakeX[i];
				snakeY[i - 1] = snakeY[i];
			}
			snakeY[snakeLength -1] -= 1;
		}
		drawSnake();
		currentDirection = UP;

	}


	private void drawGrid(){
		for(int i = 0; i < APPWIDTH; i += APPWIDTH/NCOLUMNS){
			for(int j = 0; j<APPHEIGHT; j += APPHEIGHT/NROWS){
				grid[(i*NCOLUMNS)/APPWIDTH][(j*NROWS)/APPHEIGHT] =  pixelBox(NCOLUMNS, NROWS);
				add(grid[i*NCOLUMNS/APPWIDTH][j*NROWS/APPHEIGHT] , i, j);	
			}
		}
	}

	private GRect pixelBox(int x, int y){
		GRect pix = new GRect(APPWIDTH/x, APPHEIGHT/y);
		pix.setFilled(true);
		pix.setFillColor(Color.WHITE);
		pix.setColor(Color.WHITE);
		return pix;
	}


	public void keyPressed(KeyEvent e){
		if(!(hitItself() || hitTheEdge()) && takeInKeyCommands ){
			switch(e.getKeyCode()){
			case KeyEvent.VK_UP:
				if(currentDirection != DOWN){
					moveSnakeUp();
				}
				break;

			case KeyEvent.VK_DOWN: 
				if(currentDirection != UP){
					moveSnakeDown();
				}
				break;

			case KeyEvent.VK_LEFT: 
				if(currentDirection != RIGHT){
					moveSnakeLeft();
				}
				break;

			case KeyEvent.VK_RIGHT: 
				if(currentDirection != LEFT){
					moveSnakeRight();
				}
				break;

			}	
		}

	}

	private void moveInDirection(){
		switch(currentDirection){
		case RIGHT:
			moveSnakeRight();
			break;
		case LEFT:
			moveSnakeLeft();
			break;
		case UP:
			moveSnakeUp();
			break;
		case DOWN:
			moveSnakeDown();
			break;
		}
	}

	private void placeNewFood(){
		setFood();
		grid[foodX][foodY].setFillColor(Color.RED);
		grid[foodX][foodY].setColor(Color.RED);
	}

	private void removeFood(){
		grid[foodX][foodY].setFillColor(Color.BLACK);
		grid[foodX][foodY].setColor(Color.BLACK);
		placeNewFood();
		addState = true;
		multiplier++;
	}

	private void setFood(){
		foodX = rgen.nextInt(1, NCOLUMNS - 2);
		foodY = rgen.nextInt(1, NROWS - 2);
		for(int i = 0; i < snakeLength; i++){
			if(snakeX[i] == foodX && snakeY[i] == foodY){
				setFood();
			}
		}
	}

	private boolean hitTheEdge(){
		if(snakeX[snakeLength -1] == 0 || snakeY[snakeLength -1] == 0 || 
				snakeX[snakeLength -1] == NCOLUMNS -1 || snakeY[snakeLength -1] == NCOLUMNS -1   ) {
			return true;
		}
		return false;
	}

	private boolean hitItself(){
		for(int i = 0; i < snakeLength -1; i++){
			if(snakeX[i] == snakeX[snakeLength -1] && snakeY[i] == snakeY[snakeLength -1]){
				return true;
			}
		}
		return false;
	}


	//ivars

	private RandomGenerator rgen = RandomGenerator.getInstance();

	private GRect[][] grid = new GRect[NCOLUMNS][NROWS];
	private int[] snakeX = new int[APPWIDTH*APPHEIGHT];
	private int[] snakeY = new int[APPWIDTH*APPHEIGHT];

	private int snakeLength = SNAKEINIT;

	private int currentDirection = UP;

	private int foodX;
	private int foodY;

	private boolean addState = false;
	private int stateCounter = 0;

	private GLabel scorecard;

	private GLabel highScorecard;
	private GLabel playAgain;

	private boolean takeInKeyCommands = true;

	private boolean pauseState = true;

	private int multiplier = 0; // in case two foods are eaten in quick succession



}

