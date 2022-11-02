package edu.skku.skkuhelper;

import android.app.Service;
import android.content.*;
import android.os.*;
import android.util.Log;
import android.widget.Toast;

public class BackgroundService extends Service {

    public Context context = this;
    public Handler handler = null;
    public static Runnable runnable = null;
    private String id, pwd;                         //log-in info

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, "Service created!", Toast.LENGTH_LONG).show();

        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                /************* Put functions here *************/
                /* Crawling iCampus & Get Notice Info -> Store data */
                Log.d("Periodic Background Test","Id: "+id+", Pwd: "+pwd);
                Toast.makeText(context, "Service is still running", Toast.LENGTH_LONG).show();
                /************* Put functions here *************/
                handler.postDelayed(runnable, 5000);    //min*60000
            }
        };
        handler.postDelayed(runnable, 5000);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(runnable);
        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
        Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show();
        id=intent.getStringExtra("id");
        pwd=intent.getStringExtra("pwd");
        //return super.onStartCommand(intent,flags,startid);
        return Service.START_REDELIVER_INTENT;
    }
}