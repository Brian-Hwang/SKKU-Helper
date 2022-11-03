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

public class MainActivity extends AppCompatActivity implements APIStatusDelegate, ErrorDelegate {

    /************* Canvas API GLOBAL Variables *************/
    public final static String DOMAIN = "https://canvas.skku.edu/";
    public final static String TOKEN = "i8NCRKu4HN55HJ9bTdPoziLUybwKS1xYBtlA5dyzwHtXPI8kWPlrcHOZkMlMnfMP";

    public final static String SECTION_DIVIDER = " \n \n ------------------- \n \n";
    CanvasCallback<Conversation[]> conversationCanvasCallback;
    CanvasCallback<Course[]> courseCanvasCallback;
    CanvasCallback<ToDo[]> todoCanvasCallback;
    String nextURL = "";
    ScrollView scrollView;
    Button loadNextURLButton;
    TextView output;
    /************* Canvas API GLOBAL Variables *************/

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private String id,pwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
//        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        /************* Canvas API Local Variables *************/
        output = findViewById(R.id.output);
        loadNextURLButton = findViewById(R.id.loadNextPage);
        scrollView = findViewById(R.id.scrollview);
        /************* Canvas API Local Variables *************/

        /************* Canvas API CREATE *************/
        //Set up CanvasAPI
        setUpCanvasAPI();

        todoCanvasCallback = new CanvasCallback<ToDo[]>(this) {

            @Override
            public void cache(ToDo[] todos) {
                //Cache will ALWAYS come before firstPage.
                //Only the firstPage of any API is ever cached.
                for (ToDo todo : todos) {
                    appendToTextView("[CACHED] " + todo.getTitle() + ": " + todo.getAssignment());
                    appendToTextView("[CACHED] " + todo.getTitle() + ": " +todo.getTitle());
                    appendToTextView("[CACHED] " + todo.getTitle() + ": " +todo.getDueDate());
                    appendToTextView("[CACHED] " + todo.getTitle() + ": " +todo.isChecked());
                }
                appendToTextView(SECTION_DIVIDER);
            }

            @Override
            public void firstPage(ToDo[] todos, LinkHeaders linkHeaders, retrofit.client.Response response) {
                //Save the next url for pagination.
                nextURL = linkHeaders.nextURL;

                for (ToDo todo : todos) {
                    appendToTextView(todo.getTitle() + ": " + todo.getAssignment());
                    appendToTextView(todo.getTitle() + ": " +todo.getTitle());
                    appendToTextView(todo.getTitle() + ": " +todo.getDueDate());
                    appendToTextView(todo.getTitle() + ": " +todo.isChecked());                }
            }

            @Override
            public void nextPage(ToDo[] todos, LinkHeaders linkHeaders, retrofit.client.Response response) {
                //nextPage is an optional override. The default behavior is to simply call firstPage() as we've done here;
                firstPage(todos, linkHeaders, response);
            }
        };

        courseCanvasCallback = new CanvasCallback<Course[]>(this) {
            @Override
            public void cache(Course[] courses) {
                for (Course course : courses){
                    appendToTextView("[CACHED] " + course.getId() + ": " + course.getName());
//                    appendToTextView("[CACHED] " + course.getId() + ": " + course.getCourseCode());

                }
                appendToTextView(SECTION_DIVIDER);
            }

            @Override
            public void firstPage(Course[] courses, LinkHeaders linkHeaders, Response response) {
                //Save the next url for pagination.
                nextURL = linkHeaders.nextURL;

                for (Course course : courses) {
                    appendToTextView(course.getId() + ": " + course.getName());
//                    appendToTextView(course.getId() + ": " + course.getCourseCode());
                }
            }
            @Override
            public void nextPage(Course[] courses, LinkHeaders linkHeaders, retrofit.client.Response response) {
                //nextPage is an optional override. The default behavior is to simply call firstPage() as we've done here;
                firstPage(courses, linkHeaders, response);
            }
        };

