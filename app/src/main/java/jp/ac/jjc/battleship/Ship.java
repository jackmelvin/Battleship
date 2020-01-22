package jp.ac.jjc.battleship;


import java.util.ArrayList;

class Ship {
    //Ship's length
    private int size;
    //Ship's id
    private int id;
    //Ship's rotation
    private boolean isHorizontal = true;
    //Ship's placed cells
    private ArrayList<Cell> placedCells;
    //Cells that the ship is surrounded by.
    //Two ships must not be placed right next to each other
    //There must be at least 1 cell between them
    private ArrayList<Cell> surroundingCells;
    private boolean isVisible = true;
    private boolean isPlaced = false;

    Ship(int size, int id) {
        this.size = size;
        this.id = id;
    }

    boolean isPlaced() {
        return isPlaced;
    }

    boolean isVisible() {
        return isVisible;
    }

    void setInvisible() {
        this.isVisible = false;
    }

    int getSize() {
        return size;
    }

    int getId() {
        return id;
    }

    int getImageId() {
        int imageId;
        switch (size) {
            case 1:
                imageId = R.drawable.ship_01_full_00;
                break;
            case 2:
                imageId = R.drawable.ship_02_full_00;
                break;
            case 3:
                imageId = R.drawable.ship_03_full_00;
                break;
            case 4:
                imageId = R.drawable.ship_04_full_00;
                break;
            default:
                //Error, invalid ship size
                imageId = 0;
                break;
        }
        //The ship images are named so that
        //Vertical ship image = horizontal ship image +2
        if(isHorizontal) {
            return imageId;
        } else {
            return imageId + 2;
        }
    }

    boolean isHorizontal() {
        return isHorizontal;
    }

    boolean isSunk() {
        for(Cell cell : placedCells) {
            if(!cell.isHit()) {
                return false;
            }
        }
        return true;
    }

    private int getHeadCellImageId() {
        int imageId;
        if(isHorizontal) {
            switch(size) {
                case 1:
                    imageId = R.drawable.ship_01_00;
                    break;
                case 2:
                    imageId = R.drawable.ship_02_00;
                    break;
                case 3:
                    imageId = R.drawable.ship_03_00;
                    break;
                case 4:
                    imageId = R.drawable.ship_04_00;
                    break;
                default:
                    //Error, unexpected ship size
                    imageId = -1;
            }
        } else {
            switch (size) {
                case 1:
                    imageId = R.drawable.ship_vertical_01_00;
                    break;
                case 2:
                    imageId = R.drawable.ship_vertical_02_00;
                    break;
                case 3:
                    imageId = R.drawable.ship_vertical_03_00;
                    break;
                case 4:
                    imageId = R.drawable.ship_vertical_04_00;
                    break;
                default:
                    //Error, unexpected ship size
                    imageId = -1;
            }
        }
        return imageId;
    }

    void place(ArrayList<Cell> cellsToPlace, ArrayList<Cell> surroundingCells) {
        if(isPlaced) {
            remove();
        }
        this.placedCells = cellsToPlace;
        this.surroundingCells = surroundingCells;
        //Set ship and imageId to the cells to place
        for(int i = 0; i < size; i++) {
            placedCells.get(i).placeShip(this, getHeadCellImageId() + i);
        }
        //Set surrounding cells
        for(Cell cell : surroundingCells) {
            cell.setSurroundingShip(this);
        }
        isPlaced = true;
    }

    private void remove() {
        if(!isPlaced) return;
        //Remove ship from cells placed on
        for(Cell cell : placedCells) {
            cell.removeShip(this);
        }
        //Remove ship from surrounding cells' list
        for(Cell cell : surroundingCells) {
            cell.removeShip(this);
        }
        isPlaced = false;
    }

    Cell rotate() {
        isHorizontal = !isHorizontal;
        Cell headCell = null;
        if(isPlaced) {
            headCell = placedCells.get(0);
            remove();
        }
        return headCell;
    }

    void sink() {
        //All cells are hit
        //Show the surroundingCell, make them unclickable
        for(Cell cell : surroundingCells) {
            cell.hit();
        }
        isVisible = true;
    }

    Cell getHeadCell() {
        return placedCells.get(0);
    }
}
