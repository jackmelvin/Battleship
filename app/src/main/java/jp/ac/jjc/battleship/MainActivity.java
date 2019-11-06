package jp.ac.jjc.battleship;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        ImageButton btStartGame = findViewById(R.id.bt_StartGame);
        btStartGame.setOnClickListener(new Listener());
    }

    public class Listener implements View.OnClickListener {
        @Override
            public void onClick(View view) {
            Intent intent = new Intent(MainActivity.this, PlaceShipActivity.class);
            startActivity(intent);
        }
    }

}
