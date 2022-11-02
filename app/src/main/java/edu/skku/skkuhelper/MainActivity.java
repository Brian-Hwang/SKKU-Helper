package edu.skku.skkuhelper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    public static final String EXT_Token = "token";
    private String id,pwd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnLogin = findViewById(R.id.buttonLogin);
        EditText editTextToken = findViewById(R.id.editTextPassword);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //로그인 성공하여 메인페이지로 이동
                String token = editTextToken.getText().toString();
                Intent intent = new Intent(MainActivity.this, Home_page.class);
                startActivity(intent);
            }
        });
        /************* Channel creation for Notification *************/
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(String.valueOf(R.string.CHANNEL_ID), name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        /************* Channel creation for Notification *************/


        /************* Example of Background Service *************/
        /* service start */
        Intent startIntent = new Intent(this, BackgroundService.class);
        id="testId";
        pwd="testPwd";
        startIntent.putExtra("id",id);
        startIntent.putExtra("pwd",pwd);
        startService(startIntent);
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


        /************* Example of Notification *************/
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(001, builder.build());
        /************* Example of Notification *************/
    }
}