package edu.skku.skkuhelper;

import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.os.*;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import edu.skku.skkuhelper.roomdb.SKKUAssignment;
import edu.skku.skkuhelper.roomdb.SKKUAssignmentDB;
//import edu.skku.skkuhelper.roomdb.UserinfoDB;

import edu.skku.skkuhelper.roomdb.SKKUAssignment;
import edu.skku.skkuhelper.roomdb.SKKUAssignmentDB;
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
    private String id, pwd;                         //log-in info
    boolean isNukeFinished = false;
    /************* Room DB GLOBAL Variables *************/
    SKKUAssignmentDB SKKUassignmentDB = null;
    UserInfoDB userinfoDB = null;

    /************* Room DB GLOBAL Variables *************/
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public void checkAlarm() {
        List<SKKUAssignment> assignments = SKKUassignmentDB.SKKUassignmentDao().getAll();
        Date currentDate = new Date();
        for (int i = 0; i < assignments.size(); i++) {
            SKKUAssignment tmp = assignments.get(i);
            Date dueDate = tmp.dueDate;
            String assignmentName = tmp.assignmentName;
            String courseName = tmp.courseName;
            long alarmType = tmp.isAlarm;
            long diff = (dueDate.getTime() - currentDate.getTime()) / (60 * 60 * 1000);
            if (alarmType == 1 && diff < 6) {
                sendAlarm(assignmentName, courseName + ": " + String.valueOf(diff) + " hours left", i);
                tmp.isAlarm = 0;
                SKKUassignmentDB.SKKUassignmentDao().update(tmp);
            } else if (alarmType == 2 && diff < 12) {
                sendAlarm(assignmentName, courseName + ": " + String.valueOf(diff) + " hours left", i);
                tmp.isAlarm = 0;
                SKKUassignmentDB.SKKUassignmentDao().update(tmp);
            } else if (alarmType == 3 && diff < 24) {
                sendAlarm(assignmentName, courseName + ": " + String.valueOf(diff) + " hours left", i);
                tmp.isAlarm = 0;
                SKKUassignmentDB.SKKUassignmentDao().update(tmp);
            }
            /****테스트용****/
//            else if (alarmType!=0){
//                sendAlarm(assignmentName,courseName+": "+String.valueOf(diff)+" hours left",i);
//                tmp.isAlarm=0;
//                SKKUassignmentDB.SKKUassignmentDao().update(tmp);
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
        Log.d("alarm", title);
    }

    public void getbeforeAssignments() {

        class InsertRunnable implements Runnable {
            @Override
            public void run() {
                List<SKKUAssignment> listTemp = SKKUassignmentDB.SKKUassignmentDao().getAll();
                for (SKKUAssignment temp : listTemp) {
                    assignmentidList.put(temp.assignmentId, temp.isAlarm);
                }
                SKKUassignmentDB.SKKUassignmentDao().nukeTable();
                while(true){
                    if(SKKUassignmentDB.SKKUassignmentDao().getRowCount()==0) {
                        isNukeFinished = true;
                        break;
                    }
                }

            }
        }
        InsertRunnable insertRunnable = new InsertRunnable();
        Thread addThread = new Thread(insertRunnable);
        addThread.start();

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
                Log.d("size confirm", todolist.size() + "");
            }
        };
        CourseAPI.getFirstPageFavoriteCourses(courseCanvasCallback);
        ToDoAPI.getUserTodos(todosCanvasCallback);
    }


    @Override
    public void onCreate() {
        ///Toast.makeText(this, "Service created!", Toast.LENGTH_LONG).show();
        /************* Room DB CREATE START *************/
        SKKUassignmentDB = SKKUAssignmentDB.getInstance(this);
        userinfoDB = UserInfoDB.getInstance(this);
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
        //
        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                /************* Put functions here *************/
                //Toast.makeText(context, "Service is still running", Toast.LENGTH_LONG).show();
                getbeforeAssignments();
                getAssignments();
                checkAlarm();
                /************* Put functions here *************/
                handler.postDelayed(runnable, 10000);    //min*60000, 30min=>1800000
            }
        };
        handler.postDelayed(runnable, 10000);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(runnable);
        //Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
        //Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show();
        id = intent.getStringExtra("id");
        pwd = intent.getStringExtra("pwd");
        //return super.onStartCommand(intent,flags,startid);
        return Service.START_REDELIVER_INTENT;
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
            }
        }
        InsertRunnable insertRunnable = new InsertRunnable();
        Thread addThread = new Thread(insertRunnable);
        addThread.start();
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


}