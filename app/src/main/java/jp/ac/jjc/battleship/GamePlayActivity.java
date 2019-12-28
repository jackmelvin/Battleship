package jp.ac.jjc.battleship;

import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class GamePlayActivity extends AppCompatActivity {
    final int SIZE = 10;
    final Context context = this;
    final int NUM_OF_SHIP = 10;
    Board userBoard;
    Board comBoard;
    BoardView userBoardView;
    BoardView comBoardView;

    Ship selectedShip = null;

    ImageView ivArrow;
    Dialog dialog;
    TextView tvEndGame;
    ImageButton ibContinue;
    ImageButton ibMainMenu;
    ImageButton ibReplay;
    SharedPreferences sharedPref;
    MediaManager mediaManager;
    Switch swSound;
    Switch swMusic;
    Player user;
    ComPlayer com;
    GamePlay game;
    Toast toast = null;


    SoundPool soundPool;
    int soundIdGameStart;
    int soundIdPlace;
    int soundIdHit;
    int soundIdMiss;
    int soundIdFire;
    int soundIdPickup;
    int soundIdWin;
    int soundIdLoose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_game_play);

        game = new GamePlay();
        game.setUpGame();
        System.out.println("GP onCreate");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
//        mediaManager.releaseMediaPlayer();
        saveSettings();
        System.out.println("GP onDestroy");
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        if(swMusic.isChecked()) {
//            mediaManager.pause();
//        }
//        saveSettings();
//        System.out.println("GP onStop");
//    }

    @Override
    protected void onPause() {
        super.onPause();
        if(swMusic.isChecked()) {
            mediaManager.pause();
        }
        saveSettings();
        System.out.println("GP onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Load settings
        swMusic.setChecked(sharedPref.getBoolean("music", false));
        swSound.setChecked(sharedPref.getBoolean("sound", false));
        if(swMusic.isChecked()) {
            mediaManager.play();
        }
        if(swSound.isChecked()) {
            createSoundEffect();
        }
        System.out.println("GP onResume");
    }

    class GamePlay {
        void setUpGame() {
            createBoard();
            createDialog();
            //Change background music
            mediaManager.loadMusic(R.raw.bg_music_play);
//            createSoundEffect();
            setDisplayParts();
        }

        void startGame() {
            //Play start game sound
            playSoundEffect(soundIdGameStart);
            //Disable user board
            disableBoard(user);
            //Hide parts for placing ship
            findViewById(R.id.place_ship_layout).setVisibility(View.GONE);
            //Display com board and com name
            findViewById(R.id.tv_com).setVisibility(View.VISIBLE);
            findViewById(R.id.com_board).setVisibility(View.VISIBLE);
            //Display Arrow
            ivArrow = findViewById(R.id.iv_arrow);
            ivArrow.setVisibility(View.VISIBLE);
            ivArrow.setTag("Right");
            //Draw user board and com board
            comBoardView.readyToDraw();
            userBoardView.readyToDraw();
            userBoardView.invalidate();
        }

        void endGame(Player player) {
            if(player == user) {
                playSoundEffect(soundIdWin);
                tvEndGame.setText(getString(R.string.tv_endGame_win));
            } else {
                playSoundEffect(soundIdLoose);
                tvEndGame.setText(getString(R.string.tv_endGame_lose));
            }
            tvEndGame.setVisibility(View.VISIBLE);
            ibContinue.setVisibility(View.GONE);
            dialog.findViewById(R.id.ll_sound_music_control).setVisibility(View.GONE);
            dialog.show();
        }

        void pauseGame() {
            tvEndGame.setVisibility(View.GONE);
            ibContinue.setVisibility(View.VISIBLE);
            dialog.show();
        }
        void changeArrowDir() {
            //To user turn
            if(ivArrow.getTag().equals("Left")) {
                ivArrow.setImageResource(R.drawable.iv_arrow_right);
                ivArrow.setTag("Right");
                TextView tvUser = findViewById(R.id.tv_user);
                tvUser.setTextColor(Color.RED);
                TextView tvCom = findViewById(R.id.tv_com);
                tvCom.setTextColor(Color.BLACK);
            } else if(ivArrow.getTag().equals("Right")) { //To COM turn
                ivArrow.setImageResource(R.drawable.iv_arrow_left);
                ivArrow.setTag("Left");
                TextView tvCom = findViewById(R.id.tv_com);
                tvCom.setTextColor(Color.RED);
                TextView tvUser = findViewById(R.id.tv_user);
                tvUser.setTextColor(Color.BLACK);
            }
        }

        void enableComBoard() {
            comBoardView.setOnTouchListener(new ComBoardListener());
        }

        void disableBoard(Player whoseBoard) {
            if(whoseBoard instanceof ComPlayer) {
                comBoardView.setOnTouchListener(null);
            } else {
                userBoardView.setOnTouchListener(null);
            }
        }

        void playSoundEffect(String soundName) {
            if(soundName.equals("HIT")) {
                playSoundEffect(soundIdHit);
            }
            if(soundName.equals("MISS")) {
                playSoundEffect(soundIdMiss);
            }
            if(soundName.equals("FIRE")) {
                playSoundEffect(soundIdFire);
            }
        }

        void playSoundEffect(int soundId) {
            if(soundPool == null) {
                return;
            }
            final float LEFT_VOLUME_VALUE = 1.0f;
            final float RIGHT_VOLUME_VALUE = 1.0f;
            final int MUSIC_LOOP = 0;
            final int SOUND_PLAY_PRIORITY = 0;
            final float PLAY_RATE= 1.0f;
            soundPool.play(soundId, LEFT_VOLUME_VALUE, RIGHT_VOLUME_VALUE, SOUND_PLAY_PRIORITY, MUSIC_LOOP, PLAY_RATE);
        }
    }

    public void createBoard() {

        userBoardView = (BoardView) findViewById(R.id.user_board);
        comBoardView = (BoardView) findViewById(R.id.com_board);

//        userBoardView.setOnTouchListener(new BoardTouchListener());
        comBoardView.setOnTouchListener(new ComBoardListener());
        userBoardView.setOnDragListener(new BoardOnDragListener());

        findViewById(R.id.screen).setOnDragListener(new View.OnDragListener(){
            @Override
            public boolean onDrag(View view, DragEvent event) {
                int action = event.getAction();
                if(action == DragEvent.ACTION_DROP) {
                        View v = (View) event.getLocalState();
                        v.setVisibility(View.VISIBLE);
                        return true;
                }
                return true;
            }
        });

        //Create ships for User board and Com board
        ArrayList<Ship> userShips = new ArrayList<>();
        ArrayList<Ship> comShips = new ArrayList<>();
        int shipImgId;
        for(int i = 0; i < NUM_OF_SHIP; i++) {
            int size;
            if(i < 4) {
                size = 1;
                shipImgId = R.drawable.ship_01_full_00;
            } else if (i < 7) {
                size = 2;
                shipImgId = R.drawable.ship_02_full_00;
            } else if (i < 9) {
                size = 3;
                shipImgId = R.drawable.ship_03_full_00;
            } else {
                size = 4;
                shipImgId = R.drawable.ship_04_full_00;
            }
            //Set user ships
            Ship userShip = findViewById(R.id.ship_01_00 + i);
            userShip.setSize(size);
            userShip.setShipImgId(shipImgId);
            userShip.setOnTouchListener(new ShipTouchListener());
            userShip.isVisible(true);
            userShips.add(userShip);
            //Set com ships
            Ship comShip = new Ship(this);
            comShip.setSize(size);
            //Hide all com ships until getting a hit
            comShip.isVisible(false);
            comShips.add(comShip);
        }

        //Create UserBoard
        userBoard = new Board(SIZE, userShips);
        userBoardView.setBoard(userBoard);
        //Create ComBoard
        comBoard = new Board(SIZE, comShips);
        comBoardView.setBoard(comBoard);
        //Create players
        user = new Player(userBoard, game);
        com = new ComPlayer(comBoard, game);

    }

    public void createSoundEffect() {
        final int NUMBER_OF_SIMULTANEOUS_SOUNDS = 5;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(NUMBER_OF_SIMULTANEOUS_SOUNDS)
                    .build();
        } else {
            // Deprecated way of creating a SoundPool before Android API 21.
            soundPool = new SoundPool(NUMBER_OF_SIMULTANEOUS_SOUNDS, AudioManager.STREAM_MUSIC, 0);
        }
        soundIdGameStart = soundPool.load(getApplicationContext(), R.raw.se_game_start, 1);
        soundIdPlace = soundPool.load(getApplicationContext(), R.raw.se_place, 1);
        soundIdHit = soundPool.load(getApplicationContext(), R.raw.se_hit, 1);
        soundIdMiss = soundPool.load(getApplicationContext(), R.raw.se_miss, 1);
        soundIdFire = soundPool.load(getApplicationContext(), R.raw.se_fire, 1);
        soundIdPickup = soundPool.load(getApplicationContext(), R.raw.se_pickup, 1);
        soundIdWin = soundPool.load(getApplicationContext(), R.raw.se_win, 1);
        soundIdLoose = soundPool.load(getApplicationContext(), R.raw.se_loose, 1);
    }

    public void setDisplayParts() {
        ImageButton ibRotate = findViewById(R.id.ib_random);
        ImageButton ibRandom = findViewById(R.id.ib_rotate);
        ImageButton ibNext = findViewById(R.id.ib_next);

        FunctionListener funcListener = new FunctionListener();
        ibRotate.setOnClickListener(funcListener);
        ibRandom.setOnClickListener(funcListener);
        ibNext.setOnClickListener(funcListener);

        //Display pause game button
        ImageButton ibPause = findViewById(R.id.ib_pause);
        ibPause.setVisibility(View.VISIBLE);
        ibPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                game.pauseGame();
            }
        });
    }

    public void createDialog() {
        //Create a dialog for pausing and ending game
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.game_play_dialog);
        dialog.setTitle("Title...");

        //End game message
        tvEndGame = dialog.findViewById(R.id.tv_endGame);
        tvEndGame.setBackgroundColor(Color.TRANSPARENT);
        //Buttons
        // Continue button, just close the dialog
        ibContinue = dialog.findViewById(R.id.ib_continue);
        ibContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        // Main menu button
        ibMainMenu = dialog.findViewById(R.id.ib_mainMenu);
        ibMainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GamePlayActivity.this, MainActivity.class);
                startActivity(intent);
                if (dialog != null) {
                    dialog.dismiss();
                    dialog = null;
                }
                GamePlayActivity.this.finish();
            }
        });
        // Replay button
        ibReplay = dialog.findViewById(R.id.ib_replay);
        ibReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                recreate();
            }
        });

        sharedPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        swMusic = dialog.findViewById(R.id.sw_music);
        swSound = dialog.findViewById(R.id.sw_sound);

        mediaManager = MediaManager.getInstance(this);

        swMusic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    mediaManager.play();
                } else {
                    mediaManager.pause();
                }
            }
        });

        swSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    createSoundEffect();
                } else {
                    soundPool.release();
                }
            }
        });
