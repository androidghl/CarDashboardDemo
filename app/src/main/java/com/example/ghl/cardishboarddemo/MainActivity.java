package com.example.ghl.cardishboarddemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {


    private CarDashboardView mCarDashboardView;
    private Button speed_up;
    private Button speed_down;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCarDashboardView = (CarDashboardView) findViewById(R.id.mCarDashboardView);
        speed_up = (Button) findViewById(R.id.speed_up);
        speed_down = (Button) findViewById(R.id.speed_down);

        setListener();

    }

    private void setListener() {
        //设置监听
        speed_up.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //按下的时候加速
                        mCarDashboardView.setType(1);
                        break;
                    case MotionEvent.ACTION_UP:
                        //松开做自然减速
                        mCarDashboardView.setType(0);
                        break;
                }
                return true;
            }
        });
        speed_down.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //按下的时候减速
                        mCarDashboardView.setType(2);
                        break;
                    case MotionEvent.ACTION_UP:
                        //松开做自然减速
                        mCarDashboardView.setType(0);
                        break;
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCarDashboardView != null) {
            mCarDashboardView.setVelocity(0);
            mCarDashboardView.setStart(true);
        }
        new Thread(mCarDashboardView).start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mCarDashboardView != null) {
            mCarDashboardView.setVelocity(0);
            mCarDashboardView.setStart(false);
        }
    }
}
