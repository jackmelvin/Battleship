package jp.ac.jjc.battleship;

import android.content.Context;
import android.util.AttributeSet;


import androidx.appcompat.widget.AppCompatImageButton;

import java.util.ArrayList;

public class Ship extends AppCompatImageButton {
    private int size;
    private boolean dir = true; //True == Horizontal
    private boolean isPlaced;
    private boolean isSelected;
    private boolean isVisible;
    private ArrayList<Cell> placedCells = new ArrayList<Cell>();
    private ArrayList<Cell> surroundCells = new ArrayList<Cell>();
    private int shipImgId;

    public Ship(Context context) {
        super(context);
    }

    public Ship(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

//    public Ship(Context context, int size) {
//        super(context);
//        this.size = size;
//    }

    void setShipImgId(int shipImgId) {
        this.shipImgId = shipImgId;
    }
//
//    int getShipImgId() {
//        return shipImgId;
//    }

    void setSize(int size) {
        this.size = size;
    }
    int getSize() {
        return size;
    }
    void setDir(boolean dir) {
        this.dir = dir;
    }
    boolean getDir() {
        return dir;
    }
    void rotate() {
        if(dir) {
            shipImgId += 2;
        } else {
            shipImgId -= 2;
        }
        dir = !dir;
        setImageResource(shipImgId);
    }

    boolean isVisible() {
        return isVisible;
    }

    void isVisible(boolean visibility) {
        isVisible = visibility;
    }

    int getImgBaseId() {
        int imgBaseId;
        if(dir) {
            switch (size) {
                case 1:
                    imgBaseId = R.drawable.ship_01_00;
                    break;
                case 2:
                    imgBaseId = R.drawable.ship_02_00;
                    break;
                case 3:
                    imgBaseId = R.drawable.ship_03_00;
                    break;
                case 4:
                    imgBaseId = R.drawable.ship_04_00;
                    break;
                default:
                    imgBaseId = -1;
            }
        } else {
            switch (size) {
                case 1:
                    imgBaseId = R.drawable.ship_vertical_01_00;
                    break;
                case 2:
                    imgBaseId = R.drawable.ship_vertical_02_00;
                    break;
                case 3:
                    imgBaseId = R.drawable.ship_vertical_03_00;
                    break;
                case 4:
                    imgBaseId = R.drawable.ship_vertical_04_00;
                    break;
                default:
                    imgBaseId = -1;
            }
        }
        return imgBaseId;
    }

    boolean isSunk() {
        for(Cell cell : placedCells) {
            if(!cell.isHit()) {
                return false;
            }
        }
        return true;
    }
    void sink() {
        if(!isVisible) {
            isVisible = true;
        }
        for(Cell cell : surroundCells) {
            cell.hit();
        }
    }

    boolean isPlaced() {
        return isPlaced;
    }
    void removeShip() {
//        isPlaced = false;
        if(!placedCells.isEmpty()) {
            for(Cell cellToRemove : placedCells) {
                cellToRemove.removeShip(this);
            }
            placedCells.clear();
            //Remove surroundCells
            for(Cell cell : surroundCells) {
                cell.removeSurroundShip(this);
            }
            surroundCells.clear();
        }
    }
    ArrayList<Cell> getPlacedCells() {
        return placedCells;
    }

    void placeShip(ArrayList<Cell> cellsToPlace, ArrayList<Cell> surroundCells) {
        //Set this ship on each cell-to-place
        int i = 0;
        for(Cell cell : cellsToPlace) {
            cell.setShip(this, getImgBaseId() + i++);
        }
        this.placedCells = cellsToPlace;
        //Set surround cells
        this.surroundCells = surroundCells;
        for(Cell cell : surroundCells) {
            cell.setSurroundShip(this);
        }

        isPlaced = true;
    }
}
