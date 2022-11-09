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

public class MainActivity extends AppCompatActivity implements APIStatusDelegate, ErrorDelegate  {
    private String id,pwd;

    EditText editTextToken;
    Button btnLogin;
    /************* Canvas API GLOBAL Variables *************/
    UserCallback userCallback;
    String userId=null;
    String userName=null;
    public static String TOKEN=null;
    public final static String DOMAIN = "https://canvas.skku.edu/";
    boolean isCallbackFinished = false;
    /************* Canvas API GLOBAL Variables *************/
    /************* Room DB GLOBAL Variables *************/
    UserInfoDB userinfoDB = null;
    /************* Room DB GLOBAL Variables *************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnLogin = findViewById(R.id.buttonLogin);
        editTextToken = findViewById(R.id.editTextPassword);
        CheckBox Auto_LogIn;
        SharedPreferences setting;
        SharedPreferences.Editor editor;
        Auto_LogIn = (CheckBox) findViewById(R.id.check);
        setting = getSharedPreferences("setting", 0);
        editor= setting.edit();

        /************* Room DB CREATE START *************/
        userinfoDB = UserInfoDB.getInstance(this);
        UserInfoDB Infodb = Room.databaseBuilder(getApplicationContext(), UserInfoDB.class, "userifo.db").build();
        UserInfoDao userinfoDao = Infodb.UserinfoDao();
        /************* Room DB CREATE END *************/

        /************* Canvas API CREATE START *************/



        userCallback = new UserCallback(MainActivity.this) {

            @Override
            public void cachedUser(User user) {
                APIHelpers.setCacheUser(MainActivity.this, user);
            }

            @Override
            public void user(User user, Response response) {
                userId = user.getLoginId();
                userName = user.getName();

                class InsertRunnable2 implements Runnable {
                    @Override
                    public void run() {
                        UserInfo userinfoTemp = new UserInfo();
                        userinfoTemp.userTOKEN = TOKEN;
                        userinfoTemp.userId = userId;
                        userinfoTemp.userName = userName;
                        userinfoDB.UserinfoDao().insert(userinfoTemp);
                    }
                }

                InsertRunnable2 insertRunnable2 = new InsertRunnable2();
                Thread addThread2 = new Thread(insertRunnable2);
                addThread2.start();



                //login success
                Intent intent = new Intent(MainActivity.this, Home_page.class);
                intent.putExtra("TOKEN", TOKEN);
                startActivity(intent);
//                isCallbackFinished

            }
        };
        /************* Canvas API CREATE END*************/

        if(setting.getString("TOKEN",null) != null) {
            class InsertRunnable implements Runnable {
                @Override
                public void run() {
                    TOKEN = userinfoDao.getTOKEN();
                    Log.d("TOKEN", TOKEN);

                }
            }
            InsertRunnable insertRunnable = new InsertRunnable();
            Thread addThread = new Thread(insertRunnable);
            addThread.start();
            String token = editTextToken.getText().toString();
            Intent intent = new Intent(MainActivity.this, Home_page.class);
            intent.putExtra("TOKEN",token);
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
                if(!TOKEN.equals("")){
                setUpCanvasAPI();
                UserAPI.getSelf(userCallback);}
                else{
                    Toast toast = Toast.makeText(getApplicationContext(),"Please Enter TOKEN",Toast.LENGTH_SHORT);
                    toast.show();
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

    /**
     * This is all stuff that should only need to be called once for the entire project.
     */
    public void setUpCanvasAPI() {
        //Set up the Canvas Rest Adapter.
        CanvasRestAdapter.setupInstance(this, TOKEN, DOMAIN);
        //Set up a default error delegate. This will be the same one for all API calls
        //You can override the default ErrorDelegate in any CanvasCallBack constructor.
        //In a real application, this should probably be a standalone class.
        APIHelpers.setDefaultErrorDelegateClass(this, this.getClass().getName());
    }
    @Override
    public void onCallbackStarted() {
        btnLogin.setEnabled(false);
    }

    @Override
    public void onCallbackFinished(CanvasCallback.SOURCE source) {
        if(userId==null){
            Toast toast = Toast.makeText(getApplicationContext(),"INVALID TOKEN",Toast.LENGTH_SHORT);
            toast.show();
        }
        btnLogin.setEnabled(true);

    }

    @Override
    public void onNoNetwork() {

    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void noNetworkError(RetrofitError retrofitError, Context context) {
        Log.d(APIHelpers.LOG_TAG, "There was no network");

    }

    @Override
    public void notAuthorizedError(RetrofitError retrofitError, CanvasError canvasError, Context context) {
        Log.d(APIHelpers.LOG_TAG, "HTTP 401");

    }

    @Override
    public void invalidUrlError(RetrofitError retrofitError, Context context) {
        Log.d(APIHelpers.LOG_TAG, "HTTP 404");

    }

    @Override
    public void serverError(RetrofitError retrofitError, Context context) {
        Log.d(APIHelpers.LOG_TAG, "HTTP 500");

    }

    @Override
    public void generalError(RetrofitError retrofitError, CanvasError canvasError, Context context) {
        Log.d(APIHelpers.LOG_TAG, "HTTP 200 but something went wrong. Probably a GSON parse error.");

    }
}
