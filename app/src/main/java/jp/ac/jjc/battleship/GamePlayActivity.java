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
import android.util.Log;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.Set;

public class GamePlayActivity extends AppCompatActivity {
    final int BOARD_SIZE = 10;
    final int NUMBER_OF_SHIPS = 10;
    final Context context = this;
    Board userBoard;
    Board comBoard;
    BoardView userBoardView;
    BoardView comBoardView;
    ImageView[] userShipViews;

    Ship selectedShip = null;

    ImageView ivArrow;
    Dialog dialog;
    TextView tvEndGame;
    ImageButton ibContinue;
    ImageButton ibMainMenu;
    ImageButton ibReplay;
    ImageButton ibNext;
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
        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

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
        mediaManager.releaseMediaPlayer();
        saveSettings();
        System.out.println("GP onDestroy");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(swMusic.isChecked()) {
            mediaManager.pause();
        }
        saveSettings();
        System.out.println("GP onStop");
    }

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
        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        //Load settings
        swMusic.setChecked(sharedPref.getBoolean("music", false));
        swSound.setChecked(sharedPref.getBoolean("sound", false));
        if(swMusic.isChecked()) {
            mediaManager.loadMusic(R.raw.bg_music_play);
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
            ivArrow.setVisibility(View.VISIBLE);
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
        //Create user board
        userBoard = new Board(BOARD_SIZE, NUMBER_OF_SHIPS);
        //Display user board on the screen
        userBoardView = findViewById(R.id.user_board);
        userBoardView.setBoard(userBoard);
        userBoardView.setOnDragListener(new BoardOnDragListener());
        user = new Player(userBoard, game);
        comBoard = new Board(BOARD_SIZE, NUMBER_OF_SHIPS);
        for(int i = 0; i < NUMBER_OF_SHIPS; i++) {
            comBoard.getShip(i).setInvisible();
        }
        comBoardView = findViewById(R.id.com_board);
        comBoardView.setBoard(comBoard);
        comBoardView.setOnTouchListener(new ComBoardListener());
        com = new ComPlayer(comBoard, game);
        //Find ships for drag and drop placing
        userShipViews = new ImageView[NUMBER_OF_SHIPS];
        for(int i = 0; i < NUMBER_OF_SHIPS; i++) {
            userShipViews[i] = findViewById(R.id.ship_01_00 + i);
            userShipViews[i].setOnTouchListener(new ShipTouchListener(i));
        }
        FunctionListener functionListener = new FunctionListener();
        final ImageButton ibPause = findViewById(R.id.ib_pause);
        final ImageButton ibRotate = findViewById(R.id.ib_rotate);
        final ImageButton ibRandom = findViewById(R.id.ib_random);
        ibNext = findViewById(R.id.ib_next);
        ibRotate.setOnClickListener(functionListener);
        ibRandom.setOnClickListener(functionListener);
        ibNext.setOnClickListener((functionListener));
        ivArrow = findViewById(R.id.iv_arrow);
        ivArrow.setTag("Right");

//        Resize boards, ships, buttons upon screen size
        userBoardView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                userBoardView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int cellSize = (int)userBoardView.cellSize();
                int boardSize = (int)userBoardView.boardSize();
                //Resize ships
                for(int i = 0; i < NUMBER_OF_SHIPS; i++) {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) userShipViews[i].getLayoutParams();
                    params.height = cellSize;
                    params.width = cellSize * userBoard.getShip(i).getSize();
                    params.rightMargin = cellSize;
                    params.bottomMargin = cellSize;
                    userShipViews[i].setLayoutParams(params);
                }
                //Resize user board
                resizeView(userBoardView, boardSize, boardSize);
                //Resize com board
                resizeView(comBoardView, boardSize, boardSize);
                //Set rotate button's size
                resizeView(ibRotate, (int)(cellSize * 1.5), (int)(cellSize * 1.5));
                //Set random button's size
                resizeView(ibRandom, (int)(cellSize * 1.5), (int)(cellSize * 1.5));
                //Set next button's size
                resizeView(ibNext, (int)(cellSize * 1.5), (int)(cellSize * 1.5));
                //Set pause button's size
                resizeView(ibPause, cellSize, cellSize);
                //Set arrow's size
                resizeView(ivArrow, cellSize, cellSize);
            }

            private void resizeView(View view, int width, int height) {
                ConstraintLayout.LayoutParams params;
                params = (ConstraintLayout.LayoutParams)view.getLayoutParams();
                params.width = width;
                params.height = height;
                view.setLayoutParams(params);
            }
        });
        findViewById(R.id.screen).setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent event) {
                if(event.getAction() == DragEvent.ACTION_DROP) {
                    View v = (View) event.getLocalState();
                    v.setVisibility(View.VISIBLE);
                    return true;
                }
                return true;
            }
        });

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
        dialog.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

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

        mediaManager = MediaManager.getInstance(context);

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

    public class FunctionListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch(id) {
                case R.id.ib_rotate:
                    game.playSoundEffect(soundIdPlace);
                    if(selectedShip != null && selectedShip.isPlaced()) {
                        Cell headCell = selectedShip.rotate();
                        if (!userBoard.placeShip(selectedShip, headCell)) {
                            //Can't rotate ship
                            //Display warning message
                            displayMessage(getString(R.string.tv_guide_rotate_failed));
                        } else {
                            //Resize ship view
                            float cellSize = userBoardView.cellSize();
                            RelativeLayout.LayoutParams params;
                            if(selectedShip.isHorizontal()) {
                                params = new RelativeLayout.LayoutParams((int)(cellSize * selectedShip.getSize()), (int)cellSize);
                            } else {
                                params = new RelativeLayout.LayoutParams((int)cellSize, (int)(cellSize * selectedShip.getSize()));
                            }
                            params.leftMargin = (int)(headCell.getX() * cellSize);
                            params.topMargin = (int)(headCell.getY() * cellSize);
                            userShipViews[selectedShip.getId()].setLayoutParams(params);
                            //Set rotated image to ship view
                            userShipViews[selectedShip.getId()].setImageResource(selectedShip.getImageId());
                        }
                    } else {
                        //No ship selected or selected ship hasn't been placed on board
                        //Display warning message
                        displayMessage(getString(R.string.tv_guide_rotate_select_ship));
                    }
                    break;
                case R.id.ib_random:
                    game.playSoundEffect(soundIdPlace);
                    selectedShip = null;
                    //Hide all ships View
                    for(ImageView ship : userShipViews) {
                        ship.setVisibility(View.INVISIBLE);
                        ship.setClickable(false);
                    }
                    userBoard.placeShipRandomly();

                    for(int i = 0; i < NUMBER_OF_SHIPS; i++) {
                        //Move ShipViews into BoardView
                        //Remove original view
                        ViewGroup owner = (ViewGroup) userShipViews[i].getParent();
                        owner.removeView(userShipViews[i]);
                        //Add view to boardView

                        Ship ship = userBoard.getShip(i);
                        Cell headCell = ship.getHeadCell();
                        float cellSize = userBoardView.cellSize();
                        RelativeLayout.LayoutParams params;
                        if(ship.isHorizontal()) {
                            params = new RelativeLayout.LayoutParams((int)(cellSize * ship.getSize()), (int)cellSize);
                        } else {
                            params = new RelativeLayout.LayoutParams((int)cellSize, (int)(cellSize * ship.getSize()));
                        }
                        userShipViews[i].setImageResource(userBoard.getShip(i).getImageId());
                        params.leftMargin = (int)(headCell.getX() * cellSize);
                        params.topMargin = (int)(headCell.getY() * cellSize);
                        userShipViews[i].setLayoutParams(params);
                        userBoardView.addView(userShipViews[i], params);
                        userShipViews[i].setVisibility(View.VISIBLE);
                        ibNext.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.ib_next:
//                    deselectShip();
                    //If all ships are placed on board, send Board and move to GamePlayActivity
                    if(userBoard.areAllShipsPlaced()) {
//                        Intent intent = new Intent(GamePlayActivity.this, GamePlayActivity.class);
//                        intent.putExtra("uBoard", Parcels.wrap(userBoard));
//                        startActivity(intent);
                        //Hide ship objects and then draw them on board with Canvas
                        for(ImageView ship : userShipViews) {
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
        private int id;

        ShipTouchListener(int id) {
            this.id = id;
        }

        @Override
        public boolean onTouch(View touchedShip, MotionEvent touchEvent) {
            int action = touchEvent.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                selectedShip = userBoard.getShip(id);
                ClipData clipData = ClipData.newPlainText("", "");
                View.DragShadowBuilder dragShadowBuilder = new View.DragShadowBuilder(touchedShip);
                touchedShip.startDrag(clipData, dragShadowBuilder, touchedShip, 0);
                touchedShip.setVisibility(View.INVISIBLE);
                ibNext.setVisibility(View.INVISIBLE);
                return true;
            } else {
                return false;
            }
        }
    }

    private class BoardOnDragListener implements View.OnDragListener {

        @Override
        public boolean onDrag(View board, DragEvent event) {
            int action = event.getAction();
            float cellSize = ((BoardView)board).cellSize();
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
                    if(selectedShip.isHorizontal()) { //Horizontal ship
//                        params = new RelativeLayout.LayoutParams((int)cellSize * selectedShip.getSize(), (int)cellSize);
                        cellX = event.getX() - ((float)selectedShip.getSize() / 2.0f - 0.5f) * cellSize;
                        if(cellX < 0) {
                            cellX = 0;
                        }
                        cellY = event.getY();
                    } else { //Vertical ship
//                        params  = new RelativeLayout.LayoutParams((int)cellSize, (int)cellSize * selectedShip.getSize());
                        cellX = event.getX();
                        cellY = event.getY() - ((float)selectedShip.getSize() / 2.0f - 0.5f) * cellSize;
                        if(cellY < 0) {
                            cellY = 0;
                        }
                    }
                    cell = ((BoardView)board).locateCell(cellX, cellY);

                    View view = (View) event.getLocalState();

                    if(cell != null && view != null && user.getBoard().placeShip(selectedShip, cell)) {
                        //Drop the ship on the boardView
                        RelativeLayout.LayoutParams originalParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(originalParams.width, originalParams.height);
                            //Remove original view
                        ViewGroup owner = (ViewGroup) view.getParent();
                        owner.removeView(view);
                            //Add view to boardView
                        params.leftMargin = (int) (cell.getX() * cellSize);
                        params.topMargin = (int) (cell.getY() * cellSize);
                        ((RelativeLayout)board).addView(view, params);
                        view.setVisibility(View.VISIBLE);
                    } else {
                        //Can't place ship
                        //Display warning message
                        displayMessage(getText(R.string.tv_guide_place_failed));
                        //Place ship at original place
                        view.setVisibility(View.VISIBLE);
                    }
                    if(userBoard.areAllShipsPlaced()) {
                        ibNext.setVisibility(View.VISIBLE);
                    }
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    break;
            }
            return true;
        }
    }



}
