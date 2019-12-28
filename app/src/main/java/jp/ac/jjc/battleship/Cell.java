package jp.ac.jjc.battleship;

import java.util.ArrayList;

public class Cell {
    /*Contain x-coordinate of the cell*/
    private int x = 0;
    /*Contain y-coordinate of the cell*/
    private int y = 0;
    private int imageId = -1;
    private int imageIdTemp = -1;

    private Ship ship = null;
    /*Indicating if the cell has been hit or not*/
    private boolean isHit = false;
    /*Indicating if there is a ship placed on the cell*/
    private ArrayList<Ship> shipsSurround = new ArrayList<>();

    Cell(int x, int y) {
        this.x = x;
        this.y = y;
        imageId = R.drawable.cell_default;
    }

    int getX() {
        return x;
    }

    int getY() {
        return y;
    }

    void hit() {
        isHit = true;
        if(hasShip()) {
            imageId += ship.getSize();
        } else {
            imageId = R.drawable.cell_miss;
        }
    }

    boolean isHit() {
        return isHit;
    }

    boolean hasShip() {
        return (ship != null);
    }

    void setShip(Ship ship, int imgBaseId) {
        this.ship = ship;
        this.imageId = imgBaseId;
    }

    void removeShip(Ship shipToRemove) {
        ship = null;
        if(shipsSurround.indexOf(shipToRemove) != -1) {
            shipsSurround.remove(shipToRemove);
        }
        imageId = R.drawable.cell_default;
    }

    int getImageId() {
        return imageId;
    }

    void setImageId(int imageId) {
        imageIdTemp = this.imageId;
        this.imageId = imageId;
    }

    void restoreImageId() {
        imageId = imageIdTemp;
        imageIdTemp = 0;
    }

    Ship getShip() {
        return ship;
    }

    void setSurroundShip(Ship ship) {
        shipsSurround.add(ship);
    }

    void removeSurroundShip(Ship ship) {
        shipsSurround.remove(ship);
    }

    ArrayList<Ship> getShipsSurround() {
        return shipsSurround;
    }
}
