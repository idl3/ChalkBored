package com.chalkbored;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

import com.dropbox.sync.android.DbxAccount;
import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;


/**
 * Created by idle on 12/11/13.
 */
public class Splash extends Activity implements View.OnClickListener  {
    private long ms=0;
    private long splashTime=3000;
    private boolean splashActive=true;
    private boolean paused=false;

    static final int REQUEST_LINK_TO_DBX = 0;

    final static private String APP_KEY = "u7uqp9yw4olbhwi";
    final static private String APP_SECRET = "vd0iwxgjys3ks0t";

    private String fileName = "";

    private ImageButton authBtn;

    private Activity activityMain;

    private DbxAccountManager mAccountManager;
    private DbxAccount mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash);
        authBtn = (ImageButton)findViewById(R.id.authBtn);
        authBtn.setOnClickListener(this);

        mAccountManager = DbxAccountManager.getInstance(getApplicationContext(), APP_KEY, APP_SECRET);
        if (mAccountManager.hasLinkedAccount()) {
            authBtn.setVisibility(View.GONE);
            mAccount = mAccountManager.getLinkedAccount();
            // ... Now display your own UI using the linked account information.
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
        }else{
            authBtn.setVisibility(View.VISIBLE);
        }
    }


    public void onClick(View view){
        if(view.getId()==R.id.authBtn){
            System.out.println("test test test test test test test test test");
            mAccountManager.startLink((Activity)this, REQUEST_LINK_TO_DBX);

        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("I'm here baby!!!!!!");
        if (requestCode == REQUEST_LINK_TO_DBX) {
            if (resultCode == Activity.RESULT_OK) {
                mAccount = mAccountManager.getLinkedAccount();
                authBtn.setVisibility(View.GONE);
                // ... Now display your own UI using the linked account information.


                Intent intent = new Intent(Splash.this,MainActivity.class);
                startActivity(intent);
            } else {
                authBtn.setVisibility(View.VISIBLE);
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
