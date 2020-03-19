package com.ashish2117.user.streammedia.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ashish2117.user.streammedia.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class SplashScreen extends Activity {
    FirebaseAuth mAuth;
    DatabaseReference myRef;
    int noOfPermissionsGranted;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        mAuth=FirebaseAuth.getInstance();
        splashTimeout();
        noOfPermissionsGranted = 0;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 2:
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    noOfPermissionsGranted++;
                    if(noOfPermissionsGranted == 2){
                        startApplication();
                    }
                }
                break;
            case 3:

                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    noOfPermissionsGranted++;
                    if(noOfPermissionsGranted == 2){
                        startApplication();
                    }
                }
                break;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    private void splashTimeout()
    {
        Thread thread=new Thread(){
            @Override
            public void run()
            {
                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    startApplication();
                }
            }
        };
        thread.start();
    }

    private void startApplication(){
        if(mAuth.getCurrentUser()==null)
        {
            Intent intent=new Intent(SplashScreen.this, Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        else
        {
            Intent intent=new Intent(SplashScreen.this, Friends.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        }
    }

}
