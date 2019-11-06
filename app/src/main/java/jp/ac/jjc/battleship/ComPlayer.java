package jp.ac.jjc.battleship;

import android.os.Handler;
import android.widget.ImageButton;

import java.util.Random;

enum Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT
}

class ComPlayer extends Player {
    private ImageButton[][] ibBoard;
    private GamePlayActivity.Game game;
    private Cell firstHit = null;
    private Cell nextHit = null;

    public ComPlayer(Board board, ImageButton[][] ibBoard, GamePlayActivity.Game game) {
        super(board);
        this.ibBoard = ibBoard;
        this.game = game;
        board.placeShipRandomly();

    }
    public void randomlyShoot(final Board userBoard, final ImageButton[][] ibUserBoard) {
        final Player com = this;
        //Add a 1 second delay
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Random rand = new Random();
                boolean success = false;
                while(!success) {
                    int x = 0, y = 0;

                    if (firstHit == null) { //Not getting any hit yet
                        x = rand.nextInt(10);
                        y = rand.nextInt(10);
                        if(userBoard.getCells()[x][y].isHit()) {
                            continue;
                        }
                    } else if (nextHit == null) { //Second shoot after getting a cell hit
                        int randNum = rand.nextInt(4);
                        int firstX = firstHit.getCoord()[0];
                        int firstY = firstHit.getCoord()[1];
                        switch(randNum) {
                            case 0: //Shoot the cell to the left of firstHit
                                if(!userBoard.isOutOfBounds(firstX, firstY-1) && !userBoard.getCells()[firstX][firstY-1].isHit()) {
                                    x = firstX;
                                    y = firstY-1;
                                    break;
                                }
                            case 1: //Shoot the cell to the right of firstHit
                                if(!userBoard.isOutOfBounds(firstX, firstY+1) && !userBoard.getCells()[firstX][firstY+1].isHit()) {
                                    x = firstX;
                                    y = firstY+1;
                                    break;
                                }
                            case 2: //Shoot the cell above of firstHit
                                if(!userBoard.isOutOfBounds(firstX-1, firstY) && !userBoard.getCells()[firstX-1][firstY].isHit()) {
                                    x = firstX-1;
                                    y = firstY;
                                    break;
                                }
                            case 3: //Shoot the cell below firstHit
                                if(!userBoard.isOutOfBounds(firstX+1, firstY) && !userBoard.getCells()[firstX+1][firstY].isHit()) {
                                    x = firstX+1;
                                    y = firstY;
                                    break;
                                }
                        }
                    } else { //Shoot the cell next to nextHit
                        int firstX = firstHit.getCoord()[0];
                        int firstY = firstHit.getCoord()[1];
                        int nextX = nextHit.getCoord()[0];
                        int nextY = nextHit.getCoord()[1];
                        if(nextY == firstY) { //Shooting vertically
                            if(nextX > firstX) { //Shooting down
                                if(!userBoard.isOutOfBounds(nextX+1, nextY) && !userBoard.getCells()[nextX+1][nextY].isHit()) {
                                    x = nextX+1;
                                } else {
                                    x = firstX-1;
                                }
                                y = nextY;
                            } else { //Shooting up
                                if (!userBoard.isOutOfBounds(nextX-1, nextY) && !userBoard.getCells()[nextX-1][nextY].isHit()) {
                                    x = nextX-1;
                                } else {
                                    x = firstX+1;
                                }
                                y = nextY;
                            }
                        }
                        if(nextX == firstX) { //Shooting horizontally
                            if(nextY > firstY) { //Shooting right
                                x = nextX;
                                if(!userBoard.isOutOfBounds(nextX, nextY+1) && !userBoard.getCells()[nextX][nextY+1].isHit()) {
                                    y = nextY+1;
                                } else {
                                    y = firstY-1;
                                }
                            } else { //Shooting left
                                x = nextX;
                                if(!userBoard.isOutOfBounds(nextX, nextY-1) && !userBoard.getCells()[nextX][nextY-1].isHit()) {
                                    y = nextY-1;
                                } else {
                                    y = firstY+1;
                                }
                            }
                        }
                    }
                    System.out.println("COM shot: " + x + "," + y);
                    ShootResult result = shoot(userBoard.getCells()[x][y]);
                    ibUserBoard[x][y].setBackgroundResource(userBoard.getCells()[x][y].getImgBaseId());
                    if(result == ShootResult.MISS) { //Case Miss
                        //Update miss cell img
                        updateCellImg(userBoard.getCells()[x][y], ibUserBoard);
                        //To user turn
                        //Enable Com Board to take shoot
                        enableBoard(ibBoard);
                        //Change arrow direction
                        game.changeArrowDir();
                    } else {
                        if (result == ShootResult.KILL || result == ShootResult.END) { //Case Kill or End
                            //Update ship placed cells and surround cells img
                            updateShipImg(userBoard.getCells()[x][y].getShip(), ibUserBoard);
                            if (result == ShootResult.END) {
                                //EndGame
                                game.endGame(com);
                            }
                            //Got a kill, shoot randomly
                            firstHit = null;
                            nextHit = null;
                            randomlyShoot(userBoard, ibUserBoard);
                        } else { //Case Hit
                            //Update hit cell img
                            updateCellImg(userBoard.getCells()[x][y], ibUserBoard);
                            //Set condition for next shoot
                            if(firstHit == null) {
                                firstHit = userBoard.getCells()[x][y];
                            } else {
                                nextHit = userBoard.getCells()[x][y];
                            }
                            randomlyShoot(userBoard, ibUserBoard);
                        }
                    }
                    success = true;
                }
            }
        }, 1000);



    }

    public void updateCellImg(Cell cell, ImageButton[][] ibUserBoard) {
        int x = cell.getCoord()[0];
        int y = cell.getCoord()[1];
        ibUserBoard[x][y].setBackgroundResource(cell.getImgBaseId());
    }

    public void updateShipImg(Ship ship, ImageButton[][] ibUserBoard) {
        //Update ship placed cells
        for(Cell cell : ship.getPlacedCells()) {
            updateCellImg(cell, ibUserBoard);
        }
        //Update surround cells
        for(Cell cell : ship.getSurroundCells()) {
            updateCellImg(cell, ibUserBoard);
        }
    }

    public void enableBoard(ImageButton[][] ibBoard) {
        for(int x = 0; x < ibBoard.length; x++) {
            for(int y = 0; y < ibBoard[0].length; y++) {
                if(!board.getCells()[x][y].isHit) {
                    ibBoard[x][y].setClickable(true);
                }
            }
        }
    }

    public void disableBoard(ImageButton[][] ibBoard) {
        for(int x = 0; x < ibBoard.length; x++) {
            for(int y = 0; y < ibBoard[0].length; y++) {
                ibBoard[x][y].setClickable(false);
            }
        }
    }


}
