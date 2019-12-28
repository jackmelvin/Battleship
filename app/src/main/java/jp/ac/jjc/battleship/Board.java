package jp.ac.jjc.battleship;

import java.util.ArrayList;
import java.util.Random;

class Board {
    private static final int NUMOFSHIP = 10;
    private int size;
    private int numOfShipsSunk = 0;
    private Cell[][] cells;
    private ArrayList<Ship> ships;

    Board(int size, ArrayList<Ship> ships) {
        this.size = size;
        this.cells = new Cell[size][size];
        for(int x = 0; x < size; x++) {
            for(int y = 0; y < size; y++) {
                cells[x][y] = new Cell(x, y);
            }
        }
        this.ships = ships;
    }

    int getSize() {
        return size;
    }

    void incrNumOfShipsSunk() {
        numOfShipsSunk++;
    }

    boolean areAllShipsSunk() {
        return (numOfShipsSunk == NUMOFSHIP);
    }

    Cell[][] getCells() {
        return cells;
    }

    ArrayList<Ship> getShips() {
        return ships;
    }
    boolean isOutOfBounds(int x, int y) {
        return (x < 0 || x >= size || y < 0 || y >= size);
    }

    boolean canPlaceShip(Ship ship, Cell startCell) {
        int x = startCell.getX();
        int y = startCell.getY();
        //Hold cells where the ship is placed on
        if(ship.getDir()) { //Placing vertical ship
            for(int i = 0; i < ship.getSize(); i++) {
                //If the cell is out of the board
                if(isOutOfBounds(x+i, y)) {
                    System.out.println("Can't place ship. Out of bounds.");
                    return false;
                }
                //If the cell is already placed with another ship
                if(cells[x+i][y].hasShip() && cells[x+i][y].getShip() != ship) {
                    System.out.println("Can't place ship. Already has ship.");
                    return false;
                }
                //If the cell is near a ship
                ArrayList<Ship> shipsSurround = cells[x+i][y].getShipsSurround();
                if(shipsSurround.size() > 1 || (shipsSurround.size() == 1 && shipsSurround.get(0) != ship)) {
                    System.out.println("Can't place ship. Near another ship.");
                    return false;
                }
            }
        } else {    //Placing horizontal ship
            for(int i = 0; i < ship.getSize(); i++) {
                if(isOutOfBounds(x, y+i)) {
                    System.out.println("Can't place ship. Out of bounds.");
                    return false;
                }
                if(cells[x][y+i].hasShip() && cells[x][y+i].getShip() != ship) {
                    System.out.println("Can't place ship. Already has ship.");
                    return false;
                }
                ArrayList<Ship> shipsSurround = cells[x][y+i].getShipsSurround();
                if(shipsSurround.size() > 1 || (shipsSurround.size() == 1 && shipsSurround.get(0) != ship)) {
                    System.out.println("Can't place ship. Near another ship.");
                    return false;
                }
            }
        }
        return true;
    }

    void placeShip(Ship ship, Cell startCell) {
        int x = startCell.getX();
        int y = startCell.getY();
        //Hold cells where the ship is placed on
        ArrayList<Cell> cellsToPlace = new ArrayList<Cell>();
        if(ship.getDir()) { //Placing vertical ship
            for(int i = 0; i < ship.getSize(); i++) {
                cellsToPlace.add(cells[x+i][y]);
            }
        } else {    //Placing horizontal ship
            for(int i = 0; i < ship.getSize(); i++) {
                cellsToPlace.add(cells[x][y+i]);
            }
        }

        //Set surround cells of placed ship
        //Coordinates of the cell at the end of the ship
        int endCellX = cellsToPlace.get(cellsToPlace.size() - 1).getX();
        int endCellY = cellsToPlace.get(cellsToPlace.size() - 1).getY();
        //Hold cells to be set as surround
        ArrayList<Cell> surroundCells = new ArrayList<Cell>();
        for(int i = x-1; i <= endCellX+1; i++) {
            for(int j = y-1; j <= endCellY+1; j++) {
                //If the cell is out of the board, pass
                if(isOutOfBounds(i, j)) {
                    continue;
                }
                //If the cell is placed with ship, pass
                if(cellsToPlace.indexOf(cells[i][j]) != -1) {
                    continue;
                }
                //The cell is available to set as surround
                surroundCells.add(cells[i][j]);
            }
        }

        ship.placeShip(cellsToPlace, surroundCells);

        //Succeeded to place a ship
    }

    void placeShipRandomly() {
        int num = 0;
        for(int i = ships.size()-1; i >= 0; i--) {
            if(ships.get(i).isPlaced()) {
                ships.get(i).removeShip();
            }
            Cell startCell = null;
            boolean success = false;
            while(!success) {
                Random rand = new Random();
                int randX = rand.nextInt(10);
                int randY = rand.nextInt(10);
                ships.get(i).setDir(rand.nextBoolean());
                startCell = cells[randX][randY];
                success = canPlaceShip(ships.get(i), startCell);
            }
            placeShip(ships.get(i), startCell);
        }
    }



}
