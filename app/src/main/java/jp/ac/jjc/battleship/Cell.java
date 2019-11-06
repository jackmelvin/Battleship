package jp.ac.jjc.battleship;

import android.graphics.Color;

import org.parceler.Parcel;

import java.util.ArrayList;

@Parcel
public class Cell {// implements Parcelable {
    /**Contain x-coordinate of the cell*/
    int x = 0;
    /**Contain y-coordinate of the cell*/
    int y = 0;
    int imgBaseId = Color.TRANSPARENT;
    Ship ship;
    /**Indicating if the cell has been hit or not*/
    boolean isHit = false;
    boolean hasShip = false;
    public ArrayList<Ship> shipsArround = new ArrayList<Ship>();
    
    //Empty constructor needed by Parceler library
    public Cell() {
    }
    
    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setCoord(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public int[] getCoord() {
        int[] coord = {x, y};
        return coord;
    }
    public void hit() {
        isHit = true;
        if(hasShip) {
            imgBaseId += ship.getSize();
        } else {
            imgBaseId = R.drawable.cell_miss;
        }
    }
    public boolean isHit() {
        return isHit;
    }
    public boolean hasShip() {
        return hasShip;
    }
    public void setShip(Ship ship, int imgBaseId) {
        hasShip = true;
        this.ship = ship;
        this.imgBaseId = imgBaseId;
    }
    public void removeShip() {
        hasShip = false;
        ship = null;
        this.imgBaseId = Color.TRANSPARENT;
    }
    public int getImgBaseId() {
        return imgBaseId;
    }
    public void setImgBaseId(int imgBaseId) {
        this.imgBaseId = imgBaseId;
    }
    public Ship getShip() {
        return ship;
    }
    public boolean isAroundShip() {
        if(shipsArround.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }
}