//            The dialog can only be canceled by clicking Continue Button
        dialog.setCanceledOnTouchOutside(false);
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("music", swMusic.isChecked());
        editor.putBoolean("sound", swSound.isChecked());
        editor.apply();
    }

//    //Mark touched ship as selected
//    public void selectShip(Ship ship) {
//        selectedShip = ship;
//        if(ship.isPlaced()) { //Ship has been placed on board
//            for(Cell cell : selectedShip.getPlacedCells()) {
//                cell.setImageId(cell.getImageId() + (2 * ship.getSize()));
//            }
//        } else { //Ship hasn't been placed on board
//            switch(selectedShip.getSize()) {
//                case 1:
//                    shipImgId = R.drawable.ship_01_full_01;
//                    break;
//                case 2:
//                    shipImgId = R.drawable.ship_02_full_01;
//                    break;
//                case 3:
//                    shipImgId = R.drawable.ship_03_full_01;
//                    break;
//                case 4:
//                    shipImgId = R.drawable.ship_04_full_01;
//                    break;
//            }
//            selectedShip.setImageResource(shipImgId);
//        }
//    }
//    //Remove selected mark from current selected ship
//    public void deselectShip() {
//        if(selectedShip == null) {
//            return;
//        }
//        //Remove selected mark from pre-selected cells
//        if(selectedShip.isPlaced()) { //Ship has been placed on board
//            for(Cell cell : selectedShip.getPlacedCells()) {
//                cell.updateImg();
//            }
//        } else { // Ship hasn't been placed on board
//            selectedShip.setImageResource(shipImgId - 1);
//        }
//    }

    public void displayMessage(CharSequence message) {
        if(toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(GamePlayActivity.this, message, Toast.LENGTH_LONG);
        toast.show();
    }

//    public class UserBoardListener implements View.OnTouchListener {
//        @Override
//        public boolean onTouch(View view, MotionEvent event) {
//            int action = event.getAction();
//            Cell cell = ((BoardView)view).locateCell(event.getX(), event.getY());
//            System.out.println("Touched cell: " + cell.getX() + "," + cell.getY());
//            if(action == MotionEvent.ACTION_DOWN) {
//                if(!cell.hasShip()) { //Selected a cell without ship
//                    if(placingShip) { //Place selected Ship at empty Cell
//                        game.playSoundEffect(soundIdPlace);
//                        //Place ship at new cells
//                        if (userBoard.canPlaceShip(selectedShip, cell)) {
////                            deselectShip();
//                            //Remove full ship img (right)
//                            if(userBoard.getShips().indexOf(selectedShip) != -1) {
//                                selectedShip.setVisibility(View.INVISIBLE);
//                                selectedShip.setClickable(false);
//                            }
//                            selectedShip.removeShip();
//                            userBoard.placeShip(selectedShip, cell);
//                            placingShip = false;
////                            selectShip(selectedShip);
//                        } else {
//                            //Can't place ship
//                            //Display warning message
//                            displayMessage(getText(R.string.tv_guide_place_failed));
//
//                        }
//                    } else {
////                        deselectShip();
//                        selectedShip = null;
//                    }
//                } else { //Selected a cell with ship
//                    game.playSoundEffect(soundIdPickup);
//                    //Remove selecting mark from pre-selected ship
////                    deselectShip();
//                    //Change selectedShip to the one at clicked cell, mark as selected
////                    selectShip(cell.getShip());
//                    placingShip = true;
//                }
//                ((BoardView)view).invalidate();
//            }
//            return true;
//        }
//    }

    public class FunctionListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch(id) {
                case R.id.ib_rotate:
                    game.playSoundEffect(soundIdPlace);
                    if(selectedShip != null && selectedShip.isPlaced()) {
                        selectedShip.rotate();
                        if (!userBoard.canPlaceShip(selectedShip, selectedShip.getPlacedCells().get(0))) {
                            //Can't rotate ship
                            //Display warning message
                            displayMessage(getString(R.string.tv_guide_rotate_failed));
                            selectedShip.rotate();
                        } else {
                            //Remove ship
//                            deselectShip();
                            Cell selectedShipStartCell = selectedShip.getPlacedCells().get(0);
                            selectedShip.removeShip();
                            //Place rotated ship
                            userBoard.placeShip(selectedShip, selectedShipStartCell);
                            //Mark ship as selected
//                            selectShip(selectedShip);
                            float cellSize = userBoardView.cellSize();
                            RelativeLayout.LayoutParams params;
                            if(selectedShip.getDir()) {
                                params = new RelativeLayout.LayoutParams((int)(cellSize * selectedShip.getSize()), (int)cellSize);
                                params.leftMargin = (int)(selectedShipStartCell.getX() * cellSize);
                                params.topMargin = (int)(selectedShipStartCell.getY() * cellSize);
                            } else {
                                params = new RelativeLayout.LayoutParams((int)cellSize, (int)(cellSize * selectedShip.getSize()));
                                params.leftMargin = (int)(selectedShipStartCell.getX() * cellSize);
                                params.topMargin = (int)(selectedShipStartCell.getY() * cellSize);
                            }
                            selectedShip.setLayoutParams(params);
                        }
                    } else {
                        //No ship selected or selected ship hasn't been placed on board
                        //Display warning message
                        displayMessage(getString(R.string.tv_guide_rotate_select_ship));
                    }
                    break;
                case R.id.ib_random:
                    game.playSoundEffect(soundIdPlace);
//                    deselectShip();
                    selectedShip = null;
                    //Hide all full-img-ship
                    for(Ship ship : userBoard.getShips()) {
                        ship.setVisibility(View.INVISIBLE);
                        ship.setClickable(false);
                    }
                    userBoard.placeShipRandomly();
                    userBoardView.readyToDraw();
                    userBoardView.invalidate();
                    break;
                case R.id.ib_next:
//                    deselectShip();
                    //If all ships are placed on board, send Board and move to GamePlayActivity
                    boolean allShipsPlaced = true;
                    for(Ship ship : userBoard.getShips()) {
                        if(!ship.isPlaced()) {
                            allShipsPlaced = false;
                        }
                    }
                    if(allShipsPlaced) {
//                        Intent intent = new Intent(GamePlayActivity.this, GamePlayActivity.class);
//                        intent.putExtra("uBoard", Parcels.wrap(userBoard));
//                        startActivity(intent);
                        //Hide ship objects and then draw them on board with Canvas
                        for(Ship ship : user.getBoard().getShips()) {
                            ship.setVisibility(View.GONE);
                        }
                        game.startGame();
                    } else {
                        //Display warning message
                        displayMessage(getString(R.string.tv_guide_next_ship_not_placed));
                    }
            }
        }
    }

    private class ComBoardListener implements View.OnTouchListener {
        //Get user shoot
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            int action = event.getAction();
            Cell cell = ((BoardView)view).locateCell(event.getX(), event.getY());
            if(action == MotionEvent.ACTION_DOWN) {
                if(cell.isHit()) {
                    return false;
                }
                ShootResult result = user.shoot(cell);
                if(result == ShootResult.HIT) {
                    //Do nothing
                } else if(result == ShootResult.MISS) {
                    //To com turn
                    game.changeArrowDir();
                    game.disableBoard(com);
                    com.randomlyShoot(user.getBoard(), userBoardView);
                } else { //Kill
                    //Show the sink ship
                    cell.getShip().sink();
                    if(result == ShootResult.END) {
                        //EndGame
                        game.endGame(user);

                    }
                }
                view.setClickable(false);
                ((BoardView)view).invalidate();
            }
            return true;
        }
    }

    private class ShipTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            System.out.println("Ship touched");
            int action = event.getAction();
            if(action == MotionEvent.ACTION_DOWN) {
                selectedShip = (Ship) view;
                if(selectedShip.isPlaced()) { //If ship is already placed on board
                    //Remove ship
                    selectedShip.removeShip();
                }
                ClipData clipData = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDrag(clipData, shadowBuilder, view, 0);
                view.setVisibility(View.INVISIBLE);
                return true;
            } else {
                return false;
            }
        }
    }

