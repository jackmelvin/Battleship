package jp.ac.jjc.battleship;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageButton;

import java.util.ArrayList;

public class Cell extends AppCompatImageButton {
    /*Contain x-coordinate of the cell*/
    private int x = 0;
    /*Contain y-coordinate of the cell*/
    private int y = 0;
    private int imgBaseId;
    private Ship ship;
    /*Indicating if the cell has been hit or not*/
    private boolean isHit = false;
    /*Indicating if there is a ship placed on the cell*/
    private boolean hasShip = false;
    private ArrayList<Ship> shipsSurround = new ArrayList<>();

    public Cell(Context context) {
        super(context);
        imgBaseId = R.drawable.cell_default;
    }

    public Cell(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    void setCoord(int x, int y) {
        this.x = x;
        this.y = y;
    }

    int[] getCoord() {
        return new int[]{x, y};
    }

    void hit() {
        isHit = true;
        if(hasShip) {
            imgBaseId += ship.getSize();
        } else {
            imgBaseId = R.drawable.cell_miss;
        }
        setImageResource(imgBaseId);
    }

    boolean isHit() {
        return isHit;
    }

    void updateImg() {
        setImageResource(imgBaseId);
    }

    boolean hasShip() {
        return hasShip;
    }

    void setShip(Ship ship, int imgBaseId) {
        hasShip = true;
        this.ship = ship;
        this.imgBaseId = imgBaseId;
        setImageResource(imgBaseId);
    }

    void removeShip(Ship shipToRemove) {
        hasShip = false;
        ship = null;
        if(shipsSurround.indexOf(shipToRemove) != -1) {
            shipsSurround.remove(shipToRemove);
        }
        imgBaseId = R.drawable.cell_default;
        setImageResource(imgBaseId);
    }

    int getImgBaseId() {
        return imgBaseId;
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
