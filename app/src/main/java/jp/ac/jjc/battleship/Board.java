package jp.ac.jjc.battleship;

import java.util.ArrayList;
import java.util.Random;

class Board {
    //Board width and height
    private final int SIZE;
    private final int NUMBER_OF_SHIPS;
    private Cell[][] cells;
    private Ship[] ships;
    private int numOfShipsSunk = 0;

    Board (int size, int numberOfShips) {
        this.SIZE = size;
        this.NUMBER_OF_SHIPS = numberOfShips;

        cells = new Cell[size][size];
        for(int x = 0; x < size; x++) {
            for(int y = 0; y < size; y++) {
                cells[x][y] = new Cell(x, y);
            }
        }

        ships = new Ship[numberOfShips];
        for(int i = 0; i < numberOfShips; i++) {
            int shipSize;
            if(i < 4) {
                shipSize = 1;
            } else if (i < 7) {
                shipSize = 2;
            } else if (i < 9) {
                shipSize = 3;
            } else {
                shipSize = 4;
            }
            ships[i] = new Ship(shipSize, i);
        }
    }

    int getSize() {
        return SIZE;
    }

    Cell getCell(int x, int y) {
        return cells[x][y];
    }

    Ship getShip(int index) {
        return ships[index];
    }

    boolean isOutOfBounds(int x, int y) {
        return (x < 0 || x >= SIZE || y < 0 || y >= SIZE);
    }

    boolean areAllShipsPlaced() {
        for(Ship ship : ships) {
            if(!ship.isPlaced()) {
                return false;
            }
        }
        return true;
    }

    void incrNumOfShipsSunk() {
        numOfShipsSunk++;
    }

    boolean areAllShipsSunk() {
        return (numOfShipsSunk == NUMBER_OF_SHIPS);
    }

    boolean placeShip (Ship ship, Cell headCell) {
        if(ship == null || headCell == null) return false;
        ArrayList<Cell> cellsToPlace = new ArrayList<>();
        ArrayList<Cell> surroundingCells = new ArrayList<>();
        int x = 0, y = 0;
        //Determining cells to place
        //If the cell already has ship placed on
        if(ship.isHorizontal()) {
            //Place ship horizontally
            y = headCell.getY();
            for(int i = 0; i < ship.getSize(); i++) {
                x = headCell.getX() + i;
                //If the ship is out of the board
                System.out.println("Can't place ship. Out of bounds");
                if(isOutOfBounds(x, y)) return false;
                if(cells[x][y].hasShip()) {
                    System.out.println("Can't place ship. Cell already has ship.");
                    return false;
                }
                //If the cell is next to another ship
                if(cells[x][y].nearAShipOtherThan(ship)) {
                    System.out.println("Can't place ship. Cell is near another ship.");
                    return false;
                }

                cellsToPlace.add(cells[x][y]);
            }
        } else {
            //Place ship vertically
            x = headCell.getX();
            for(int i = 0; i < ship.getSize(); i++) {
                y = headCell.getY() + i;
                //If the ship is out of the board
                System.out.println("Can't place ship. Out of bounds");
                if(isOutOfBounds(x, y)) return false;
                if(cells[x][y].hasShip()) {
                    System.out.println("Can't place ship. Cell already has ship.");
                    return false;
                }
                //If the cell is next to another ship
                if(cells[x][y].nearAShipOtherThan(ship)) {
                    System.out.println("Can't place ship. Cell is near another ship.");
                    return false;
                }

                cellsToPlace.add(cells[x][y]);
            }
        }

        //Determining surrounding cells
        int headX = headCell.getX() - 1;
        int headY = headCell.getY() - 1;
        int lastX = cellsToPlace.get(cellsToPlace.size() - 1).getX() + 1;
        int lastY = cellsToPlace.get(cellsToPlace.size() - 1).getY() + 1;
        for(x = headX; x <= lastX; x++) {
            for(y = headY; y <= lastY; y++) {
                //if the cell is out of the board, skip
                if(isOutOfBounds(x, y)) continue;
                if(cellsToPlace.contains(cells[x][y])) continue;

                surroundingCells.add(cells[x][y]);
            }
        }
        //Place ship
        ship.place(cellsToPlace, surroundingCells);
        return true;
    }

    boolean rotateShip(Ship ship) {
        Cell headCell = ship.rotate();
        if(placeShip(ship, headCell)) {
            return true;
        } else {
            ship.rotate();
            placeShip(ship, headCell);
            return false;
        }
    }

    void placeShipRandomly() {
        for(Ship ship : ships) {
            Cell startCell;
            boolean success = false;
            while(!success) {
                Random rand = new Random();
                int randX = rand.nextInt(SIZE);
                int randY = rand.nextInt(SIZE);
                startCell = cells[randX][randY];
                if(rand.nextBoolean()) {
                    ship.rotate();
                }
                success = placeShip(ship, startCell);
            }
        }
    }
}
