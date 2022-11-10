package edu.skku.skkuhelper;

import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.os.*;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Date;
import java.util.List;

import edu.skku.skkuhelper.roomdb.SKKUAssignment;
import edu.skku.skkuhelper.roomdb.SKKUAssignmentDB;
//import edu.skku.skkuhelper.roomdb.UserinfoDB;

import edu.skku.skkuhelper.roomdb.SKKUAssignment;
import edu.skku.skkuhelper.roomdb.SKKUAssignmentDB;
//import edu.skku.skkuhelper.roomdb.Userinfo;
//import edu.skku.skkuhelper.roomdb.UserinfoDB;


public class BackgroundService extends Service {

    public Context context = this;
    public Handler handler = null;
    public static Runnable runnable = null;
    private String id, pwd;                         //log-in info
    /************* Room DB GLOBAL Variables *************/
    SKKUAssignmentDB SKKUassignmentDB = null;
    //UserinfoDB userinfoDB = null;
    /************* Room DB GLOBAL Variables *************/
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public void checkAlarm(){
        List<SKKUAssignment> assignments=SKKUassignmentDB.SKKUassignmentDao().getAll();
        List<SKKUAssignment> toSend;
        Date currentDate = new Date();
        for(int i=0;i<assignments.size();i++){
            SKKUAssignment tmp=assignments.get(i);
            Date dueDate=tmp.dueDate;
            String assignmentName=tmp.assignmentName;
            String courseName=tmp.courseName;
            long alarmType=tmp.isAlarm;
            long diff=(dueDate.getTime()-currentDate.getTime())/(60*60*1000);
            if(alarmType==1 && diff<6){
                sendAlarm(assignmentName,courseName+": "+String.valueOf(diff)+" hours left",i);
                tmp.isAlarm=0;
                SKKUassignmentDB.SKKUassignmentDao().update(tmp);
            }
            else if (alarmType==2 && diff<24){
                sendAlarm(assignmentName,courseName+": "+String.valueOf(diff)+" hours left",i);
                tmp.isAlarm=0;
                SKKUassignmentDB.SKKUassignmentDao().update(tmp);
            }
            else if (alarmType==3 && diff<24){
                sendAlarm(assignmentName,courseName+": "+String.valueOf(diff)+" hours left",i);
                tmp.isAlarm=0;
                SKKUassignmentDB.SKKUassignmentDao().update(tmp);
            }
            /****테스트용****/
//            else{
//                sendAlarm(assignmentName,courseName+": "+String.valueOf(diff)+" hours left",i);
//                tmp.isAlarm=0;
//                SKKUassignmentDB.SKKUassignmentDao().update(tmp);
//            }
            /****테스트용****/
        }
    }

    public void sendAlarm(String title, String contents, int id){
        /************* Notification builder creation for Notification *************/
        // Create an explicit intent for an Activity in your app
        //TODO change Intent activity to class specificaiton
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, String.valueOf(R.string.CHANNEL_ID))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(contents)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);
        /************* Notification builder creation for Notification *************/


        /************* Example of Notification *************/
        NotificationManagerCompat notificationManagercompat = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define
        notificationManagercompat.notify(id, builder.build());
        /************* Example of Notification *************/
        Log.d("alarm",title);
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, "Service created!", Toast.LENGTH_LONG).show();
        /************* Room DB CREATE START *************/
        SKKUassignmentDB = SKKUAssignmentDB.getInstance(this);
        //userinfoDB = userinfoDB.getInstance(this);
        /************* Room DB CREATE END *************/
        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                /************* Put functions here *************/
                /* Crawling iCampus & Get Notice Info -> Store data */
                Toast.makeText(context, "Service is still running", Toast.LENGTH_LONG).show();
                /************* Put functions here *************/
                checkAlarm();
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