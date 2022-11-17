package edu.skku.skkuhelper;

import static java.lang.Thread.sleep;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.instructure.canvasapi.api.UserAPI;
import com.instructure.canvasapi.model.CanvasError;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.APIStatusDelegate;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import com.instructure.canvasapi.utilities.ErrorDelegate;
import com.instructure.canvasapi.utilities.UserCallback;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.room.Room;

import edu.skku.skkuhelper.roomdb.SKKUAssignmentDB;
import edu.skku.skkuhelper.roomdb.UserInfo;
import edu.skku.skkuhelper.roomdb.UserInfoDB;
import edu.skku.skkuhelper.roomdb.UserInfoDao;
import retrofit.RetrofitError;
import retrofit.client.Response;

import android.widget.Button;
import android.widget.Toast;
import android.content.SharedPreferences;

import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class MainActivity extends AppCompatActivity{
    private String id,pwd;

    EditText editTextToken;
    Button btnLogin;

    public static String TOKEN=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        stopService(new Intent(MainActivity.this,BackgroundService.class));

        btnLogin = findViewById(R.id.buttonLogin);
        editTextToken = findViewById(R.id.editTextPassword);
        CheckBox Auto_LogIn;
        SharedPreferences setting;
        SharedPreferences.Editor editor;
        Auto_LogIn = (CheckBox) findViewById(R.id.check);
        setting = getSharedPreferences("setting", 0);
        editor= setting.edit();

        if(setting.getString("TOKEN",null) != null) {

            Intent intent = new Intent(MainActivity.this, Home_page.class);
            intent.putExtra("TOKEN",setting.getString("TOKEN",""));
            startActivity(intent);
        }


        Auto_LogIn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if(isChecked){

                    String TOKEN = editTextToken.getText().toString();
                    editor.putString("TOKEN", TOKEN);
                   // editor.putBoolean("Auto_Login_enabled", true);
                    editor.apply();
                }
                else{
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
                    Log.d("asdf","main");
                    Intent intent = new Intent(MainActivity.this, Home_page.class);
                    intent.putExtra("TOKEN", editTextToken.getText().toString());
                    startActivity(intent);
                    finish();
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




//        /************* Notification builder creation for Notification *************/
//        // Create an explicit intent for an Activity in your app
//        //TODO change Intent activity to class specificaiton
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        /*NotificationCompat.Builder builder = new NotificationCompat.Builder(this, String.valueOf(R.string.CHANNEL_ID))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("ICAMPUS NOTIFICATION")
                .setContentText("Software Engineering lectrue due 10.03")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);*/
        /************* Notification builder creation for Notification *************/


        /************* Example of Notification *************/
        /*NotificationManagerCompat notificationManagercompat = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define
        notificationManagercompat.notify(1, builder.build());*/
        /************* Example of Notification *************/
    }
}
