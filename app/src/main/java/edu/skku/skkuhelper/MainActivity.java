package edu.skku.skkuhelper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;
import com.instructure.canvasapi.api.ConversationAPI;
import com.instructure.canvasapi.api.CourseAPI;
import com.instructure.canvasapi.api.ToDoAPI;
import com.instructure.canvasapi.model.CanvasError;
import com.instructure.canvasapi.model.Conversation;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.ToDo;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.APIStatusDelegate;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import com.instructure.canvasapi.utilities.ErrorDelegate;
import com.instructure.canvasapi.utilities.LinkHeaders;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import edu.skku.skkuhelper.databinding.ActivityMainBinding;
import retrofit.RetrofitError;
import retrofit.client.Response;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class MainActivity extends AppCompatActivity {
    private String id,pwd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnLogin = findViewById(R.id.buttonLogin);
        EditText editTextToken = findViewById(R.id.editTextPassword);
        CheckBox Auto_LogIn;
        SharedPreferences setting;
        SharedPreferences.Editor editor;
        Auto_LogIn = (CheckBox) findViewById(R.id.check);
        setting = getSharedPreferences("setting", 0);
        editor= setting.edit();

        /*if(setting.getBoolean("Auto_Login_enabled", true)){
            //Auto_LogIn.setChecked(true);
            Intent intent = new Intent(MainActivity.this, Home_page.class);
            startActivity(intent);
        }*/
        if(setting.getString("TOKEN", null) != null) {
            Intent intent = new Intent(MainActivity.this, Home_page.class);
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
                    editor.commit();
                }
                else{
                    editor.clear();
                    editor.commit();
                }
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //login success
                if(editTextToken.getText().toString().equals("123")) {

                }
                else {
                    String token = editTextToken.getText().toString();
                    Intent intent = new Intent(MainActivity.this, Home_page.class);
                    intent.putExtra("TOKEN",token);
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
        NotificationManagerCompat notificationManagercompat = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define
        notificationManagercompat.notify(1, builder.build());
        /************* Example of Notification *************/
    }
}
