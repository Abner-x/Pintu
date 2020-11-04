package com.example.pintu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void startEasy(View view) {
        Intent main = new Intent(this, MainActivity2.class);
        main.putExtra("Difficulty", 3);
        startActivityForResult(main,1);

    }

    public void startMedium(View view) {
        Intent main = new Intent(this, MainActivity2.class);
        main.putExtra("Difficulty", 4);
        startActivityForResult(main,1);

    }

    public void startHard(View view) {
        Intent main = new Intent(this, MainActivity2.class);
        main.putExtra("Difficulty", 5);
        startActivityForResult(main,1);

    }

}