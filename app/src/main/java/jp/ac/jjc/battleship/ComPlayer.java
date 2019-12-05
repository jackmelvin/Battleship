package jp.ac.jjc.battleship;

import android.os.Handler;

import java.util.ArrayList;
import java.util.Random;

class ComPlayer extends Player {
    private Cell firstHit = null;
    private Cell nextHit = null;
    private ComPlayer com = this;
    private ArrayList<Integer> secondShootDir;
    private ShootResult lastShootResult = null;

    ComPlayer(Board board, GamePlayActivity.GamePlay game) {
        super(board, game);
        board.placeShipRandomly();

    }
    void randomlyShoot(final Board userBoard) {
        game.disableBoard(board);
        //Add a delay
        int delay;
        if(lastShootResult == ShootResult.HIT) {
            delay = 1000;
        } else {
            delay = 1500 + (int)(Math.random() * 500);
        }

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Random rand = new Random();
                boolean shootSuccess = false;
                while(!shootSuccess) {
                    int x = 0, y = 0;

                    if (firstHit == null) { //Not getting any hit yet
                        x = rand.nextInt(10);
                        y = rand.nextInt(10);
                        if(userBoard.getCells()[x][y].isHit()) {
                            continue;
                        }
                    } else if (nextHit == null) { //Second shoot after getting a cell hit
                        boolean success = false;
                        while(!success) {
                            int randNum = rand.nextInt(secondShootDir.size());
                            int randDir = secondShootDir.get(randNum);
                            int firstX = firstHit.getCoord()[0];
                            int firstY = firstHit.getCoord()[1];
                            switch(randDir) {
                                case 0: //Shoot the cell to the left of firstHit
                                    if(!userBoard.isOutOfBounds(firstX, firstY-1) && !userBoard.getCells()[firstX][firstY-1].isHit()) {
                                        x = firstX;
                                        y = firstY-1;
                                        break;
                                    } else {
                                        secondShootDir.remove(Integer.valueOf(randDir));
                                        continue;
                                    }
                                case 1: //Shoot the cell to the right of firstHit
                                    if(!userBoard.isOutOfBounds(firstX, firstY+1) && !userBoard.getCells()[firstX][firstY+1].isHit()) {
                                        x = firstX;
                                        y = firstY+1;
                                        break;
                                    } else {
                                        secondShootDir.remove(Integer.valueOf(randDir));
                                        continue;
                                    }
                                case 2: //Shoot the cell above of firstHit
                                    if(!userBoard.isOutOfBounds(firstX-1, firstY) && !userBoard.getCells()[firstX-1][firstY].isHit()) {
                                        x = firstX-1;
                                        y = firstY;
                                        break;
                                    } else {
                                        secondShootDir.remove(Integer.valueOf(randDir));
                                        continue;
                                    }
                                case 3: //Shoot the cell below firstHit
                                    if(!userBoard.isOutOfBounds(firstX+1, firstY) && !userBoard.getCells()[firstX+1][firstY].isHit()) {
                                        x = firstX+1;
                                        y = firstY;
                                        break;
                                    } else {
                                        secondShootDir.remove(Integer.valueOf(randDir));
                                        continue;
                                    }
                                default:
                                    continue;
                            }
                            success = true;
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
                    ShootResult result = shoot(userBoard.getCells()[x][y]);
                    lastShootResult = result;
                    if(result == ShootResult.MISS) { //Case Miss
                        //To user turn
                        //Enable Com Board to take shoot
                        game.enableBoard(board);
                        //Change arrow direction
                        game.changeArrowDir();
                    } else if (result == ShootResult.END) { //Case End
                        //EndGame
                        game.endGame(com);
                    } else if (result == ShootResult.KILL) {//Case Kill
                        //Got a kill, shoot randomly
                        firstHit = null;
                        nextHit = null;
                        randomlyShoot(userBoard);
                    } else { //Case Hit
                        //Set condition for next shoot
                        if(firstHit == null) {
                            firstHit = userBoard.getCells()[x][y];
                            secondShootDir = new ArrayList<>();
                            secondShootDir.add(0);
                            secondShootDir.add(1);
                            secondShootDir.add(2);
                            secondShootDir.add(3);
                        } else {
                            nextHit = userBoard.getCells()[x][y];
                        }
                        randomlyShoot(userBoard);
                    }
                    shootSuccess = true;
                }
            }
        }, delay);



    }

}
