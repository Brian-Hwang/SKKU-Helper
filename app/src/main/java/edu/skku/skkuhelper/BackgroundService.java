package edu.skku.skkuhelper;

import static android.app.PendingIntent.FLAG_IMMUTABLE;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.os.*;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.Lifecycle;
import androidx.room.Room;

import com.instructure.canvasapi.api.CourseAPI;
import com.instructure.canvasapi.api.ToDoAPI;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.CanvasError;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.ToDo;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.APIStatusDelegate;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import com.instructure.canvasapi.utilities.ErrorDelegate;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import edu.skku.skkuhelper.roomdb.SKKUAssignment;
import edu.skku.skkuhelper.roomdb.SKKUAssignmentDB;
//import edu.skku.skkuhelper.roomdb.UserinfoDB;


import edu.skku.skkuhelper.roomdb.SKKUNotice;
import edu.skku.skkuhelper.roomdb.SKKUNoticeDB;
import edu.skku.skkuhelper.roomdb.SKKUNoticeDao;
import edu.skku.skkuhelper.roomdb.SKKUAssignment;
import edu.skku.skkuhelper.roomdb.SKKUAssignmentDB;
import edu.skku.skkuhelper.roomdb.SKKUNotice;
import edu.skku.skkuhelper.roomdb.SKKUNoticeDB;
import edu.skku.skkuhelper.roomdb.SKKUNoticeDao;
import edu.skku.skkuhelper.roomdb.UserInfoDB;
import edu.skku.skkuhelper.roomdb.UserInfoDao;
import retrofit.RetrofitError;
import retrofit.client.Response;
//import edu.skku.skkuhelper.roomdb.Userinfo;


public class BackgroundService extends Service implements APIStatusDelegate, ErrorDelegate {

    //변수
    public final static String DOMAIN = "https://canvas.skku.edu/";
    public static String TOKEN = null;

    LinkedHashMap<Long, String> courseList = new LinkedHashMap<>();
    ArrayList<todoClass> todolist = new ArrayList<>();
    LinkedHashMap<Long, Long> assignmentidList = new LinkedHashMap<>();

    CanvasCallback<Course[]> courseCanvasCallback;
    CanvasCallback<ToDo[]> todosCanvasCallback;
    //

    public Context context = this;
    public Handler handler = null;
    public static Runnable runnable = null;
    private String id, pwd;
    boolean isNukeFinished = false;
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    /************* Room DB GLOBAL Variables *************/
    SKKUAssignmentDB SKKUassignmentDB = null;
    UserInfoDB userinfoDB = null;
    SKKUNoticeDB SKKUnoticeDB = null;   //notice_room

    /************* Room DB GLOBAL Variables *************/
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public void checkAlarm() {
        List<SKKUAssignment> assignments = SKKUassignmentDB.SKKUassignmentDao().getAll();
        Date currentDate = new Date();
        for (int i = 1; i < assignments.size()+1; i++) {
            SKKUAssignment tmp = assignments.get(i-1);
            Date dueDate = tmp.dueDate;
            String assignmentName = tmp.assignmentName;
            String courseName = tmp.courseName;
            long alarmType = tmp.isAlarm;
            long diff = (dueDate.getTime() - currentDate.getTime()) / (60 * 60 * 1000);
            if (alarmType == 1 && diff < 6) {
                sendAlarm(assignmentName, courseName + ": " + String.valueOf(diff) + " hours left", i);
                tmp.isAlarm = 0;
                SKKUassignmentDB.SKKUassignmentDao().update(tmp);
            } else if (alarmType == 2 && diff < 24) {
                sendAlarm(assignmentName, courseName + ": " + String.valueOf(diff) + " hours left", i);
                tmp.isAlarm = 0;
                SKKUassignmentDB.SKKUassignmentDao().update(tmp);
            } else if (alarmType == 3 && diff < 48) {
                sendAlarm(assignmentName, courseName + ": " + String.valueOf(diff) + " hours left", i);
                tmp.isAlarm = 0;
                SKKUassignmentDB.SKKUassignmentDao().update(tmp);
            }
            /****테스트용****/
//            if (alarmType!=0){
//                Log.d("alarmtype",String.valueOf(alarmType));
//                Log.d("alarmtype",String.valueOf(assignments.get(i).isAlarm));
//                sendAlarm(assignmentName,courseName+": "+String.valueOf(diff)+" hours left",i);
//                tmp.isAlarm=0;
//                SKKUassignmentDB.SKKUassignmentDao().update(tmp);
//                List<SKKUAssignment> updated=SKKUassignmentDB.SKKUassignmentDao().getAll();
//                for (int j=0;j<updated.size();j++){
//                    Log.d("isAlarm",String.valueOf(updated.get(j).isAlarm));
//                }
//            }
            /****테스트용****/
        }
    }

