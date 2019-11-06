package jp.ac.jjc.battleship;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.parceler.Parcels;

public class GamePlayActivity extends AppCompatActivity {
    private static final int SIZE = 10;
    final Context context = this;
    Dialog dialog;
    TextView tvEndGame;
    Button btContinue;
    Button btMainMenu;
    Button btReplay;
    Player user;
    ComPlayer com;
    ImageButton[][] ibUserBoard;
    ImageButton[][] ibComBoard;
    Game game = new Game();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_game_play);

        game = new Game();
        game.setUpGame();
    }

    class Game {
        ImageView ivArrow;
        void setUpGame() {
            ivArrow = findViewById(R.id.iv_arrow);
            ivArrow.setTag("Right");
            user = new Player((Board) Parcels.unwrap(getIntent().getParcelableExtra("uBoard")));

            //Create User and Com Board View
            ibUserBoard = new ImageButton[SIZE][SIZE];
            ibComBoard = new ImageButton[SIZE][SIZE];
            int cellCount = 0;
            for (int x = 0; x < SIZE; x++) {
                for (int y = 0; y < SIZE; y++) {
                    //Re-create User Board View
                    ibUserBoard[x][y] = findViewById(R.id.user_cell_00 + cellCount);
                    if(user.getBoard().getCells()[x][y].hasShip) {
                        ibUserBoard[x][y].setBackgroundResource(user.getBoard().getCells()[x][y].getImgBaseId());
                    } else {
                        ibUserBoard[x][y].setBackgroundColor(Color.TRANSPARENT);
                    }
                    ibUserBoard[x][y].setClickable(false);

                    //Create new Com Board View
                    ibComBoard[x][y] = findViewById(R.id.com_cell_00 + cellCount);
                    ibComBoard[x][y].setBackgroundColor(Color.TRANSPARENT);
//                ibComBoard[x][y].setVisibility(View.INVISIBLE);
                    ibComBoard[x][y].setOnClickListener(new UserShootListener(x, y));

                    //For bt_next Cell id
                    cellCount++;
                }
            }
            com = new ComPlayer(new Board(SIZE), ibComBoard, game);

            //Pause game button
            Button btPause = findViewById(R.id.bt_pause);
            btPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pauseGame();
                }
            });

            //Create a dialog for pausing and ending game
            dialog = new Dialog(context);
            dialog.setContentView(R.layout.custom);
            dialog.setTitle("Title...");

            //End game message
            tvEndGame = (TextView) dialog.findViewById(R.id.tv_endGame);
            tvEndGame.setBackgroundColor(Color.TRANSPARENT);
            //Buttons
                // Continue button, just close the dialog
            btContinue = (Button) dialog.findViewById(R.id.bt_continue);
            btContinue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
                // Main menu button
            btMainMenu = (Button) dialog.findViewById(R.id.bt_mainMenu);
            btMainMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(GamePlayActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            });
                // Replay button
            btReplay = (Button) dialog.findViewById(R.id.bt_replay);
            btReplay.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   Intent intent = new Intent(GamePlayActivity.this, PlaceShipActivity.class);
                   startActivity(intent);
               }
            });
            //The dialog can only be canceled by clicking Continue Button
            dialog.setCanceledOnTouchOutside(false);
        }

        void endGame(Player player) {
            if(player == user) {
                tvEndGame.setText(getString(R.string.tv_endGame_win));
            } else {
                tvEndGame.setText(getString(R.string.tv_endGame_lose));
            }
            btContinue.setVisibility(View.GONE);
            dialog.show();
        }

        void pauseGame() {
            tvEndGame.setVisibility(View.GONE);
            btContinue.setVisibility(View.VISIBLE);
            dialog.show();
        }
        public void changeArrowDir() {
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
    }



    private class UserShootListener implements View.OnClickListener {
        int x, y;
        UserShootListener(int x, int y) {
            this.x = x;
            this.y = y;
        }
        //Get user shoot
        @Override
        public void onClick(View view) {
            System.out.println("User shot: " + x + "," + y);
            ShootResult result = user.shoot(com.getBoard().getCells()[x][y]);
            if(result == ShootResult.HIT) {
                ibComBoard[x][y].setBackgroundResource(R.drawable.user_hit);
            } else {
                if(result == ShootResult.MISS) {
                    ibComBoard[x][y].setBackgroundResource(com.getBoard().getCells()[x][y].getImgBaseId());
                    //To com turn
                    game.changeArrowDir();
                    com.disableBoard(ibComBoard);
                    com.randomlyShoot(user.getBoard(), ibUserBoard);
                } else { //Kill
                    //Show the sunk ship
                    for(Cell cell : com.getBoard().getCells()[x][y].getShip().getPlacedCells()) {
                        int x = cell.getCoord()[0];
                        int y = cell.getCoord()[1];
                        ibComBoard[x][y].setBackgroundResource(cell.getImgBaseId());
                    }
                    //Update surrounded cells
                    for(Cell cell : com.getBoard().getCells()[x][y].getShip().getSurroundCells()) {
                        cell.hit();
                        int x = cell.getCoord()[0];
                        int y = cell.getCoord()[1];
                        ibComBoard[x][y].setBackgroundResource(cell.getImgBaseId());
                    }
                    if(result == ShootResult.END) {
                        //EndGame
                        game.endGame(user);

                    }
                }
            }
            view.setVisibility(View.VISIBLE);
            view.setClickable(false);
        }

    }
}


