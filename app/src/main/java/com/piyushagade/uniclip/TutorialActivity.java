package com.piyushagade.uniclip;

import android.annotation.SuppressLint;
import android.os.Vibrator;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;

public class TutorialActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_tutorial);


        //Go next
        ((Button)findViewById(R.id.go_n_to_tut_2)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                (findViewById(R.id.rl_tut_2)).setVisibility(View.VISIBLE);
                (findViewById(R.id.rl_tut_1)).setVisibility(View.GONE);
            }

        });
        ((Button)findViewById(R.id.go_n_to_tut_3)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                (findViewById(R.id.rl_tut_3)).setVisibility(View.VISIBLE);
                (findViewById(R.id.rl_tut_2)).setVisibility(View.GONE);
            }

        });
        ((Button)findViewById(R.id.go_n_to_tut_4)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                (findViewById(R.id.rl_tut_4)).setVisibility(View.VISIBLE);
                (findViewById(R.id.rl_tut_3)).setVisibility(View.GONE);
            }

        });
        ((Button)findViewById(R.id.go_n_to_tut_5)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                (findViewById(R.id.rl_tut_5)).setVisibility(View.VISIBLE);
                (findViewById(R.id.rl_tut_4)).setVisibility(View.GONE);
            }

        });
        ((Button)findViewById(R.id.go_n_to_tut_6)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                (findViewById(R.id.rl_tut_6)).setVisibility(View.VISIBLE);
                (findViewById(R.id.rl_tut_5)).setVisibility(View.GONE);
            }

        });
        ((Button)findViewById(R.id.go_n_to_tut_7)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                (findViewById(R.id.rl_tut_7)).setVisibility(View.VISIBLE);
                (findViewById(R.id.rl_tut_6)).setVisibility(View.GONE);
            }

        });
        ((Button)findViewById(R.id.go_n_to_tut_8)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                (findViewById(R.id.rl_tut_8)).setVisibility(View.VISIBLE);
                (findViewById(R.id.rl_tut_7)).setVisibility(View.GONE);
            }

        });

        // Go prev
        ((Button)findViewById(R.id.go_p_to_tut_1)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                (findViewById(R.id.rl_tut_2)).setVisibility(View.GONE);
                (findViewById(R.id.rl_tut_1)).setVisibility(View.VISIBLE);
            }

        });
        ((Button)findViewById(R.id.go_p_to_tut_2)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                (findViewById(R.id.rl_tut_3)).setVisibility(View.GONE);
                (findViewById(R.id.rl_tut_2)).setVisibility(View.VISIBLE);
            }

        });
        ((Button)findViewById(R.id.go_p_to_tut_3)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                (findViewById(R.id.rl_tut_4)).setVisibility(View.GONE);
                (findViewById(R.id.rl_tut_3)).setVisibility(View.VISIBLE);
            }

        });
        ((Button)findViewById(R.id.go_p_to_tut_4)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                (findViewById(R.id.rl_tut_5)).setVisibility(View.GONE);
                (findViewById(R.id.rl_tut_4)).setVisibility(View.VISIBLE);
            }

        });
        ((Button)findViewById(R.id.go_p_to_tut_5)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                (findViewById(R.id.rl_tut_6)).setVisibility(View.GONE);
                (findViewById(R.id.rl_tut_5)).setVisibility(View.VISIBLE);
            }

        });
        ((Button)findViewById(R.id.go_p_to_tut_6)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                (findViewById(R.id.rl_tut_7)).setVisibility(View.GONE);
                (findViewById(R.id.rl_tut_6)).setVisibility(View.VISIBLE);
            }

        });
        ((Button)findViewById(R.id.go_p_to_tut_7)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                (findViewById(R.id.rl_tut_8)).setVisibility(View.GONE);
                (findViewById(R.id.rl_tut_7)).setVisibility(View.VISIBLE);
            }

        });

        //Close and finish
        ((Button)findViewById(R.id.close_tut)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);finish();
            }

        });
        ((Button)findViewById(R.id.finish_tut)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);finish();
            }

        });

        //Empty listeners
        ((RelativeLayout)findViewById(R.id.rl_tut_1)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Do nothing
            }

        });

        ((RelativeLayout)findViewById(R.id.rl_tut_2)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Do nothing
            }

        });

        ((RelativeLayout)findViewById(R.id.rl_tut_3)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Do nothing
            }

        });

        ((RelativeLayout)findViewById(R.id.rl_tut_4)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Do nothing
            }

        });

        ((RelativeLayout)findViewById(R.id.rl_tut_5)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Do nothing
            }

        });

        ((RelativeLayout)findViewById(R.id.rl_tut_6)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Do nothing
            }

        });

        ((RelativeLayout)findViewById(R.id.rl_tut_7)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Do nothing
            }

        });

        ((RelativeLayout)findViewById(R.id.rl_tut_8)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Do nothing
            }

        });
    }



    //Vibrate method
    private void vibrate(int time){
        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(time);
    }
}
