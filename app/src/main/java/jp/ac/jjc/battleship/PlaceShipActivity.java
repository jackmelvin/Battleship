package jp.ac.jjc.battleship;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import org.parceler.Parcels;

import java.util.ArrayList;

public class PlaceShipActivity extends AppCompatActivity {
    final int SIZE = 10;
    final int NUMOFSHIP = 10;
    Board userBoard;
    ArrayList<ImageButton> ibShips;
    ImageButton[][] ibUserBoard;
    Ship[] ships;
    Ship selectedShip = null;
    ImageButton selectedIbShip = null;
    boolean placingShip = false;
    ShipListener[] shipListeners;
    ArrayList<Cell> selectedShipCells;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_place_ship);

        createBoard();
    }

    public void enableBoard() {
        for(int x = 0; x < SIZE; x++) {
            for(int y = 0; y < SIZE; y++) {
                ibUserBoard[x][y].setClickable(true);
            }
        }
    }

    public void disableBoard() {
        for(int x = 0; x < SIZE; x++) {
            for(int y = 0; y < SIZE; y++) {
                ibUserBoard[x][y].setClickable(false);
            }
        }
    }

    public void updateCellImg(Cell cell) {
        int x = cell.getCoord()[0];
        int y = cell.getCoord()[1];
        ibUserBoard[x][y].setBackgroundResource(cell.getImgBaseId());
    }

    public void updateShipImg(Ship ship) {
        for(Cell cell : ship.getPlacedCells()) {
            updateCellImg(cell);
        }
    }

    public void deselectShip() {
        userBoard.getShips()[selectedShip.getId()] = selectedShip;
        selectedShip = null;
    }

    public void createBoard() {
        int userCellBaseId = R.id.user_cell_00;
        int cellCount = 0;
        ibUserBoard = new ImageButton[SIZE][SIZE];
        for(int x = 0; x < SIZE; x++) {
            for(int y = 0; y < SIZE; y++) {
                ibUserBoard[x][y] = findViewById(userCellBaseId + cellCount);
                ibUserBoard[x][y].setClickable(false);
                ibUserBoard[x][y].setOnClickListener(new BoardListener(x, y));
                ibUserBoard[x][y].setBackgroundColor(Color.TRANSPARENT);
                cellCount++;
            }
        }
        userBoard = new Board(SIZE);
        ships = userBoard.getShips();
        disableBoard();
        ibShips = new ArrayList<ImageButton>();
        ibShips.add((ImageButton)findViewById(R.id.ship_04_00));
        ibShips.add((ImageButton)findViewById(R.id.ship_03_00));
        ibShips.add((ImageButton)findViewById(R.id.ship_03_01));
        ibShips.add((ImageButton)findViewById(R.id.ship_02_00));
        ibShips.add((ImageButton)findViewById(R.id.ship_02_01));
        ibShips.add((ImageButton)findViewById(R.id.ship_02_02));
        ibShips.add((ImageButton)findViewById(R.id.ship_01_00));
        ibShips.add((ImageButton)findViewById(R.id.ship_01_01));
        ibShips.add((ImageButton)findViewById(R.id.ship_01_02));
        ibShips.add((ImageButton)findViewById(R.id.ship_01_03));
        shipListeners = new ShipListener[10];
        for(int i = 0; i < NUMOFSHIP; i++) {
            shipListeners[i] = new ShipListener(i);
            ibShips.get(i).setOnClickListener(shipListeners[i]);
        }
        ImageButton ibRotate = findViewById(R.id.ib_random);
        ImageButton ibRandom = findViewById(R.id.ib_rotate);
        ImageButton ibNext = findViewById(R.id.ib_next);
        ibRotate.setBackgroundColor(Color.TRANSPARENT);
        ibRandom.setBackgroundColor(Color.TRANSPARENT);
        ibNext.setBackgroundColor(Color.TRANSPARENT);
        FunctionListener funcListener = new FunctionListener();
        ibRotate.setOnClickListener(funcListener);
        ibRandom.setOnClickListener(funcListener);
        ibNext.setOnClickListener(funcListener);

    }

    public class ShipListener implements View.OnClickListener {
        private int shipId;
        public ShipListener(int shipId) {
            this.shipId = shipId;
        }
        @Override
        public void onClick(View view) {
            selectedShip = ships[shipId];
            selectedIbShip = (ImageButton)view;
            placingShip = true;
            enableBoard();
        }
    }

    public class BoardListener implements View.OnClickListener {
        int x, y;
        public BoardListener(int x, int y) {
            this.x = x;
            this.y = y;
        }
        @Override
        public void onClick(View view) {
            if(!userBoard.getCells()[x][y].hasShip()) {
                if(placingShip) { //Place selected Ship at empty Cell
                    if (userBoard.placeShip(selectedShip, userBoard.getCells()[x][y])) {
                        //Place ship at new cells
                        //userBoard.placeShip(selectedShip, userBoard.getCells()[x][y]);
                        updateShipImg(selectedShip);
                        //Update ship old cells img
                        if(selectedShipCells != null && !selectedShipCells.isEmpty()) {
                            for(Cell cell : selectedShipCells) {
                                updateCellImg(cell);
                            }
                            selectedShipCells.clear();
                        }
                        selectedIbShip.setVisibility(View.INVISIBLE);
                        selectedIbShip.setClickable(false);
                        ibShips.remove(selectedIbShip);
                        //End placing ship
                        placingShip = false;
                    } else {
                        System.out.println("Can't place ship here");
                    }
                } else {
                    selectedShip = null;
                }
            } else {
                selectedShip = userBoard.getCells()[x][y].getShip();
                //Remove ship from old cells
                        /*ArrayList<Cell> cells = new ArrayList<Cell>();
                        cells.addAll(selectedShip.getPlacedCells());*///Got a new way to write
                selectedShipCells = new ArrayList<Cell>(selectedShip.getPlacedCells());
                selectedShip.removeShip();
                placingShip = true;
                enableBoard();
            }
        }
    }

    public class FunctionListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch(id) {
                case R.id.ib_rotate:
                    if(selectedShip != null && selectedShip.isPlaced()) {
                        if(!placingShip) {
                            selectedShipCells = new ArrayList<Cell>(selectedShip.getPlacedCells());
                            selectedShip.removeShip();
                        }
                        //Update ship's old cells img
                        for(Cell cellToUpdate : selectedShipCells) {
                            updateCellImg(cellToUpdate);
                        }
                        selectedShip.changeDir();
                        if (!userBoard.placeShip(selectedShip, selectedShipCells.get(0))) {
                            selectedShip.changeDir();
                            userBoard.placeShip(selectedShip, selectedShipCells.get(0));
                        }
                        updateShipImg(selectedShip);
                        placingShip = false;
                    }
                    break;
                case R.id.ib_random:
                    for(ImageButton ship : ibShips) {
                        ship.setVisibility(View.GONE);
                    }
                    ibShips.clear();
                    //Remove all Ships from Board before placing them randomly
                    for(Ship ship : userBoard.getShips()) {
                        ship.removeShip();
                    }
                    userBoard.placeShipRandomly();
//                    for(Ship ship : userBoard.getShips()) {
//                        for(Cell cell : ship.getPlacedCells()) {
//                            int x = cell.getCoord()[0];
//                            int y = cell.getCoord()[1];
//                            ibUserBoard[x][y].setClickable(true);
//                        }
//                    }
                    //Update Board with randomly placed Ships
                    for(int x = 0; x < SIZE; x++) {
                        for(int y = 0; y < SIZE; y++) {
                            if(userBoard.getCells()[x][y].hasShip()) {
                                ibUserBoard[x][y].setBackgroundResource(userBoard.getCells()[x][y].getImgBaseId());
                                ibUserBoard[x][y].setClickable(true);
                            } else {
                                ibUserBoard[x][y].setBackgroundColor(Color.TRANSPARENT);
                            }
                        }
                    }
                    break;
                case R.id.ib_next:
                    //If all ships are placed on board, send Board and move to GamePlayActivity
                    if(ibShips.isEmpty()) {
                        Intent intent = new Intent(PlaceShipActivity.this, GamePlayActivity.class);
                        intent.putExtra("uBoard", Parcels.wrap(userBoard));
                        startActivity(intent);
                    }
            }
        }
    }

}