    public void sendAlarm(String title, String contents, int id) {
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
        //Log.d("alarm", title);
    }
    //

    public void getJson() {
        Log.d("servertest","asdf");
        try {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("http://13.124.68.141:8000/notice/get_all" +
                            "?student_id=2022310000&tag_num=0&type=0")
                    .addHeader("auth", "myAuth")
                    .addHeader("Content-Type", "application/json")
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    Log.d("SERVERTEST", request.toString());
                    Log.d("SERVERTEST", e.toString());


                }

                //
                public void LogLineBreak(String str) {
                    if (str.length() > 3000) {    // 텍스트가 3000자 이상이 넘어가면 줄
                        Log.d("servertest", str.substring(0, 3000));
                        LogLineBreak(str.substring(3000));
                    } else {
                        Log.e("servertest", str);
                    }
                }
                //

                @Override
                public void onResponse(com.squareup.okhttp.Response response) throws IOException {
                    String rbd = response.body().string();

                    //LogLineBreak(rbd);
                    //LogLineBreak(response.body().string());
                    //Log.d("SERVERTEST", response.body().string());
                    new Handler(getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            try {

                                JSONObject jsonObject = new JSONObject(rbd);
                                //JSONObject jsonObject = new JSONObject(response.body().string());
                                JSONArray jsonArray = jsonObject.getJSONArray("data");
                                Log.d("servertest",String.valueOf(jsonArray.length()));
                                for (int i=0; i<jsonArray.length();i++) {
                                    SKKUNotice noticetemp = new SKKUNotice();
                                    JSONObject jsobj = jsonArray.getJSONObject(i);
                                    noticetemp.noticeId = jsobj.getLong("id_server_notice");
                                    //String idstr = String.valueOf(noticeid);
                                    //Log.d("servertest", "this is data whose id is "+idstr+" and index is "+String.valueOf(i));
                                    SKKUNotice noticebefore = SKKUnoticeDB.SKKUnoticeDao().getById(noticetemp.noticeId);
                                    //기존아이디가 있으면 체크넘버 0으로 갱신
                                    if(noticebefore!=null) {
                                        //noticebefore.check = 0;
                                        SKKUnoticeDB.SKKUnoticeDao().update(noticebefore);

                                    } else { //없으면 체크넘버 1로 받음

                                        noticetemp.title = jsobj.getString("title");
                                        noticetemp.sum = jsobj.getString("summary");
                                        noticetemp.tag = jsobj.getInt("tag_num");
                                        noticetemp.watch = jsobj.getInt("watch");
                                        noticetemp.writer = jsobj.getString("writer");
                                        noticetemp.link = jsobj.getString("link");
                                        noticetemp.check = 1;
                                        LogLineBreak(noticetemp.title); //test용
                                        Log.d("severtest",String.valueOf(noticetemp.check)); //test용
                                        noticetemp.date = formatter.parse(jsobj.getString("date")); //실제 사용할 date
                                        SKKUnoticeDB.SKKUnoticeDao().insert(noticetemp);


                                    }





                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    });


                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkNotice() {
        List<SKKUNotice> notices = SKKUnoticeDB.SKKUnoticeDao().getAll();
        Log.d("asdf",String.valueOf(notices.size()));
        int cnt=0;
        for (int i = 0; i < notices.size(); i++) {
            SKKUNotice tmp = notices.get(i);
            int alarmType = tmp.check;
            if (alarmType == 1) {
                cnt++;
                tmp.check = 0;
                SKKUnoticeDB.SKKUnoticeDao().update(tmp);
            }
        }
        if(cnt!=0) {
            sendNoticeAlarm("새로운 공지가 " + String.valueOf(cnt) + "개 있습니다.");
        }
    }

    //
    /**** 공지용추가중 ****/
    //title=("New Notice!"), String contents="새로운 공지가 "+신규공지개수+"개 있습니다.", id=0;
    //사용법 sendNoticeAlarm(assignmentName, courseName + ": " + String.valueOf(diff) + " hours left", i);
    public void sendNoticeAlarm(String contents) {
        /************* Notification builder creation for Notification *************/
        // Create an explicit intent for an Activity in your app
        //TODO change Intent activity to class specificaiton
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, String.valueOf(R.string.CHANNEL_ID))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("New Notice!")
                .setContentText(contents)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);
        /************* Notification builder creation for Notification *************/


        /************* Example of Notification *************/
        NotificationManagerCompat notificationManagercompat = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define
        notificationManagercompat.notify(0, builder.build());
        /************* Example of Notification *************/
        //Log.d("alarm", title);
    }
    /***** 추가중 *****/

    public void getbeforeAssignments() {

        class InsertRunnable implements Runnable {
            @Override
            public void run() {
                List<SKKUAssignment> listTemp = SKKUassignmentDB.SKKUassignmentDao().getAll();
                for (SKKUAssignment temp : listTemp) {
                    assignmentidList.put(temp.assignmentId, temp.isAlarm);
                }
                Log.d("asdf","put");
//                SKKUassignmentDB.SKKUassignmentDao().nukeTable();

//                    if(SKKUassignmentDB.SKKUassignmentDao().getRowCount()==0) {
//                        isNukeFinished = true;
//                        break;
//                    }
                SKKUassignmentDB.SKKUassignmentDao().nukeTable();
                while(true){
                    if(SKKUassignmentDB.SKKUassignmentDao().getRowCount()==0) {
                        isNukeFinished = true;
                        Log.d("asdf","nuke");
                        break;
                    }
                }

            }
        }
        InsertRunnable insertRunnable = new InsertRunnable();
        Thread addThread = new Thread(insertRunnable);
        addThread.start();
//        try {
//            addThread.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    public void getAssignments() {
        courseCanvasCallback = new CanvasCallback<Course[]>(this) {
            @Override
            public void cache(Course[] courses) {
            }

            @Override
            public void firstPage(Course[] courses, LinkHeaders linkHeaders, Response response) {
                for (Course course : courses) {
                    courseList.put(course.getId(), course.getName());
                }
            }
        };

        todosCanvasCallback = new CanvasCallback<ToDo[]>(this) {
            @Override
            public void cache(ToDo[] courses) {
            }

            @Override
            public void firstPage(ToDo[] todos, LinkHeaders linkHeaders, Response response) {

                for (ToDo todo : todos) {
                    while (true) {
                        if (courseList.size() != 0) break;
                    }
                    Date today = new Date();
                    if (todo.getAssignment().getPointsPossible() == 0.0f || todo.getAssignment().getDueDate().before(today))
                        continue;
                    todoClass todoTemp = new todoClass();
                    todoTemp.isLecture = todo.getAssignment().getTurnInType() == Assignment.TURN_IN_TYPE.EXTERNAL_TOOL;
                    todoTemp.assignmentName = todo.getAssignment().getName();
                    todoTemp.courseName = courseList.get(todo.getAssignment().getCourseId());
                    todoTemp.assignmentId = todo.getAssignment().getId();
                    todoTemp.courseId = todo.getAssignment().getCourseId();
                    todoTemp.dueDate = todo.getDueDate();
                    todoTemp.url = todo.getHtmlUrl();
                    todolist.add(todoTemp);
                }
                //Log.d("size confirm", todolist.size() + "");
            }
        };
        CourseAPI.getFirstPageFavoriteCourses(courseCanvasCallback);
        ToDoAPI.getUserTodos(todosCanvasCallback);
    }





    @Override
    public void onCreate() {
        ///Toast.makeText(this, "Service created!", Toast.LENGTH_LONG).show();
        //
        handler = new Handler();
        Log.d("asdf","background?");
        runnable = new Runnable() {
            public void run() {
                /************* Put functions here *************/
                //Toast.makeText(context, "Service is still running", Toast.LENGTH_LONG).show();
                getbeforeAssignments();
                while(true){
                    if (isNukeFinished){
                        Log.d("asdf","TRUE");
                        getAssignments();
                        break;
                    }
                }
                Log.d("asdf","TRUE");
                getJson();
                checkNotice();
                //checkAlarm();
                /************* Put functions here *************/
                handler.postDelayed(runnable, 3600000);    //min*60000, 1 hr=>3600000
            }
        };
        handler.postDelayed(runnable, 3600000);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(runnable);
        //Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
        //Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show();
        Log.d("asdfg","bg");
        startForegroundService();
        /************* Room DB CREATE START *************/
        SKKUassignmentDB = SKKUAssignmentDB.getInstance(this);
        userinfoDB = UserInfoDB.getInstance(this);
        SKKUnoticeDB = SKKUNoticeDB.getInstance(this);  //notice_room
        //
        UserInfoDB db = Room.databaseBuilder(getApplicationContext(), UserInfoDB.class, "userifo.db").build();
        UserInfoDao userInfoDao = db.UserinfoDao();

        /************* Room DB CREATE END *************/
        /************* Canvas API CREATE START *************/
        class InsertRunnable implements Runnable {
            @Override
            public void run() {
                TOKEN = userInfoDao.getTOKEN();
            }
        }
        InsertRunnable insertRunnable = new InsertRunnable();
        Thread addThread = new Thread(insertRunnable);
        addThread.start();

        while (true) {
            if (TOKEN != null) break;
        }
        setUpCanvasAPI();
        /************* Canvas API CREATE END*************/
        //return super.onStartCommand(intent,flags,startid);
        //return Service.START_REDELIVER_INTENT;
        return START_STICKY;
    }


    //
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

    }