//    private class BoardTouchListener implements View.OnTouchListener {
//        @Override
//        public boolean onTouch(View board, MotionEvent event) {
//            int action = event.getAction();
////            if(action == MotionEvent.ACTION_DOWN) {
////                for(Ship ship : userBoard.getShips()) {
////                    ship.setVisibility(View.VISIBLE);
////                }
////            }
//            userBoardView.invalidate();
//            return true;
//        }
//    }

    private class BoardOnDragListener implements View.OnDragListener {

        @Override
        public boolean onDrag(View board, DragEvent event) {
            int action = event.getAction();
            float cellSize = ((BoardView)board).cellSize();
            RelativeLayout.LayoutParams params;
            Cell cell = null;
            float cellX, cellY;
            switch (action) {
                case DragEvent.ACTION_DRAG_STARTED:
                    //Do nothing
                    break;
                case DragEvent.ACTION_DRAG_LOCATION:
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    //Draw ship shadow
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
//                    if(cell != null) {
//                        for(int i = 0; i < selectedShip.getSize(); i++) {
//                            user.getBoard().getCells()[cell.getX() + i][cell.getY() + i].restoreImageId();
//                        }
//                    }
                    //Remove ship shadow
                    break;
                case DragEvent.ACTION_DROP:
                    if(selectedShip.getDir()) { //Horizontal ship
                        params = new RelativeLayout.LayoutParams((int)cellSize * selectedShip.getSize(), (int)cellSize);
                        cellX = event.getX() - ((float)selectedShip.getSize() / 2.0f - 0.5f) * cellSize;
                        if(cellX < 0) {
                            cellX = 0;
                        }
                        cellY = event.getY();
                    } else { //Vertical ship
                        params  = new RelativeLayout.LayoutParams((int)cellSize, (int)cellSize * selectedShip.getSize());
                        cellX = event.getX();
                        cellY = event.getY() - ((float)selectedShip.getSize() / 2.0f - 0.5f) * cellSize;
                        if(cellY < 0) {
                            cellY = 0;
                        }
                    }
                    cell = ((BoardView)board).locateCell(cellX, cellY);

                    if(cell != null && user.getBoard().canPlaceShip(selectedShip, cell)) {
                        //Drop the ship on the boardView
                            //Remove original view
                        View view = (View) event.getLocalState();
                        ViewGroup owner = (ViewGroup) view.getParent();
                        owner.removeView(view);
                            //Add view to boardView
                        params.leftMargin = (int) (cell.getX() * cellSize);
                        params.topMargin = (int) (cell.getY() * cellSize);
                        ((RelativeLayout)board).addView(view, params);
                        view.setOnTouchListener(new ShipTouchListener());
                        view.setVisibility(View.VISIBLE);

                        //Place the ship on the board
                        user.getBoard().placeShip(selectedShip, cell);
//                        board.invalidate();
                    } else {
                        //Can't place ship
                        //Display warning message
                        displayMessage(getText(R.string.tv_guide_place_failed));
                        //Place ship at original place
                        ((View)event.getLocalState()).setVisibility(View.VISIBLE);
                    }

//                    BoardView container = (BoardView) board;
//                    container.addView(view);
//                    view.setVisibility(View.VISIBLE);
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    break;
            }
            return true;
        }
    }



}
