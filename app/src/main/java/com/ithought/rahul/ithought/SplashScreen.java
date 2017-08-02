package com.ithought.rahul.ithought;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class SplashScreen extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(SplashScreen.this, LoginActivity.class);
                startActivity(i);

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);



//        if(isNetworkAvailable()) {
//
//            Intent i = new Intent(SplashScreen.this, LoginActivity.class);
//            startActivity(i);
//    /* DO WHATEVER YOU WANT IF INTERNET IS AVAILABLE */
//
//        } else {
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setCancelable(false);
//            builder.setTitle("No Internet");
//            builder.setMessage("It seems like you don't have active internet connection. Please Retry after connecting to internet");
//
//            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                    finish();
//                }
//            });
//
//            builder.setPositiveButton("Retry", new DialogInterface.OnClickListener(){
//                @Override
//                public void onClick(DialogInterface dialog, int which)
//                {
//                    dialog.dismiss();
//                  //  InitiateDownload();
//                }
//            });
//            AlertDialog dialog = builder.create(); // calling builder.create after adding buttons
//            dialog.show();
//            Toast.makeText(this, "Network Unavailable!", Toast.LENGTH_LONG).show();
//        }
    }

//    private boolean isNetworkAvailable() {
//        ConnectivityManager connectivityManager
//                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
//        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
//    }
}
