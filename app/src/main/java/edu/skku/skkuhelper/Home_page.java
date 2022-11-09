package edu.skku.skkuhelper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import java.util.Objects;

import edu.skku.skkuhelper.roomdb.SKKUAssignment;
import edu.skku.skkuhelper.roomdb.SKKUAssignmentDB;
import edu.skku.skkuhelper.roomdb.Userinfo;
import edu.skku.skkuhelper.roomdb.UserinfoDB;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class Home_page extends AppCompatActivity implements APIStatusDelegate, ErrorDelegate, NavigationView.OnNavigationItemSelectedListener {
    private long time = 0;
    /************* Canvas API GLOBAL Variables *************/
    private AppBarConfiguration appBarConfiguration;
    public final static String DOMAIN = "https://canvas.skku.edu/";
    public static String TOKEN = "i8NCRKu4HN55HJ9bTdPoziLUybwKS1xYBtlA5dyzwHtXPI8kWPlrcHOZkMlMnfMP";

    LinkedHashMap<Long, String> courseList = new LinkedHashMap<>();
    ArrayList<todoClass> todolist = new ArrayList<>();

    CanvasCallback<Course[]> courseCanvasCallback;
    CanvasCallback<ToDo[]> todosCanvasCallback;
    UserCallback userCallback;
    String userId;
    String userName;
    /************* Canvas API GLOBAL Variables *************/

    /************* Room DB GLOBAL Variables *************/
    SKKUAssignmentDB SKKUassignmentDB = null;
    UserinfoDB userinfoDB = null;
    /************* Room DB GLOBAL Variables *************/


    FragmentManager manager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Bundle bundle = getIntent().getExtras();
        TOKEN = bundle.getString("TOKEN");


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        manager = getSupportFragmentManager();
        Objects.requireNonNull(getSupportActionBar()).setTitle("Lecture/Assignment");

        /************* Room DB CREATE START *************/
        SKKUassignmentDB = SKKUAssignmentDB.getInstance(this);
        userinfoDB = userinfoDB.getInstance(this);
        /************* Room DB CREATE END *************/


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

        UserAPI.getSelf(userCallback);
        CourseAPI.getFirstPageFavoriteCourses(courseCanvasCallback);
        ToDoAPI.getUserTodos(todosCanvasCallback);
        /************* Canvas API CREATE END*************/

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
            finishAffinity();
            /* erase user id, password.*/
            Intent intent = new Intent(Home_page.this, MainActivity.class);
            startActivity(intent);
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
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
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
        class InsertRunnable2 implements Runnable {
            @Override
            public void run() {
                Userinfo userinfoTemp = new Userinfo();
                userinfoTemp.userTOKEN = TOKEN;
                userinfoTemp.userId = userId;
                userinfoTemp.userName = userName;
                userinfoDB.UserinfoDao().insert(userinfoTemp);
            }
        }
        InsertRunnable2 insertRunnable2 = new InsertRunnable2();
        Thread addThread2 = new Thread(insertRunnable2);
        addThread2.start();



        manager.beginTransaction().replace(R.id.content_main, new BlankFragment()).commit();

    }

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
