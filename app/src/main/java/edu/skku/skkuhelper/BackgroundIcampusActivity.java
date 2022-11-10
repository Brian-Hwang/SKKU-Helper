package edu.skku.skkuhelper;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;
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
import com.instructure.canvasapi.utilities.UserCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

import edu.skku.skkuhelper.roomdb.SKKUAssignment;
import edu.skku.skkuhelper.roomdb.SKKUAssignmentDB;
import edu.skku.skkuhelper.roomdb.SKKUAssignmentDao;
import edu.skku.skkuhelper.roomdb.UserInfoDB;
import edu.skku.skkuhelper.roomdb.UserInfoDao;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class BackgroundIcampusActivity extends AppCompatActivity implements APIStatusDelegate, ErrorDelegate {

    /************* Canvas API GLOBAL Variables *************/
    private AppBarConfiguration appBarConfiguration;
    public final static String DOMAIN = "https://canvas.skku.edu/";
    public static String TOKEN=null;

    LinkedHashMap<Long, String> courseList = new LinkedHashMap<>();
    ArrayList<todoClass> todolist = new ArrayList<>();

    CanvasCallback<Course[]> courseCanvasCallback;
    CanvasCallback<ToDo[]> todosCanvasCallback;
    //    UserCallback userCallback;
    String userId;
    String userName;
    /************* Canvas API GLOBAL Variables *************/

    /************* Room DB GLOBAL Variables *************/
    SKKUAssignmentDB SKKUassignmentDB = null;
    UserInfoDB userinfoDB = null;
    /************* Room DB GLOBAL Variables *************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /************* Room DB CREATE START *************/
        userinfoDB = UserInfoDB.getInstance(this);
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


        while(true){
            if (TOKEN != null) break;
        }

        //Set up CanvasAPI
        setUpCanvasAPI();

        courseCanvasCallback = new CanvasCallback<Course[]>(this) {
            @Override
            public void cache(Course[] courses) {
            }

            @Override
            public void firstPage(Course[] courses, LinkHeaders linkHeaders, Response response) {
                for (Course course : courses) {
                    courseList.put(course.getId(),course.getName());

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
                    while(true){
                        if (courseList.size() != 0) break;
                    }
                    Date today = new Date();
                    if(todo.getAssignment().getPointsPossible()==0.0f || todo.getAssignment().getDueDate().before(today))
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
        /************* Canvas API CREATE END*************/

    }

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
        for(todoClass todos : todolist) {
            Log.d("TODO LIST : ", String.valueOf(todos.assignmentName) + String.valueOf(todos.courseName) + String.valueOf(todos.assignmentId) + String.valueOf(todos.courseId) + String.valueOf(todos.isLecture) + String.valueOf(todos.dueDate) + String.valueOf(todos.url));

            class InsertRunnable implements Runnable {
                @Override
                public void run() {
                    SKKUAssignment todoTemp = new SKKUAssignment();
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

            InsertRunnable insertRunnable = new InsertRunnable();
            Thread addThread = new Thread(insertRunnable);
            addThread.start();
        }
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
