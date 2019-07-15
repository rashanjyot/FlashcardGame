package com.rashanjyot.flashcardgame.Activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.rashanjyot.flashcardgame.GlobalApplication;
import com.rashanjyot.flashcardgame.R;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_TIME_OUT = 15000;
    private static final int CHECK_INTERVAL = 300;
    private static int count=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ((GlobalApplication)getApplication()).init(this);

        setupHandler();
    }

    public void setupHandler()
    {
        if(count<SPLASH_TIME_OUT)
        {
            count+=CHECK_INTERVAL;
            new Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            if(GlobalApplication.getCurrentUser()!=null)
                            {
                                Intent i = new Intent(SplashActivity.this, HomeNavActivity.class);
                                startActivity(i);
                                finish();
                            }
                            else
                            {
                                setupHandler();
                            }
                        }
                    },
                    CHECK_INTERVAL);
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Timed out while loading data for user, please restart",Toast.LENGTH_LONG).show();
            SplashActivity.this.finish();
        }
    }
}
