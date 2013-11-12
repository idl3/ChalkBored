package com.chalkbored;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

/**
 * Created by idle on 12/11/13.
 */
public class Splash extends Activity {
    private long ms=0;
    private long splashTime=2000;
    private boolean splashActive=true;
    private boolean paused=false;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash);
        Thread mythread = new Thread(){
            public void run(){
                try{
                    while(splashActive && ms < splashTime){
                        if(!paused)
                            ms=ms+100;
                        sleep(100);
                    }

                }catch(Exception e){}
                finally{
                    Intent intent = new Intent(Splash.this,MainActivity.class);
                    startActivity(intent);
                }
            }
        };
        mythread.start();
    }
}
