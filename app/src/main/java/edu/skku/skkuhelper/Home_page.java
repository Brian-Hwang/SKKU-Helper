package edu.skku.skkuhelper;

import static java.lang.Thread.sleep;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.room.Room;

import com.google.android.material.navigation.NavigationView;
import com.instructure.canvasapi.api.CourseAPI;
import com.instructure.canvasapi.api.ToDoAPI;
import com.instructure.canvasapi.api.UserAPI;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.CanvasError;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.ToDo;
import com.instructure.canvasapi.model.User;
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
import java.util.List;
import java.util.Objects;



import edu.skku.skkuhelper.roomdb.SKKUNotice;
import edu.skku.skkuhelper.roomdb.SKKUNoticeDB;
import edu.skku.skkuhelper.roomdb.SKKUNoticeDao;
import edu.skku.skkuhelper.roomdb.SKKUAssignment;
import edu.skku.skkuhelper.roomdb.SKKUAssignmentDB;
import edu.skku.skkuhelper.roomdb.SKKUAssignmentDao;
import edu.skku.skkuhelper.roomdb.UserInfo;
import edu.skku.skkuhelper.roomdb.UserInfoDB;
import edu.skku.skkuhelper.roomdb.UserInfoDao;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class Home_page extends AppCompatActivity implements APIStatusDelegate, ErrorDelegate, NavigationView.OnNavigationItemSelectedListener {
    private long time = 0;
    NavigationView navigationView;
    /************* Canvas API GLOBAL Variables *************/
    private AppBarConfiguration appBarConfiguration;
    public final static String DOMAIN = "https://canvas.skku.edu/";
    public static String TOKEN;

    LinkedHashMap<Long, String> courseList = new LinkedHashMap<>();
    ArrayList<todoClass> todolist = new ArrayList<>();

    CanvasCallback<Course[]> courseCanvasCallback;
    CanvasCallback<ToDo[]> todosCanvasCallback;
    LinkedHashMap<Long, Long> assignmentidList = new LinkedHashMap<>();
    UserCallback userCallback;
    String userId=null;
    String userName;
    boolean isNukeFinished = false;
    boolean isCallbackFinished = false;
    /************* Canvas API GLOBAL Variables *************/

    /************* Room DB GLOBAL Variables *************/
    SKKUAssignmentDB SKKUassignmentDB = null;
    UserInfoDB userinfoDB = null;
    /************* Room DB GLOBAL Variables *************/


    FragmentManager manager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        stopService(new Intent(Home_page.this,BackgroundService.class));

        /************* Room DB CREATE START *************/
        userinfoDB = UserInfoDB.getInstance(this);
        UserInfoDB infoDB = Room.databaseBuilder(getApplicationContext(), UserInfoDB.class, "userifo.db").build();
        UserInfoDao userInfoDao = infoDB.UserinfoDao();
        SKKUassignmentDB = SKKUAssignmentDB.getInstance(this);
        /************* Room DB CREATE END *************/
        setContentView(R.layout.activity_navigation_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Bundle bundle = getIntent().getExtras();
        TOKEN = bundle.getString("TOKEN");

        Log.d("TOKEN", TOKEN);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        Log.d("TOKEN", TOKEN);



        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        manager = getSupportFragmentManager();
        Objects.requireNonNull(getSupportActionBar()).setTitle("Lecture/Assignment");
        Log.d("TOKEN", TOKEN);





        /************* Canvas API CREATE START *************/

        //Set up CanvasAPI
        setUpCanvasAPI();

        userCallback = new UserCallback(Home_page.this) {

            @Override
            public void cachedUser(User user) {
                APIHelpers.setCacheUser(Home_page.this, user);
            }

            @Override
            public void user(User user, Response response) {
                userId = user.getLoginId();
                userName = user.getName();
            }
        };

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

        todosCanvasCallback = new CanvasCallback<ToDo[]>(Home_page.this) {
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
            }
        };

        getbeforeAssignments();
        while (true)
            if (isNukeFinished) {
                Log.d("BEFOREASSIGNMENT", "FINISHED12");
                CourseAPI.getFirstPageFavoriteCourses(courseCanvasCallback);
                ToDoAPI.getUserTodos(todosCanvasCallback);
                UserAPI.getSelf(userCallback);
                break;
            }

//        CourseAPI.getFirstPageFavoriteCourses(courseCanvasCallback);
//        ToDoAPI.getUserTodos(todosCanvasCallback);
//        UserAPI.getSelf(userCallback);



        /************* Canvas API CREATE END*************/



    }

    @Override
    protected void onRestart() {
//        stopService(new Intent(Home_page.this,BackgroundService.class));
        super.onRestart();
    }

    public void getbeforeAssignments() {

        class InsertRunnable implements Runnable {
            @Override
            public void run() {
                List<SKKUAssignment> listTemp = SKKUassignmentDB.SKKUassignmentDao().getAll();
                if(listTemp!=null){
                    for (SKKUAssignment temp : listTemp) {
                        Log.d("BEFOREASSIGNMENT", String.valueOf(temp.isAlarm));
                        assignmentidList.put(temp.assignmentId, temp.isAlarm);
                    }
                }
                Log.d("BEFOREASSIGNMENT", "FINISHED1");
                SKKUassignmentDB.SKKUassignmentDao().nukeTable();
                while(true){
                    if(SKKUassignmentDB.SKKUassignmentDao().getRowCount()==0) {
                        Log.d("isNukeFinished", String.valueOf(true));
                        isNukeFinished = true;
                        break;
                    }
                }
                Log.d("BEFOREASSIGNMENT", "FINISHED2");


            }
        }
        InsertRunnable insertRunnable = new InsertRunnable();
        Thread addThread = new Thread(insertRunnable);
        addThread.start();

    }
    @Override
    public void onBackPressed() {
        Toast toast = Toast.makeText(getApplicationContext(),"뒤로 버튼을 한번 더 누르면 종료합니다.",Toast.LENGTH_SHORT);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            //뒤로가기 두번 하면 종료
            if (System.currentTimeMillis() > time + 2000) {
                time = System.currentTimeMillis();
                toast.show();
                return;
            }
            if (System.currentTimeMillis() <= time + 2000) {
                finishAffinity();
                toast.cancel();
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        SharedPreferences.Editor editor;
        SharedPreferences setting;
        setting = getSharedPreferences("setting", 0);
        editor= setting.edit();

        FragmentManager manager = getSupportFragmentManager();

        if (id == R.id.nav_icampus) {
            getSupportActionBar().setTitle("Lecture/Assignment");
            manager.beginTransaction().replace(R.id.content_main, new BlankFragment()).commit();
        } else if (id == R.id.nav_notification) {
            getSupportActionBar().setTitle("Notification");
            manager.beginTransaction().replace(R.id.content_main, new BlankFragment3()).commit();
        }
        //logout
        else if (id == R.id.nav_logout) {
            editor.clear();
            editor.commit();


            //Intent intentBackground = new Intent(Home_page.this,BackgroundService.class);
            //stopService(intentBackground);
            /* erase user id, password.*/
            Intent intent = new Intent(Home_page.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
                /************* Example of Background Service *************/
        /* service start */
        Intent startIntent = new Intent(this, BackgroundService.class);
        startForegroundService(startIntent);
        /************* Example of Background Service *************/
        super.onDestroy();
    }

    @Override
    protected void onResume() {
//        stopService(new Intent(Home_page.this,BackgroundService.class));
        super.onResume();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }


    @Override
    public void onCallbackStarted() {
    }

//    @Override
//    public void onCallbackFinished(CanvasCallback.SOURCE source) {
//        Log.d("BEFOREASSIGNMENT1", String.valueOf(assignmentidList.size()));
//        if (courseCanvasCallback.isFinished()) {
//            Log.d("APICALLBACK1", source.name());
//            for (todoClass todos : todolist) {
//                Log.d("TODO LIST : ", String.valueOf(todos.assignmentName) + String.valueOf(todos.courseName) + String.valueOf(todos.assignmentId) + String.valueOf(todos.courseId) + String.valueOf(todos.isLecture) + String.valueOf(todos.dueDate) + String.valueOf(todos.url));
//
//                class InsertRunnable implements Runnable {
//                    @Override
//                    public void run() {
//                        SKKUAssignment todoTemp = new SKKUAssignment();
//                        Log.d("BEFOREASSIGNMENT2", String.valueOf(todos.assignmentId));
//                        Log.d("BEFOREASSIGNMENT3", String.valueOf(assignmentidList.get(todos.assignmentId)));
//
//                        if (assignmentidList.containsKey(todos.assignmentId) &&assignmentidList.get(todos.assignmentId)!=null )
//                            todoTemp.isAlarm = assignmentidList.get(todos.assignmentId);
//                        else
//                            todoTemp.isAlarm = 3;
//
//                        todoTemp.isLecture = todos.isLecture;
//                        todoTemp.assignmentName = todos.assignmentName;
//                        todoTemp.courseName = todos.courseName;
//                        todoTemp.assignmentId = todos.assignmentId;
//                        todoTemp.courseId = todos.courseId;
//                        todoTemp.dueDate = todos.dueDate;
//                        todoTemp.url = todos.url;
//                        SKKUassignmentDB.SKKUassignmentDao().insert(todoTemp);
//                        while(true) {
//                            Log.d("HERERERERE", SKKUassignmentDB.SKKUassignmentDao().getRowCount() + "" + todolist.size());
//                            if (SKKUassignmentDB.SKKUassignmentDao().getRowCount() == todolist.size()){
//                                isCallbackFinished = true;
//                                break;
//                            }
//                        }
//                    }
//
//                }
//
//                InsertRunnable insertRunnable = new InsertRunnable();
//                Thread addThread = new Thread(insertRunnable);
//                addThread.start();
//            }
//            while(true){
//                if(isCallbackFinished){
//                    Log.d("CALLBACKFINISHED","TRUE");
//                    manager.beginTransaction().replace(R.id.content_main, new BlankFragment()).commit();
//                    break;
//                }
//            }
//        }
//        if (userId != null) {
//            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//            Log.d("APICALLBACK2", source.name());
//            class InsertRunnable2 implements Runnable {
//                @Override
//                public void run() {
//                    UserInfo userinfoTemp = new UserInfo();
//                    userinfoTemp.userTOKEN = TOKEN;
//                    userinfoTemp.userId = userId;
//                    userinfoTemp.userName = userName;
//                    userinfoDB.UserinfoDao().insert(userinfoTemp);
//                    runOnUiThread(() -> {
//                        View view = navigationView.getHeaderView(0);
//                        TextView textView1 = view.findViewById(R.id.textView);
//                        TextView textView2 = view.findViewById(R.id.studentName);
//                        textView1.setText(userId);
//                        textView2.setText(userName);
//                    });
//                }
//            }
//
//            InsertRunnable2 insertRunnable2 = new InsertRunnable2();
//            Thread addThread2 = new Thread(insertRunnable2);
//            addThread2.start();
//        }
//    }
@Override
public void onCallbackFinished(CanvasCallback.SOURCE source) {
    Log.d("BEFOREASSIGNMENT1", String.valueOf(assignmentidList.size()));
    if (courseCanvasCallback.isFinished()) {
        Log.d("APICALLBACK1", source.name());
        for (todoClass todos : todolist) {
            Log.d("TODO LIST : ", String.valueOf(todos.assignmentName) + String.valueOf(todos.courseName) + String.valueOf(todos.assignmentId) + String.valueOf(todos.courseId) + String.valueOf(todos.isLecture) + String.valueOf(todos.dueDate) + String.valueOf(todos.url));

            class InsertRunnable implements Runnable {
                @Override
                public void run() {
                    SKKUAssignment todoTemp = new SKKUAssignment();
                    Log.d("BEFOREASSIGNMENT2", String.valueOf(todos.assignmentId));
                    Log.d("BEFOREASSIGNMENT3", String.valueOf(assignmentidList.get(todos.assignmentId)));

                    if (assignmentidList.containsKey(todos.assignmentId) &&assignmentidList.get(todos.assignmentId)!=null )
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

            InsertRunnable insertRunnable = new InsertRunnable();
            Thread addThread = new Thread(insertRunnable);
            addThread.start();
        }
        manager.beginTransaction().replace(R.id.content_main, new BlankFragment()).commit();
    }
    if (userId != null) {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Log.d("APICALLBACK2", source.name());
        class InsertRunnable2 implements Runnable {
            @Override
            public void run() {
                UserInfo userinfoTemp = new UserInfo();
                userinfoTemp.userTOKEN = TOKEN;
                userinfoTemp.userId = userId;
                userinfoTemp.userName = userName;
                userinfoDB.UserinfoDao().insert(userinfoTemp);
                runOnUiThread(() -> {
                    View view = navigationView.getHeaderView(0);
                    TextView textView1 = view.findViewById(R.id.textView);
                    TextView textView2 = view.findViewById(R.id.studentName);
                    textView1.setText(userId);
                    textView2.setText(userName);
                });
            }
        }

        InsertRunnable2 insertRunnable2 = new InsertRunnable2();
        Thread addThread2 = new Thread(insertRunnable2);
        addThread2.start();
    }
}

//    @Override
//    public void onCallbackFinished(CanvasCallback.SOURCE source) {
//        if (courseCanvasCallback.isFinished()) {
//            Log.d("APICALLBACK1", source.name());
//            for (todoClass todos : todolist) {
//                Log.d("TODO LIST : ", String.valueOf(todos.assignmentName) + String.valueOf(todos.courseName) + String.valueOf(todos.assignmentId) + String.valueOf(todos.courseId) + String.valueOf(todos.isLecture) + String.valueOf(todos.dueDate) + String.valueOf(todos.url));
//
//                class InsertRunnable implements Runnable {
//                    @Override
//                    public void run() {
//                        SKKUAssignment todoTemp = new SKKUAssignment();
//                        todoTemp.isLecture = todos.isLecture;
//                        todoTemp.assignmentName = todos.assignmentName;
//                        todoTemp.courseName = todos.courseName;
//                        todoTemp.assignmentId = todos.assignmentId;
//                        todoTemp.courseId = todos.courseId;
//                        todoTemp.dueDate = todos.dueDate;
//                        todoTemp.url = todos.url;
//                        SKKUassignmentDB.SKKUassignmentDao().insert(todoTemp);
//                    }
//                }
//
//                InsertRunnable insertRunnable = new InsertRunnable();
//                Thread addThread = new Thread(insertRunnable);
//                addThread.start();
//            }
//            manager.beginTransaction().replace(R.id.content_main, new BlankFragment()).commit();
//        }
//        if (userId!=null) {
//            Log.d("APICALLBACK2", source.name());
//            class InsertRunnable2 implements Runnable {
//                @Override
//                public void run() {
//                    UserInfo userinfoTemp = new UserInfo();
//                    userinfoTemp.userTOKEN = TOKEN;
//                    userinfoTemp.userId = userId;
//                    userinfoTemp.userName = userName;
//                    userinfoDB.UserinfoDao().insert(userinfoTemp);
//                    runOnUiThread(() -> {
//                        View view = navigationView.getHeaderView(0);
//                        TextView textView1 = view.findViewById(R.id.textView);
//                        TextView textView2 = view.findViewById(R.id.studentName);
//                        textView1.setText(userId);
//                        textView2.setText(userName);
//                    });
//                }
//            }
//
//            InsertRunnable2 insertRunnable2 = new InsertRunnable2();
//            Thread addThread2 = new Thread(insertRunnable2);
//            addThread2.start();
//        }
//    }

    @Override
    public void onNoNetwork() {

    }

    @Override
    public Context getContext() {
        return this;
    }


    /**
     * Error Delegate Overrides.
     */

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
