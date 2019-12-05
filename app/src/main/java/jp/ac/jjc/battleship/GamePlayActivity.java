package jp.ac.jjc.battleship;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class GamePlayActivity extends AppCompatActivity {
    final int SIZE = 10;
    final Context context = this;
    final Handler handler = new Handler();
    final int NUMOFSHIP = 10;
    Board userBoard;
    Board comBoard;

    Ship selectedShip = null;
    boolean placingShip = false;
    int shipImgId = 0;

    TextView tvGuide;
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
            disableBoard(userBoard);
            //Hide parts for placing ship
            findViewById(R.id.place_ship_layout).setVisibility(View.GONE);
            //Display com board and com name
            findViewById(R.id.tv_com).setVisibility(View.VISIBLE);
            findViewById(R.id.com_board).setVisibility(View.VISIBLE);
            //Display Arrow
            ivArrow = findViewById(R.id.iv_arrow);
            ivArrow.setVisibility(View.VISIBLE);
            ivArrow.setTag("Right");
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

        void enableBoard(Board board) {
            for(int x = 0; x < SIZE; x++) {
                for(int y = 0; y < SIZE; y++) {
                    board.getCells()[x][y].setClickable(true);
                }
            }
        }

        void disableBoard(Board board) {
            for(int x = 0; x < SIZE; x++) {
                for(int y = 0; y < SIZE; y++) {
                    board.getCells()[x][y].setClickable(false);
                }
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
        //Create new UserBoard

        //Create cells for User board and Com board
        Cell[][] userCells = new Cell[SIZE][SIZE];
        Cell[][] comCells = new Cell[SIZE][SIZE];
        int userCellBaseId = R.id.user_cell_00;
        int comCellBaseId = R.id.com_cell_00;
        int cellCount = 0;
        UserBoardListener userBoardListener = new UserBoardListener();
        ComBoardListener comBoardListener = new ComBoardListener();
        for(int x = 0; x < SIZE; x++) {
            for(int y = 0; y < SIZE; y++) {
                //Set user cells
                userCells[x][y] = findViewById(userCellBaseId + cellCount);
                userCells[x][y].setCoord(x, y);
                userCells[x][y].setOnClickListener(userBoardListener);
                //Set com cells
                comCells[x][y] = findViewById(comCellBaseId + cellCount);
                comCells[x][y].setCoord(x, y);
                comCells[x][y].setOnClickListener(comBoardListener);
                cellCount++;
            }
        }

        //Create ships for User board and Com board
        ArrayList<Ship> userShips = new ArrayList<>();
        ArrayList<Ship> comShips = new ArrayList<>();
        ShipListener shipListener = new ShipListener();
        for(int i = 0; i < NUMOFSHIP; i++) {
            int size;
            if(i < 4) {
                size = 1;
            } else if (i < 7) {
                size = 2;
            } else if (i < 9) {
                size = 3;
            } else {
                size = 4;
            }
            //Set user ships
            Ship userShip = findViewById(R.id.ship_01_00 + i);
            userShip.setSize(size);
            userShip.setOnClickListener(shipListener);
            userShips.add(userShip);
            //Set com ships
            Ship comShip = new Ship(this);
            comShip.setSize(size);
            comShips.add(comShip);
        }

        //Create UserBoard
        userBoard = new Board(SIZE, userCells, userShips);
        //Create ComBoard
        comBoard = new Board(SIZE, comCells, comShips);
        //Create players
        user = new Player(userBoard, game);
        com = new ComPlayer(comBoard, game);
        //Hide all com ships until getting a hit
        for(int i = 0; i < SIZE; i++) {
            for(int j = 0; j < SIZE; j++) {
                com.getBoard().getCells()[i][j].setImageResource(R.drawable.cell_default);
            }
        }

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

        //Display guiding message
        tvGuide = findViewById(R.id.tv_guide);
        tvGuide.setClickable(false);

        //Display pause game button
        ImageButton ibPause = findViewById(R.id.ib_pause);
        ibPause.setVisibility(View.VISIBLE);
        ibPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                game.pauseGame();
            }
        });

        //Click a space to deselect ship
        findViewById(R.id.screen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deselectShip();
                selectedShip = null;
                placingShip = false;
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

    //Mark touched ship as selected
    public void selectShip(Ship ship) {
        selectedShip = ship;
        if(ship.isPlaced()) { //Ship has been placed on board
            for(Cell cell : selectedShip.getPlacedCells()) {
                cell.setImageResource(cell.getImgBaseId() + (2 * ship.getSize()));
            }
        } else { //Ship hasn't been placed on board
            switch(selectedShip.getSize()) {
                case 1:
                    shipImgId = R.drawable.ship_01_full_01;
                    break;
                case 2:
                    shipImgId = R.drawable.ship_02_full_01;
                    break;
                case 3:
                    shipImgId = R.drawable.ship_03_full_01;
                    break;
                case 4:
                    shipImgId = R.drawable.ship_04_full_01;
                    break;
            }
            selectedShip.setImageResource(shipImgId);
        }
    }
    //Remove selected mark from current selected ship
    public void deselectShip() {
        if(selectedShip == null) {
            return;
        }
        //Remove selected mark from pre-selected cells
        if(selectedShip.isPlaced()) { //Ship has been placed on board
            for(Cell cell : selectedShip.getPlacedCells()) {
                cell.updateImg();
            }
        } else { // Ship hasn't been placed on board
            selectedShip.setImageResource(shipImgId - 1);
        }
    }

    public void displayMessage(CharSequence message) {
        tvGuide.setText(message);
        tvGuide.setVisibility(View.VISIBLE);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tvGuide.setText("");
                tvGuide.setVisibility(View.GONE);
            }
        }, 1500);
    }

    public class ShipListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            game.playSoundEffect(soundIdPickup);
            //Deselect pre-selected ship
            deselectShip();
            //Select this ship
            selectShip((Ship)view);
            placingShip = true;
        }
    }

    public class UserBoardListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if(!((Cell)view).hasShip()) { //Selected a cell without ship
                if(placingShip) { //Place selected Ship at empty Cell
                    game.playSoundEffect(soundIdPlace);
                    //Place ship at new cells
                    if (userBoard.canPlaceShip(selectedShip, (Cell)view)) {
                        deselectShip();
                        //Remove full ship img (right)
                        if(userBoard.getShips().indexOf(selectedShip) != -1) {
                            selectedShip.setVisibility(View.INVISIBLE);
                            selectedShip.setClickable(false);
                        }
                        selectedShip.removeShip();
                        userBoard.placeShip(selectedShip, (Cell)view);
                        placingShip = false;
                        selectShip(selectedShip);
                    } else {
                        //Can't place ship
                        //Display warning message
                        displayMessage(getText(R.string.tv_guide_place_failed));

                    }
                } else {
                    deselectShip();
                    selectedShip = null;
                }
            } else { //Selected a cell with ship
                game.playSoundEffect(soundIdPickup);
                //Remove selecting mark from pre-selected ship
                deselectShip();
                //Change selectedShip to the one at clicked cell, mark as selected
                selectShip(((Cell)view).getShip());
                placingShip = true;
            }
        }
    }

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
                            deselectShip();
                            Cell selectedShipStartCell = selectedShip.getPlacedCells().get(0);
                            selectedShip.removeShip();
                            //Place rotated ship
                            userBoard.placeShip(selectedShip, selectedShipStartCell);
                            //Mark ship as selected
                            selectShip(selectedShip);
                            //Prevent moving ship after rotated
                            placingShip = false;
                        }
                    } else {
                        //No ship selected or selected ship hasn't been placed on board
                        //Display warning message
                        displayMessage(getString(R.string.tv_guide_rotate_select_ship));
                    }
                    break;
                case R.id.ib_random:
                    game.playSoundEffect(soundIdPlace);
                    deselectShip();
                    selectedShip = null;
                    placingShip = false;
                    //Hide all full-img-ship
                    for(Ship ship : userBoard.getShips()) {
                        ship.setVisibility(View.INVISIBLE);
                        ship.setClickable(false);
                    }
                    userBoard.placeShipRandomly();

                    break;
                case R.id.ib_next:
                    deselectShip();
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
                        game.startGame();
                    } else {
                        //Display warning message
                        displayMessage(getString(R.string.tv_guide_next_ship_not_placed));
                    }
            }
        }
    }

    private class ComBoardListener implements View.OnClickListener {
        //Get user shoot
        @Override
        public void onClick(View view) {
            if(((Cell)view).isHit()) {
                return;
            }
            ShootResult result = user.shoot((Cell)view);
            if(result == ShootResult.HIT) {
                ((Cell)view).setImageResource(R.drawable.user_hit);
            } else if(result == ShootResult.MISS) {
                //To com turn
                game.changeArrowDir();
                game.disableBoard(comBoard);
                com.randomlyShoot(user.getBoard());
            } else { //Kill
                //Show the sink ship
                ((Cell)view).getShip().sink();
                if(result == ShootResult.END) {
                    //EndGame
                    game.endGame(user);

                }
            }
            view.setClickable(false);
        }

    }


}