        //Set up the callback
        conversationCanvasCallback = new CanvasCallback<Conversation[]>(this) {
            @Override
            public void cache(Conversation[] conversations) {
                //Cache will ALWAYS come before firstPage.
                //Only the firstPage of any API is ever cached.
                for (Conversation conversation : conversations) {
                    appendToTextView("[CACHED] " + conversation.getId() + ": " + conversation.getLastMessagePreview());
                }
                appendToTextView(SECTION_DIVIDER);
            }

            @Override
            public void firstPage(Conversation[] conversations, LinkHeaders linkHeaders, retrofit.client.Response response) {
                //Save the next url for pagination.
                nextURL = linkHeaders.nextURL;

                for (Conversation conversation : conversations) {
                    appendToTextView(conversation.getId() + ": " + conversation.getLastMessagePreview());
                }
            }

            @Override
            public void nextPage(Conversation[] conversations, LinkHeaders linkHeaders, retrofit.client.Response response) {
                //nextPage is an optional override. The default behavior is to simply call firstPage() as we've done here;
                firstPage(conversations, linkHeaders, response);
            }

        };
        //If they press the button, make an API call.
        loadNextURLButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeAPICall();
            }
        });

        //Make the actual API call.
        makeAPICall();

        /************* Canvas API CREATE *************/


        /************* Channel creation for Notification *************/
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        {
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
//        Button btn = findViewById(R.id.testBtn);
//        /* service start */
//        Intent startIntent = new Intent(this, BackgroundService.class);
//        id="testId";
//        pwd="testPwd";
//        startIntent.putExtra("id",id);
//        startIntent.putExtra("pwd",pwd);
//        startService(startIntent);
//        /* service terminate */
//        btn.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this,BackgroundService.class);
//                stopService(intent);
//            }
//        });
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
    /**
     * Helper for making an API call.
     */

    public void makeAPICall() {
        //Don't let them spam the button.
        loadNextURLButton.setEnabled(false);

        //Check if the first api call has come back.
        if ("".equals(nextURL)) {
            ToDoAPI.getUserTodos(todoCanvasCallback);
            CourseAPI.getFirstPageFavoriteCourses(courseCanvasCallback);
        }
        //Check if we're at the end of the paginated list.
        else if (nextURL != null) {
            ToDoAPI.getUserTodos(todoCanvasCallback);
            CourseAPI.getNextPageCourses(courseCanvasCallback,nextURL);
//            ConversationAPI.getNextPageConversations(conversationCanvasCallback, nextURL);
        }
        //We are at the end of the list.
        else {
            Toast.makeText(getContext(), getString(R.string.noMoreItems), Toast.LENGTH_LONG).show();
            loadNextURLButton.setEnabled(true);
        }
//        //Check if the first api call has come back.
//        if ("".equals(nextURL)) {
//            ConversationAPI.getFirstPageConversations(conversationCanvasCallback, ConversationAPI.ConversationScope.ALL);
//        }
//        //Check if we're at the end of the paginated list.
//        else if (nextURL != null) {
//            ConversationAPI.getNextPageConversations(conversationCanvasCallback, nextURL);
//        }
//        //We are at the end of the list.
//        else {
//            Toast.makeText(getContext(), getString(R.string.noMoreItems), Toast.LENGTH_LONG).show();
//            loadNextURLButton.setEnabled(true);
//        }
    }

    /**
     * Helper for appending to a text view
     */

    public void appendToTextView(String text){
        if(output == null){
            output = (TextView)findViewById(R.id.output);
        }

        String current = output.getText().toString();
        output.setText(current + "\n" + text);

        //Scroll to the bottom of the scrollview.
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
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
        //The API call has finished.

        appendToTextView(SECTION_DIVIDER);

        //Let them load another page now.
        loadNextURLButton.setEnabled(true);
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