package edu.skku.skkuhelper;

import static java.lang.Thread.sleep;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.core.app.NotificationCompat;
import androidx.room.Room;

import android.widget.Button;
import android.widget.Toast;
import android.content.SharedPreferences;

import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.CompoundButton;


import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.skku.skkuhelper.roomdb.SKKUNotice;
import edu.skku.skkuhelper.roomdb.SKKUNoticeDB;
import edu.skku.skkuhelper.roomdb.SKKUNoticeDao;

public class MainActivity extends AppCompatActivity {
    private String id, pwd;

    EditText editTextToken;
    Button btnLogin;
    /************* Canvas API GLOBAL Variables *************/
    public static String TOKEN = null;
    /************* Canvas API GLOBAL Variables *************/
    /************* SERVER API GLOBAL Variables *************/
    String SB = "12";

    /************* SERVER API GLOBAL Variables *************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("SERVERTEST", SB);


        btnLogin = findViewById(R.id.buttonLogin);
        editTextToken = findViewById(R.id.editTextPassword);
        CheckBox Auto_LogIn;
        SharedPreferences setting;
        SharedPreferences.Editor editor;
        Auto_LogIn = (CheckBox) findViewById(R.id.check);
        setting = getSharedPreferences("setting", 0);
        editor = setting.edit();
//        getJson();
        if (setting.getString("TOKEN", null) != null) {
            Intent intent = new Intent(MainActivity.this, Home_page.class);
            intent.putExtra("TOKEN", setting.getString("TOKEN", ""));
            startActivity(intent);
        }


        Auto_LogIn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    String TOKEN = editTextToken.getText().toString();
                    editor.putString("TOKEN", TOKEN);
                    editor.apply();
                } else {
                    editor.clear();
                    editor.apply();
                }
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TOKEN = editTextToken.getText().toString();
                //Set up CanvasAPI
                if (TOKEN.equals("")) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Please Enter TOKEN", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    Intent intent = new Intent(MainActivity.this, Home_page.class);
                    intent.putExtra("TOKEN", setting.getString("TOKEN", ""));
                    startActivity(intent);
                }

            }
        });

        /************* Channel creation for Notification START *************/
        CharSequence name = getString(R.string.channel_name);
        String description = getString(R.string.channel_description);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(String.valueOf(R.string.CHANNEL_ID), name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        /************* Channel creation for Notification END *************/
//        /************* Example of Background Service *************/
//        /* service start */
//        Intent startIntent = new Intent(this, BackgroundService.class);
//        id="testId";
//        pwd="testPwd";
//        startIntent.putExtra("id",id);
//        startIntent.putExtra("pwd",pwd);
//        startService(startIntent);
//        /* service terminate */
//        /* how to quit background service */
//        /*testBtn.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this,BackgroundService.class);
//                stopService(intent);
//            }
//        });*/
//        /************* Example of Background Service *************/

        /************* Example of Background Service *************/
        /* service start */
//        Intent startIntent = new Intent(this, BackgroundService.class);
//        id="testId";
//        pwd="testPwd";
//        startIntent.putExtra("id",id);
//        startIntent.putExtra("pwd",pwd);
//        startService(startIntent);
        /* service terminate */
        /* how to quit background service */
        /*testBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,BackgroundService.class);
                stopService(intent);
            }
        });*/
        /************* Example of Background Service *************/

        /************* Notification builder creation for Notification *************/
        // Create an explicit intent for an Activity in your app
        //TODO change Intent activity to class specificaiton
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, String.valueOf(R.string.CHANNEL_ID))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("ICAMPUS NOTIFICATION")
                .setContentText("Software Engineering lectrue due 10.03")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);
        /************* Notification builder creation for Notification *************/
    }

}
