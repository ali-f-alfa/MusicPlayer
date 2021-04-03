package com.example.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class songActivity extends AppCompatActivity {

    public static MainActivity Main;
    public Button playBtn2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        Main = MainActivity.GetMainActivity();
        playBtn2 = (Button) findViewById(R.id.playBtn2);
    }
    public void PlayButtonClick(View view) {
//        Main.PlayButtonClick(view);
        LinearLayout ll = (LinearLayout) findViewById(R.id.CON);
        ll.addView(Main.inputbox);
    }

    public void PrevButtonClick(View view){
        Main.PrevButtonClick(view);
    }

}