package jp.ac.jjc.battleship;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private final Context mContext = this;
    private Dialog dialog;
    private MediaManager mediaManager;

    SharedPreferences sharedPref;
    public Switch swMusic;
    public Switch swSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        //SharedPreferences to save game settings
        sharedPref = mContext.getSharedPreferences("settings", Context.MODE_PRIVATE);

        //Move to game play activity with start game button
        Button btStartGame = findViewById(R.id.bt_StartGame);
        btStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, GamePlayActivity.class);
                startActivity(intent);
            }
        });

        //Display a dialog showing how to play
        Button btHowToPlay = findViewById(R.id.bt_howToPlay);
        btHowToPlay.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                showHowToPlayDialog();
            }
        });

        //Play background music in all activities with ONE MediaPlayer
        mediaManager = MediaManager.getInstance(this);

        //Enable / Disable background music with switch
        swMusic = findViewById(R.id.sw_music_main);
        swSound = findViewById(R.id.sw_sound_main);
        swMusic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mediaManager.play();
                } else {
                    mediaManager.pause();
                }
            }
        });
        System.out.println("Main onCreate");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Stop music when the game is quit
        mediaManager.releaseMediaPlayer();
        saveSettings();
        System.out.println("Main onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Pause music when the game is hidden
        mediaManager.pause();
        saveSettings();
        System.out.println("Main onPause");
    }

//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        mediaManager.pause();
//        saveSettings();
//    }
//
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
        //Load switch checked status
        swMusic.setChecked(sharedPref.getBoolean("music", false));
        swSound.setChecked(sharedPref.getBoolean("sound", false));
        //Play / Resume music if switch is checked
        if(swMusic.isChecked()) {
            mediaManager.play();
        }
        System.out.println("Main onResume");
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("music", swMusic.isChecked());
        editor.putBoolean("sound", swSound.isChecked());
        editor.apply();
    }

    void showHowToPlayDialog() {
        //Update game play guide

        //Initialize text to display
        String text = getString(R.string.tv_htp_6);
        // Initialize a new SpannableStringBuilder instance
        SpannableStringBuilder ssBuilder = new SpannableStringBuilder(text);
        // Initialize a new ImageSpan to display HIT image
        ImageSpan hitImageSpan = new ImageSpan(mContext,R.drawable.user_hit);
        // Initialize a new ImageSpan to display KILL image
        ImageSpan killImageSpan = new ImageSpan(mContext,R.drawable.ship_01_01);
        // Initialize a new ImageSpan to display MISS image
        ImageSpan missImageSpan = new ImageSpan(mContext,R.drawable.cell_miss);

        // Apply the HIT image to the span
        ssBuilder.setSpan(hitImageSpan,
                          text.indexOf(getString(R.string.span_hit)) + String.valueOf(getString(R.string.span_hit)).length(),  // Start of the span (inclusive)
                          text.indexOf(getString(R.string.span_hit)) + String.valueOf(getString(R.string.span_hit)).length()+1, // End of the span (exclusive)
                          Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Do not extend the span when text add later

        // Apply the KILL image to the span
        ssBuilder.setSpan(killImageSpan,
                text.indexOf(getString(R.string.span_kill)) + String.valueOf(getString(R.string.span_kill)).length(),  // Start of the span (inclusive)
                text.indexOf(getString(R.string.span_kill)) + String.valueOf(getString(R.string.span_kill)).length()+1, // End of the span (exclusive)
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Do not extend the span when text add later

        // Apply the MISS image to the span
        ssBuilder.setSpan(missImageSpan,
                text.indexOf(getString(R.string.span_miss)) + String.valueOf(getString(R.string.span_miss)).length(),  // Start of the span (inclusive)
                text.indexOf(getString(R.string.span_miss)) + String.valueOf(getString(R.string.span_miss)).length()+1, // End of the span (exclusive)
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Do not extend the span when text add later

        dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.how_to_play_dialog);
        dialog.setTitle("Title...");

        ((TextView)dialog.findViewById(R.id.tv_game_play_guide)).setText(ssBuilder);

        //Button to close the How To Play dialog
        Button btClose = (Button) dialog.findViewById(R.id.bt_close);
        btClose.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
//
//        //The dialog can only be canceled by clicking Continue Button
//        dialog.setCanceledOnTouchOutside(false);
    }
}
