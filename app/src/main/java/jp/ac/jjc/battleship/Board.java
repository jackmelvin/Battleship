package jp.ac.jjc.battleship;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Random;

;
@Parcel
public class Board { //implements Parcelable {
    private static final int NUMOFSHIP = 10;
    int size;
    int numOfShipsSunk = 0;
    Cell[][] cells;
    Ship[] ships = {
            new Ship(4, 0),
            new Ship(3, 1), new Ship(3, 2),
            new Ship(2, 3), new Ship(2, 4), new Ship(2, 5),
            new Ship(1, 6), new Ship(1, 7), new Ship(1, 8), new Ship(1, 9)
    };
    //Empty constructor needed by the Parceler library
    public Board() {
    }
    public Board(int size) {
        this.size = size;
        cells = new Cell[size][size];
        for(int x = 0; x < size; x++) {
            for(int y = 0; y < size; y++) {
                cells[x][y] = new Cell(x, y);
            }
        }
    }

    public void incrNumOfShipsSunk() {
        numOfShipsSunk++;
    }

    public boolean areAllShipsSunk() {
        if(numOfShipsSunk == NUMOFSHIP) {
            return true;
        } else {
            return false;
        }
    }
    public int getSize() {
        return size;
    }

    public Cell[][] getCells() {
        return cells;
    }

    public Ship[] getShips() {
        return ships;
    }
    public boolean isOutOfBounds(int x, int y) {
        if(x < 0 || x >= size || y < 0 || y >= size) {
            return true;
        }
        return false;
    }

//    public boolean canPlaceShip(Ship ship, Cell startCell) {
//        int x = startCell.getCoord()[0];
//        int y = startCell.getCoord()[1];
//        if(!ship.getDir()) {
//            for(int i = 0; i < ship.getSize(); i++) {
//                if(isOutOfBounds(x+i, y)) {
//                    return false;
//                }
//                if(cells[x+i][y].isAroundShip()) {
//                    return false;
//                }
//                if(cells[x+i][y].hasShip()) {
//                    if(cells[x+i][y].getShip() != ship) {
//                        return false;
//                    }
//                }
//            }
//        } else {
//            for(int i = 0; i < ship.getSize(); i++) {
//                if(isOutOfBounds(x, y+i)) {
//                    return false;
//                }
//                if(cells[x][y+i].isAroundShip()) {
//                    return false;
//                }
//                if(cells[x][y+i].hasShip()) {
//                    if(cells[x][y+i].getShip() != ship) {
//                        return false;
//                    }
//                }
//            }
//        }
//        return true;
//    }

    public boolean placeShip(Ship ship, Cell startCell) {
        int x = startCell.getCoord()[0];
        int y = startCell.getCoord()[1];
        ArrayList<Cell> cellsToPlace = new ArrayList<Cell>();
        int imgBaseId = ship.getImgBaseId();
        if(!ship.getDir()) {
            for(int i = 0; i < ship.getSize(); i++) {
                if(isOutOfBounds(x+i, y)) {
                    return false;
                }
                if(cells[x+i][y].isAroundShip()) {
                    return false;
                }
                if(cells[x+i][y].hasShip()) {
                    if(cells[x+i][y].getShip() != ship) {
                        return false;
                    }
                }
                cellsToPlace.add(cells[x+i][y]);
            }
        } else {
            for(int i = 0; i < ship.getSize(); i++) {
                if(isOutOfBounds(x, y+i)) {
                    return false;
                }
                if(cells[x][y+i].isAroundShip()) {
                    return false;
                }
                if(cells[x][y+i].hasShip()) {
                    if(cells[x][y+i].getShip() != ship) {
                        return false;
                    }
                }
                cellsToPlace.add(cells[x][y+i]);
            }
        }
        for(int i = 0; i < ship.getSize(); i++) {
            cellsToPlace.get(i).setShip(ship, (imgBaseId + i));
        }
        ship.placeShip(cellsToPlace);

        //
        int endCellX = cellsToPlace.get(cellsToPlace.size() - 1).getCoord()[0];
        int endCellY = cellsToPlace.get(cellsToPlace.size() - 1).getCoord()[1];
        ArrayList<Cell> arroundCells = new ArrayList<Cell>();
        for(int i = x-1; i <= endCellX+1; i++) {
            for(int j = y-1; j <= endCellY+1; j++) {
                if(isOutOfBounds(i, j)) {
                    continue;
                }
                if(cellsToPlace.indexOf(cells[i][j]) != -1) {
                    continue;
                }
                arroundCells.add(cells[i][j]);
                cells[i][j].shipsArround.add(ship);
            }
        }
        ship.setSurroundCells(arroundCells);

        //Succeeded to place a ship
        return true;
    }

    public void placeShipRandomly() {
        for(Ship shipToPlace : ships) {
            if(shipToPlace.isPlaced()) {
                shipToPlace.removeShip();
            }
            Random rand = new Random();
            int randX = rand.nextInt(10);
            int randY = rand.nextInt(10);
            shipToPlace.setDir(rand.nextBoolean());
            Cell startCell = new Cell(randX, randY);
            while(!placeShip(shipToPlace, startCell)) {
                randX = (int) (Math.random() * 10);
                randY = (int) (Math.random() * 10);
                shipToPlace.setDir(rand.nextBoolean());
                startCell.setCoord(randX, randY);
            }
        }
    }



}
