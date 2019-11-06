package jp.ac.jjc.battleship;

import org.parceler.Parcel;

import java.util.ArrayList;
@Parcel
public class Ship {
    int size;
    int id;
    int imgBaseId;
    int numOfHit;
    boolean dir = true;
    boolean isPlaced;
    ArrayList<Cell> placedCells = new ArrayList<Cell>();
    ArrayList<Cell> surroundCells = new ArrayList<Cell>();
    //Empty constructor needed by the Parceler library
    public Ship() {
    }

    public Ship(int size, int id) {
        this.size = size;
        this.id = id;
    }

//    //For parcelable
//    @Override
//    public int describeContents() {
//        return 0;
//    }
//    @Override
//    public void writeToParcel(Parcel out, int flags) {
//        out.writeInt(size);
//        out.writeInt(id);
//        out.writeInt(imgBaseId);
//        out.writeBooleanArray(new boolean[] {
//                dir, isPlaced
//        });
//        out.writeList(placedCells);
//    }
//    Ship(Parcel in) {
//        this.size = in.readInt();
//        this.id = in.readInt();
//        this.imgBaseId = in.readInt();
//        boolean[] myBooleanArray = in.createBooleanArray();
//        this.dir = myBooleanArray[0];
//        this.isPlaced = myBooleanArray[1];
//    }
//    public static final Creator<Ship> CREATOR = new Creator<Ship>() {
//        @Override
//        public Ship createFromParcel(Parcel in) {
//            return new Ship(in);
//        }
//        @Override
//        public Ship[] newArray(int size) {
//            return new Ship[size];
//        }
//    };
//
//    //End parcelable

    public int getSize() {
        return size;
    }
    public int getId() {
        return id;
    }
    public void setDir(boolean dir) {
        this.dir = dir;
    }
    public boolean getDir() {
        return dir;
    }
    public void changeDir() {
        if(dir) {
            dir = false;
        } else {
            dir = true;
        }
    }
    public int getImgBaseId() {
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

    public boolean isSunk() {
        for(Cell cell : placedCells) {
            if(!cell.isHit()) {
                return false;
            }
        }
        return true;
    }
    public void sunk() {
        for(Cell cell : surroundCells) {
            cell.hit();
        }
    }

    public boolean isPlaced() {
        return isPlaced;
    }
    public void removeShip() {
//        isPlaced = false;
        if(!placedCells.isEmpty()) {
            for(Cell cellToRemove : placedCells) {
                cellToRemove.removeShip();
            }
            placedCells.clear();
            //Remove aroundCells
            for(Cell cell : surroundCells) {
                cell.shipsArround.remove(this);
            }
            surroundCells.clear();
        }
    }
    public ArrayList<Cell> getPlacedCells() {
        return placedCells;
    }
    public void setSurroundCells(ArrayList<Cell> arroundCells) {
        this.surroundCells = arroundCells;
    }
    public ArrayList<Cell> getSurroundCells() {
        return surroundCells;
    }
    public void placeShip(ArrayList<Cell> placedCells) {
        isPlaced = true;
        this.placedCells = placedCells;
    }
    public void rotate() {
        dir = !dir;
    }
}