    @Override
    public void onCallbackFinished(CanvasCallback.SOURCE source) {
        class InsertRunnable implements Runnable {
            @Override
            public void run() {
                for (todoClass todos : todolist) {
                    Log.d("TODO LIST : ", String.valueOf(todos.assignmentName) + String.valueOf(todos.courseName) + String.valueOf(todos.assignmentId) + String.valueOf(todos.courseId) + String.valueOf(todos.isLecture) + String.valueOf(todos.dueDate) + String.valueOf(todos.url));
                    SKKUAssignment todoTemp = new SKKUAssignment();
                    if (assignmentidList.containsKey(todos.assignmentId))
                        todoTemp.isAlarm = assignmentidList.get(todos.assignmentId);
                    else
                        todoTemp.isAlarm = 3;

                    todoTemp.isLecture = todos.isLecture;
                    todoTemp.assignmentName = todos.assignmentName;
                    todoTemp.courseName = todos.courseName;
                    todoTemp.assignmentId = todos.assignmentId;
                    todoTemp.courseId = todos.courseId;
                    todoTemp.dueDate = todos.dueDate;
                    todoTemp.url = todos.url;
                    SKKUassignmentDB.SKKUassignmentDao().insert(todoTemp);
                }
                todolist.clear();
            }
        }
        InsertRunnable insertRunnable = new InsertRunnable();
        Thread addThread = new Thread(insertRunnable);
        addThread.start();
        try {
            addThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        checkAlarm();//
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
    //
    private void startForegroundService() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("SKKU-Helper");
        builder.setContentText("Service providing");
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(new NotificationChannel("default", "기본 채널",NotificationManager.IMPORTANCE_DEFAULT));
        }
        startForeground(1, builder.build());
    }

}